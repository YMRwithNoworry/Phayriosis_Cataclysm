package alku.phayriosis_cataclysm.event;

import alku.phayriosis_cataclysm.Config;
import alku.phayriosis_cataclysm.Phayriosis_cataclysm;
import alku.phayriosis_cataclysm.core.PhayriosisCompat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Per-hit damage ceiling for every PHA creature.
 *
 * <p>A single blow can only ever take {@code phaDamageCap} health (20 = 10 hearts by default)
 * off a PHA creature, however large it was: a Draconic Evolution blade, a modded nuke, a
 * critical hit with a fully enchanted axe. Armour piercing and true damage are capped as
 * well - the ceiling is a property of the victim, not of the damage type. Players, vanilla
 * mobs and mobs of other mods are left alone, so this is purely a durability buff for the
 * infection.</p>
 *
 * <p>Two interception points, because one is not enough:</p>
 * <ol>
 *   <li>{@link LivingHurtEvent} at {@link EventPriority#LOWEST} - the hit itself is clamped
 *       while it is still whole, before armour, enchantments, Resistance and absorption get
 *       to work on it. This is the value every other combat rule sees, so a capped hit cannot
 *       drain more absorption than it deals damage either.</li>
 *   <li>{@link LivingDamageEvent} at {@link EventPriority#LOWEST} - the amount that is about
 *       to be subtracted from the health bar is clamped one last time, which keeps the
 *       guarantee even if something scaled the damage up in between (holy vulnerability is
 *       handled by running that scaling at {@link EventPriority#LOW} before this).</li>
 * </ol>
 *
 * <p>Damage types listed in {@code damageCapBypassDamageTypes} ignore the cap entirely, so a
 * pack can still let {@code minecraft:out_of_world} or an admin kill through.</p>
 */
@Mod.EventBusSubscriber(modid = Phayriosis_cataclysm.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DamageCapHandler {

    /**
     * Clamp the incoming hit itself (raw damage, before any mitigation).
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;

        float capped = cappedDamage(victim, event.getSource(), event.getAmount());
        if (capped < event.getAmount()) {
            event.setAmount(capped);
            logCap(victim, event.getSource(), capped);
        }
    }

    /**
     * Clamp the final amount, i.e. what the health bar is about to lose.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;

        float capped = cappedDamage(victim, event.getSource(), event.getAmount());
        if (capped < event.getAmount()) {
            event.setAmount(capped);
            logCap(victim, event.getSource(), capped);
        }
    }

    /**
     * {@code amount} reduced to the configured ceiling, or returned unchanged when the cap is
     * off, when the victim is not a PHA creature, when the hit is already small enough or when
     * its damage type is on the bypass list. Public so other rules and tests can reuse the
     * exact same decision.
     */
    public static float cappedDamage(LivingEntity victim, DamageSource source, float amount) {
        if (amount <= 0.0F) return amount;

        float cap = capFor(victim);
        if (amount <= cap) return amount;
        return isBypassed(source) ? amount : cap;
    }

    /**
     * The ceiling a PHA creature is currently protected by, or {@link Float#MAX_VALUE} when the
     * cap does not apply at all.
     */
    public static float capFor(LivingEntity victim) {
        if (!Config.ENABLE_DAMAGE_CAP.get() || !PhayriosisCompat.isPhaCreature(victim)) {
            return Float.MAX_VALUE;
        }
        return Config.PHA_DAMAGE_CAP.get().floatValue();
    }

    /**
     * Whether this damage type is exempted from the cap. Both the full registry id
     * ({@code minecraft:out_of_world}) and the bare registry path ({@code out_of_world} - the
     * path, not the death-message id, which for that type is {@code outOfWorld}) are accepted
     * in the config list.
     */
    private static boolean isBypassed(DamageSource source) {
        List<? extends String> bypass = Config.DAMAGE_CAP_BYPASS_DAMAGE_TYPES.get();
        if (bypass.isEmpty() || source == null) return false;

        return source.typeHolder().unwrapKey()
                .map(key -> bypass.contains(key.location().toString())
                        || bypass.contains(key.location().getPath()))
                // A source built from a direct holder carries no registry key; its message id is
                // the only spelling left, so accept that one too.
                .orElseGet(() -> bypass.contains(source.getMsgId()));
    }

    private static void logCap(LivingEntity victim, DamageSource source, float capped) {
        if (Config.DEBUG_LOGGING.get()) {
            Phayriosis_cataclysm.LOGGER.info("[Cataclysm] damage cap held {} to {} ({})",
                    victim.getName().getString(), capped, source.getMsgId());
        }
    }
}
