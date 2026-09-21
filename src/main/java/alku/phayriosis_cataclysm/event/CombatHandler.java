package alku.phayriosis_cataclysm.event;

import alku.phayriosis_cataclysm.Config;
import alku.phayriosis_cataclysm.Phayriosis_cataclysm;
import alku.phayriosis_cataclysm.core.InfectionStageManager;
import alku.phayriosis_cataclysm.core.PhayriosisCompat;
import alku.phayriosis_cataclysm.entity.FusedHorrorEntity;
import alku.phayriosis_cataclysm.entity.ai.AdaptiveCarapaceHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles combat-related mechanics:
 * 
 * 1. Adaptive Carapace: High-tier infection mobs gain damage resistance
 *    to a specific damage type after being hit by it consecutively.
 * 
 * 2. Call Swarm sound effects: Plays global sound when swarm call triggers.
 * 
 * 3. Stage-based mob buffs: At higher stages, infection mobs get passive buffs.
 */
@Mod.EventBusSubscriber(modid = Phayriosis_cataclysm.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CombatHandler {

    // Track damage history per entity for carapace system (for non-FusedHorror entities)
    private static final Map<UUID, AdaptiveCarapaceHandler> carapaceHandlers = new HashMap<>();

    /**
     * Optionally cancels Phayriosis Reborn's unattributed scripted damage. To
     * players this is the instant-damage aura: it has no attacker, no attack
     * animation, and appears as soon as a parasite has a target. Normal melee
     * attacks still use their own damage source and are kept.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurtAura(LivingHurtEvent event) {
        if (Config.PARASITE_AURA_DAMAGE_ENABLED.get()) return;

        LivingEntity victim = event.getEntity();
        if (victim == null || victim.level().isClientSide()) return;
        if (PhayriosisCompat.isInfectionMob(victim)) return;

        DamageSource source = event.getSource();
        if (source.getEntity() != null || source.getDirectEntity() != null) return;
        if (!PhayriosisCompat.isUnattributedAuraDamage(source)) return;

        event.setCanceled(true);
    }

    /**
     * Handle damage events for adaptive carapace on Phayriosis entities.
     * FusedHorrorEntity handles its own carapace internally.
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!Config.ENABLE_ADAPTIVE_CARAPACE.get()) return;

        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;

        // Only process for infection mobs
        if (!PhayriosisCompat.isInfectionMob(victim)) return;

        int stage = InfectionStageManager.getCurrentStage();

        // Adaptive carapace for Phayriosis entities at high stage
        if (stage >= Config.CARAPACE_MIN_STAGE.get()) {
            // Don't double-process for FusedHorror (it handles its own)
            if (victim instanceof FusedHorrorEntity) return;

            AdaptiveCarapaceHandler handler = carapaceHandlers.computeIfAbsent(
                    victim.getUUID(), k -> new AdaptiveCarapaceHandler());

            float originalDamage = event.getAmount();
            float reducedDamage = handler.processDamage(victim, event.getSource(), originalDamage);

            if (reducedDamage < originalDamage) {
                event.setAmount(reducedDamage);
                // Play a "clink" sound to indicate carapace absorption
                victim.level().playSound(null, victim.blockPosition(),
                        SoundEvents.ANVIL_PLACE, SoundSource.HOSTILE, 0.5f, 1.5f);
            }
        }
    }

    /**
     * Clean up carapace data when entities die or unload.
     */
    @SubscribeEvent
    public static void onLivingDeath(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        Entity entity = event.getEntity();
        carapaceHandlers.remove(entity.getUUID());
    }

    /**
     * Apply stage-based buffs to infection mobs when they spawn or are ticked.
     * Higher stages make infection mobs tougher passively.
     */
    @SubscribeEvent
    public static void onLivingTick(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (entity.tickCount % 100 != 0) return; // Only check every 5 seconds

        if (!PhayriosisCompat.isInfectionMob(entity)) return;

        int stage = InfectionStageManager.getCurrentStage();

        // Infection mobs regenerate at the configured stage
        if (stage >= Config.REGEN_MIN_STAGE.get() && entity.tickCount % 200 == 0) {
            if (entity.getHealth() < entity.getMaxHealth()) {
                entity.heal(1.0f);
            }
        }

        // Infection mobs stop burning at the configured stage
        if (stage >= Config.FIRE_RESISTANCE_MIN_STAGE.get() && entity.tickCount % 400 == 0) {
            entity.setRemainingFireTicks(0);
        }
    }

    /**
     * Periodically clean up stale carapace handler entries.
     */
    @SubscribeEvent
    public static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;
        if (event.getServer().getTickCount() % 6000 != 0) return; // Every 5 minutes

        // Remove handlers for entities that no longer exist
        carapaceHandlers.entrySet().removeIf(entry -> {
            for (ServerLevel level : event.getServer().getAllLevels()) {
                if (level.getEntity(entry.getKey()) != null) return false;
            }
            return true;
        });
    }
}
