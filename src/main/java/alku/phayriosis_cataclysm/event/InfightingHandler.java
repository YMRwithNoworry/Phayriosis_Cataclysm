package alku.phayriosis_cataclysm.event;

import alku.phayriosis_cataclysm.Config;
import alku.phayriosis_cataclysm.Phayriosis_cataclysm;
import alku.phayriosis_cataclysm.core.PhayriosisCompat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

/**
 * Keeps the PHA mods from fighting each other.
 *
 * <p>With both Phayriosis versions installed at once (and this addon on top) the creature
 * families can end up hunting each other, which is both unintended lore-wise and a waste of
 * the infection's strength. This handler makes them ignore each other instead. The scope is
 * configurable: cross-mod only (default), all PHA creatures, or off.</p>
 *
 * <p>Three interception points, because there is no single choke point for "attacking":</p>
 * <ol>
 *   <li>{@link LivingChangeTargetEvent} - every target acquisition (nearest-player goals,
 *       retaliation, mob-made decisions) funnels through {@code Mob#setTarget}, so cancelling
 *       there stops the aggro before it ever starts.</li>
 *   <li>{@link LivingAttackEvent} - cancels hits that slip through (targets restored from a
 *       save, projectiles, a mob already locked on before this addon loaded, or a rule change).</li>
 *   <li>{@link EntityJoinLevelEvent} - drops a stale target when the creature (re)enters the
 *       world, which is what makes the rule apply to already existing mobs.</li>
 * </ol>
 *
 * <p>Phayriosis Reborn delivers most of its damage through an unattributed contact aura; for
 * protected victims that aura is traced back to the nearest infection mob targeting them and
 * cancelled as well.</p>
 */
@Mod.EventBusSubscriber(modid = Phayriosis_cataclysm.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InfightingHandler {

    /**
     * Never let a protected pair pick each other as a target.
     */
    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (event.isCanceled()) return;
        if (isProtected(event.getEntity(), event.getNewTarget())) {
            event.setCanceled(true);
        }
    }

    /**
     * Stop damage between protected creatures, including hits whose target was set before the
     * rule applied.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;

        if (isProtected(PhayriosisCompat.getAttacker(event.getSource()), victim)) {
            event.setCanceled(true);
            return;
        }

        // Phayriosis Reborn's scripted aura carries no attacker at all; only bother tracing it
        // when the victim itself is a PHA creature.
        DamageSource source = event.getSource();
        if (source.getEntity() == null && source.getDirectEntity() == null
                && isPhaCreature(victim)
                && victim.level() instanceof ServerLevel level
                && PhayriosisCompat.isUnattributedAuraDamage(source)
                && isProtected(PhayriosisCompat.findTargetingInfectionMob(level, victim), victim)) {
            event.setCanceled(true);
        }
    }

    /**
     * Drop an already set target when a creature joins the level, so mobs that were locked onto
     * each other before this addon loaded let go.
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Mob mob)) return;

        LivingEntity target = mob.getTarget();
        if (isProtected(mob, target)) {
            mob.setTarget(null);
        }
    }

    /**
     * Whether an attack (or a target acquisition) between these two entities must be blocked
     * under the configured {@link Config.InfightingScope}.
     */
    public static boolean isProtected(@Nullable Entity attacker, @Nullable Entity victim) {
        if (attacker == null || victim == null || attacker == victim) return false;
        if (!(attacker instanceof LivingEntity) || !(victim instanceof LivingEntity)) return false;
        if (!isPhaCreature(attacker) || !isPhaCreature(victim)) return false;

        Config.InfightingScope scope = Config.PHA_INFIGHTING.get();
        return switch (scope) {
            case OFF -> false;
            case ALL_PHA -> true;
            case CROSS_MOD -> !namespaceOf(attacker).equals(namespaceOf(victim));
        };
    }

    /** Whether this entity belongs to one of the configured PHA namespaces. */
    public static boolean isPhaCreature(Entity entity) {
        String id = PhayriosisCompat.getEntityId(entity);
        if (id == null) return false;
        String namespace = namespaceOf(id);
        return Config.PHA_NAMESPACES.get().contains(namespace);
    }

    /** Namespace part of a registry id ("phayriosis:primitive_dreadmind" -> "phayriosis"). */
    private static String namespaceOf(Entity entity) {
        String id = PhayriosisCompat.getEntityId(entity);
        return id == null ? "" : namespaceOf(id);
    }

    private static String namespaceOf(String registryId) {
        ResourceLocation location = ResourceLocation.tryParse(registryId);
        return location == null ? "" : location.getNamespace();
    }
}
