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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
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
 * <p>A PHA creature that attacks the same victim {@code hemolysisHitsToTrigger} (5) times inside
 * {@code hemolysisStreakWindow} (one minute) injects a haemolysin: the victim receives a
 * {@code hemolysisDuration} (5 second) Hemolysis effect. Every further completed streak adds one
 * layer on top of it, up to {@code hemolysisMaxLayers}.</p>
 *
 * <p>The count is kept on the attacks themselves, through {@link LivingAttackEvent}: a shield,
 * armour, Resistance, absorption or the victim's hurt-immunity frames may well swallow the blow,
 * the creature still gets credit for swinging. Attacks that another rule already cancelled - PHA
 * infighting, an attacker that is stunned - do not count. The streak restarts as soon as the
 * attacker switches targets or the five attacks no longer fit inside the window.</p>
 */
@Mod.EventBusSubscriber(modid = Phayriosis_cataclysm.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HemolysisEventHandler {

    /** Attack streaks per attacker, keyed by the attacker's UUID. */
    private static final Map<UUID, AttackStreak> STREAKS = new HashMap<>();

    /** How often (in ticks) streaks of entities that no longer exist are purged. */
    private static final int CLEANUP_INTERVAL = 600;

    /**
     * Count the attack itself and inject the toxin once the streak is complete. Runs last so
     * attacks other listeners cancelled are already marked as such.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.isCanceled()) return;
        if (!Config.ENABLE_HEMOLYSIS.get()) return;

        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;
        if (!(victim.level() instanceof ServerLevel level)) return;
        if (victim.isDeadOrDying()) return;

        LivingEntity attacker = resolveAttacker(level, victim, event.getSource());
        if (attacker == null || attacker == victim) return;
        if (!PhayriosisCompat.isInfectionMob(attacker)) return;

        long now = level.getGameTime();
        AttackStreak streak = STREAKS.get(attacker.getUUID());
        long window = Config.HEMOLYSIS_STREAK_WINDOW.get();

        // A new victim, a finished streak or a window that no longer holds the first attack all
        // start the count over.
        boolean restart = streak == null
                || !streak.targetId().equals(victim.getUUID())
                || now - streak.firstHitTick() > window;

        AttackStreak updated = restart
                ? new AttackStreak(victim.getUUID(), 1, now)
                : new AttackStreak(victim.getUUID(), streak.hits() + 1, streak.firstHitTick());

        if (updated.hits() < Config.HEMOLYSIS_HITS_TO_TRIGGER.get()) {
            STREAKS.put(attacker.getUUID(), updated);
            return;
        }

        inflictHemolysis(attacker, victim);
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
     * The PHA creature behind an attack. Real attacks carry their attacker; Phayriosis Reborn's
     * scripted contact aura does not, so it can optionally be traced back to the nearest
     * infection mob targeting the victim.
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
     * Attacks one attacker has landed on one target since the streak started.
     *
     * @param targetId    victim the streak belongs to
     * @param hits        attacks landed on that victim so far
     * @param firstHitTick game time of the first attack of the streak; the whole streak has to
     *                     fit into {@code hemolysisStreakWindow} from here
     */
    private record AttackStreak(UUID targetId, int hits, long firstHitTick) {
    }
}
