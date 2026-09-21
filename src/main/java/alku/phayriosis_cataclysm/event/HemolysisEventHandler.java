package alku.phayriosis_cataclysm.event;

import alku.phayriosis_cataclysm.Config;
import alku.phayriosis_cataclysm.Phayriosis_cataclysm;
import alku.phayriosis_cataclysm.core.PhayriosisCompat;
import alku.phayriosis_cataclysm.init.ModEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Hemolysis (溶血) trigger.
 *
 * <p>A PHA creature that keeps attacking the same victim without actually hurting it
 * eventually gives up on brute force and injects a haemolysin instead. Concretely:
 * after {@code hemolysisHitsToTrigger} (5) consecutive hits on one target, if the
 * damage those hits <em>actually dealt</em> is still below
 * {@code hemolysisDamageRatioThreshold} (400%) of the attacker's own attack power,
 * the target receives a 30 second Hemolysis effect. Every further trigger adds one
 * layer, and each layer replaces the duration.</p>
 *
 * <p>The comparison deliberately uses the damage after armour, enchantments,
 * Resistance, shields and absorption - that is the situation the mechanic exists for:
 * a target that is soaking the hits. A target that takes the hits at full strength
 * simply dies instead of bleeding.</p>
 *
 * <p>Hits are counted in {@link LivingDamageEvent} (post-mitigation health damage)
 * rather than {@link net.minecraftforge.event.entity.living.LivingHurtEvent}
 * (pre-armour damage), and the streak is dropped as soon as the attacker switches
 * targets or the hits are spaced further apart than
 * {@code hemolysisStreakWindow}.</p>
 */
@Mod.EventBusSubscriber(modid = Phayriosis_cataclysm.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HemolysisEventHandler {

    /** Consecutive hits per attacker, keyed by the attacker's UUID. */
    private static final Map<UUID, AttackStreak> STREAKS = new HashMap<>();

    /** How often (in ticks) streaks of entities that no longer exist are purged. */
    private static final int CLEANUP_INTERVAL = 600;

    /**
     * Track the damage a PHA creature actually deals and inflict Hemolysis once the
     * streak is long enough while the damage stayed low. Runs last so it sees the
     * damage every other listener has already agreed on.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!Config.ENABLE_HEMOLYSIS.get()) return;

        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;
        if (!(victim.level() instanceof ServerLevel level)) return;
        if (victim.isDeadOrDying()) return;

        LivingEntity attacker = resolveAttacker(level, victim, event.getSource());
        if (attacker == null || attacker == victim) return;
        if (!PhayriosisCompat.isInfectionMob(attacker)) return;

        float dealt = Math.max(0.0F, event.getAmount());
        long now = level.getGameTime();
        AttackStreak streak = STREAKS.get(attacker.getUUID());

        // A new victim, an expired streak or a forgotten attacker starts counting from one.
        boolean restart = streak == null
                || !streak.targetId().equals(victim.getUUID())
                || now - streak.lastHitTick() > Config.HEMOLYSIS_STREAK_WINDOW.get();

        int hits = restart ? 1 : streak.hits() + 1;
        float totalDamage = (restart ? 0.0F : streak.totalDamage()) + dealt;
        float strongestHit = restart ? dealt : Math.max(streak.maxHit(), dealt);

        if (hits < Config.HEMOLYSIS_HITS_TO_TRIGGER.get()) {
            STREAKS.put(attacker.getUUID(), new AttackStreak(victim.getUUID(), hits, totalDamage, strongestHit, now));
            return;
        }

        // Threshold reached: the parasite only resorts to the toxin when chewing on the
        // target was not getting anywhere (damage below 400% of its own attack power).
        float attackPower = attackPower(attacker, strongestHit);
        float threshold = attackPower * Config.HEMOLYSIS_DAMAGE_RATIO_THRESHOLD.get().floatValue();
        if (totalDamage < threshold) {
            inflictHemolysis(attacker, victim);
        }

        STREAKS.remove(attacker.getUUID());
    }

    /**
     * Apply one layer of Hemolysis, or refresh the duration when the victim is already
     * capped out.
     */
    private static void inflictHemolysis(LivingEntity attacker, LivingEntity victim) {
        MobEffect effect = ModEffects.HEMOLYSIS.get();
        MobEffectInstance active = victim.getEffect(effect);
        int maxAmplifier = Math.max(0, Config.HEMOLYSIS_MAX_LAYERS.get() - 1);
        int amplifier = active == null ? 0 : Math.min(active.getAmplifier() + 1, maxAmplifier);

        victim.addEffect(new MobEffectInstance(effect, Config.HEMOLYSIS_DURATION.get(), amplifier,
                false, true, true), attacker);

        if (Config.DEBUG_LOGGING.get()) {
            Phayriosis_cataclysm.LOGGER.info("[Cataclysm] {} inflicted Hemolysis layer {} on {}",
                    attacker.getName().getString(), amplifier + 1, victim.getName().getString());
        }
    }

    /**
     * The PHA creature behind a damage source. Real attacks carry their attacker;
     * Phayriosis Reborn's scripted contact aura does not, so it can optionally be
     * traced back to the nearest infection mob targeting the victim.
     */
    private static LivingEntity resolveAttacker(ServerLevel level, LivingEntity victim, DamageSource source) {
        LivingEntity attacker = PhayriosisCompat.getAttacker(source);
        if (attacker != null) return attacker;

        if (Config.HEMOLYSIS_COUNT_SCRIPTED_DAMAGE.get() && PhayriosisCompat.isUnattributedAuraDamage(source)) {
            return PhayriosisCompat.findTargetingInfectionMob(level, victim);
        }

        return null;
    }

    /**
     * The attacker's attack power, used as the reference for the 400% check. Scripted
     * units sometimes deal damage that never shows up in the attribute, so the
     * strongest hit observed in the streak is used as a fallback.
     */
    private static float attackPower(LivingEntity attacker, float strongestHit) {
        float power = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (power <= 0.0F) {
            power = strongestHit;
        }
        return power;
    }

    /**
     * A dead attacker can never finish its streak.
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        STREAKS.remove(event.getEntity().getUUID());
    }

    /**
     * Periodically drop streaks belonging to entities that are gone (killed, despawned
     * or unloaded) so the map stays bounded.
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (STREAKS.isEmpty()) return;
        if (event.getServer().getTickCount() % CLEANUP_INTERVAL != 0) return;

        STREAKS.entrySet().removeIf(entry -> {
            for (ServerLevel level : event.getServer().getAllLevels()) {
                if (level.getEntity(entry.getKey()) != null) return false;
            }
            return true;
        });
    }

    /**
     * Consecutive hits one attacker landed on one target, together with the damage
     * that actually made it through the victim's mitigation.
     */
    private record AttackStreak(UUID targetId, int hits, float totalDamage, float maxHit, long lastHitTick) {
    }
}
