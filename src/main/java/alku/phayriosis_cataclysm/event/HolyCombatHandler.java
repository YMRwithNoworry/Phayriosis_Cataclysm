package alku.phayriosis_cataclysm.event;

import alku.phayriosis_cataclysm.Config;
import alku.phayriosis_cataclysm.Phayriosis_cataclysm;
import alku.phayriosis_cataclysm.core.ModDamageTypes;
import alku.phayriosis_cataclysm.core.PhayriosisCompat;
import alku.phayriosis_cataclysm.core.TrueDamage;
import alku.phayriosis_cataclysm.effect.HolyMobEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The two combat rules of the Holy effect.
 *
 * <p><b>1. PHA damage against a holy unit becomes true damage.</b> The attack is intercepted
 * before anything can mitigate it (highest priority: raw damage, before shields, armour,
 * enchantments, Resistance and absorption), cancelled, and re-applied through
 * {@code phayriosis_cataclysm:holy_infection} - a damage type registered in every
 * {@code bypasses_*} tag - while keeping the original attacker so aggro, knockback and death
 * messages still work.</p>
 *
 * <p><b>2. A holy unit marks what it attacks.</b> Every hit adds one vulnerability stack
 * ({@code holyVulnerabilityPerHit}, +10% by default, capped at
 * {@code holyVulnerabilityMaxStacks} = 20 stacks / +200%) to the victim. Damage that PHA
 * creatures then deal to that victim is scaled up by its stacks. Stacks fade after
 * {@code holyVulnerabilityDuration} ticks without a new holy hit (0 disables the fade).</p>
 */
@Mod.EventBusSubscriber(modid = Phayriosis_cataclysm.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HolyCombatHandler {

    /** Vulnerability stacks per victim, keyed by victim UUID. */
    private static final Map<UUID, Vulnerability> VULNERABLE = new HashMap<>();

    /** How often stale vulnerability entries are purged (ticks). */
    private static final int CLEANUP_INTERVAL = 600;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!Config.ENABLE_HOLY_EMPOWERMENT.get()) return;

        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;
        if (victim.isDeadOrDying()) return;

        DamageSource source = event.getSource();
        boolean rerouted = source.is(ModDamageTypes.HOLY_INFECTION);

        // Rule 2: a holy attacker marks its victim. Skipped for the rerouted hit so one
        // attack counts once.
        if (!rerouted) {
            LivingEntity attacker = PhayriosisCompat.getAttacker(source);
            if (attacker != null && attacker != victim && HolyMobEffect.isHoly(attacker)) {
                addVulnerability(victim);
            }
        }

        // Rule 3: PHA damage against a holy victim is turned into true damage.
        if (rerouted || event.getAmount() <= 0.0F) return;
        if (!HolyMobEffect.isHoly(victim)) return;

        LivingEntity infectionAttacker = resolveInfectionAttacker(victim, source);
        if (infectionAttacker == null) return;

        event.setCanceled(true);

        Entity direct = source.getDirectEntity() != null ? source.getDirectEntity() : infectionAttacker;
        DamageSource trueSource = ModDamageTypes.source(victim.level(), ModDamageTypes.HOLY_INFECTION,
                direct, infectionAttacker);
        TrueDamage.apply(victim, event.getAmount(), trueSource);
        TrueDamage.damageParticles(victim);
    }

    /**
     * Rule 2 applied to the damage itself: scale up what PHA creatures deal to a marked
     * victim. Runs late so it sees the final amount, but one step before
     * {@link DamageCapHandler} (which sits at the very end of the chain) so the per-hit
     * ceiling of a PHA victim cannot be multiplied away.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!Config.ENABLE_HOLY_EMPOWERMENT.get()) return;

        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;

        int stacks = stacksOf(victim, victim.level().getGameTime());
        if (stacks <= 0) return;

        LivingEntity attacker = PhayriosisCompat.getAttacker(event.getSource());
        if (attacker == null || !PhayriosisCompat.isInfectionMob(attacker)) return;

        float multiplier = 1.0F + stacks * Config.HOLY_VULNERABILITY_PER_HIT.get().floatValue();
        if (multiplier > 1.0F) {
            event.setAmount(event.getAmount() * multiplier);
        }
    }

    /** A dead victim does not need its stacks any more. */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        VULNERABLE.remove(event.getEntity().getUUID());
    }

    /**
     * Purge stacks of entities that are gone or whose vulnerability timed out.
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (VULNERABLE.isEmpty()) return;
        if (event.getServer().getTickCount() % CLEANUP_INTERVAL != 0) return;

        long now = 0L;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            now = Math.max(now, level.getGameTime());
        }
        final long stamp = now;

        VULNERABLE.entrySet().removeIf(entry -> {
            if (isExpired(entry.getValue(), stamp)) return true;
            for (ServerLevel level : event.getServer().getAllLevels()) {
                if (level.getEntity(entry.getKey()) != null) return false;
            }
            return true;
        });
    }

    /** Current vulnerability stacks of {@code victim}, honouring the fade-out timer. */
    public static int stacksOf(LivingEntity victim, long now) {
        Vulnerability vulnerability = VULNERABLE.get(victim.getUUID());
        if (vulnerability == null || isExpired(vulnerability, now)) return 0;
        return vulnerability.stacks();
    }

    /** Clears the stacks of one victim (used by tests and by effect clean-up). */
    public static void clearVulnerability(LivingEntity victim) {
        VULNERABLE.remove(victim.getUUID());
    }

    private static void addVulnerability(LivingEntity victim) {
        long now = victim.level().getGameTime();
        Vulnerability previous = VULNERABLE.get(victim.getUUID());
        int stacks = previous == null || isExpired(previous, now)
                ? 1
                : Math.min(previous.stacks() + 1, Config.HOLY_VULNERABILITY_MAX_STACKS.get());
        VULNERABLE.put(victim.getUUID(), new Vulnerability(stacks, now));

        if (Config.DEBUG_LOGGING.get()) {
            Phayriosis_cataclysm.LOGGER.info("[Cataclysm] {} is now at {} holy vulnerability stacks",
                    victim.getName().getString(), stacks);
        }
    }

    private static boolean isExpired(Vulnerability vulnerability, long now) {
        int duration = Config.HOLY_VULNERABILITY_DURATION.get();
        return duration > 0 && now - vulnerability.lastHitTick() > duration;
    }

    /**
     * The PHA creature behind a damage source: a real attacker when the source carries one,
     * otherwise (for Phayriosis Reborn's scripted aura) the nearest infection mob that is
     * targeting the victim.
     */
    private static LivingEntity resolveInfectionAttacker(LivingEntity victim, DamageSource source) {
        LivingEntity attacker = PhayriosisCompat.getAttacker(source);
        if (attacker != null) return PhayriosisCompat.isInfectionMob(attacker) ? attacker : null;

        if (victim.level() instanceof ServerLevel level && PhayriosisCompat.isUnattributedAuraDamage(source)) {
            return PhayriosisCompat.findTargetingInfectionMob(level, victim);
        }
        return null;
    }

    /**
     * Vulnerability stacks one victim carries, plus when it was last refreshed.
     */
    private record Vulnerability(int stacks, long lastHitTick) {
    }
}
