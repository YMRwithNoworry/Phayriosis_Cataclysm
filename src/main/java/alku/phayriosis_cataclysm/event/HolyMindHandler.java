package alku.phayriosis_cataclysm.event;

import alku.phayriosis_cataclysm.Config;
import alku.phayriosis_cataclysm.Phayriosis_cataclysm;
import alku.phayriosis_cataclysm.core.PhayriosisCompat;
import alku.phayriosis_cataclysm.effect.HolyMobEffect;
import alku.phayriosis_cataclysm.init.ModEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dreadmind holy pulses.
 *
 * <p>Every {@code holyPulseInterval} ticks (30 seconds by default) each Dreadmind on the field
 * picks {@code holyUnitsPerPulse} (3) random PHA units within {@code holySearchRadius} and
 * blesses them with {@link HolyMobEffect}. Units that are not blessed yet are preferred, so a
 * pulse actually spreads the blessing instead of always re-buffing the same trio; already holy
 * units are still refreshed when there is nothing new to pick.</p>
 *
 * <p>The Dreadmind ids are configurable ({@code holyMindMobs}) and default to the Dreadmind
 * family of both PHA versions: {@code phayriosisreborn:dreadmind},
 * {@code phayriosis:primitive_dreadmind} and {@code phayriosis:morbid_dreadmind}.</p>
 */
@Mod.EventBusSubscriber(modid = Phayriosis_cataclysm.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HolyMindHandler {

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!Config.ENABLE_HOLY_EMPOWERMENT.get()) return;

        int interval = Config.HOLY_PULSE_INTERVAL.get();
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (level.getGameTime() % interval != 0) continue;
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof Mob mob && isHolyMind(mob) && mob.isAlive()) {
                    pulse(level, mob);
                }
            }
        }
    }

    /**
     * Bless up to {@code holyUnitsPerPulse} PHA units around {@code mind}. Public so the
     * behaviour can be exercised directly (tests, commands).
     *
     * @return the units that received the blessing
     */
    public static List<Mob> pulse(ServerLevel level, Mob mind) {
        double radius = Config.HOLY_SEARCH_RADIUS.get();
        int wanted = Config.HOLY_UNITS_PER_PULSE.get();

        List<Mob> candidates = level.getEntitiesOfClass(Mob.class,
                mind.getBoundingBox().inflate(radius),
                candidate -> candidate.isAlive() && PhayriosisCompat.isInfectionMob(candidate));

        // Prefer units that are not blessed yet, shuffle for randomness, then top up with
        // already blessed ones (their duration is simply refreshed).
        List<Mob> unblessed = new ArrayList<>();
        List<Mob> blessed = new ArrayList<>();
        for (Mob candidate : candidates) {
            (HolyMobEffect.isHoly(candidate) ? blessed : unblessed).add(candidate);
        }
        Collections.shuffle(unblessed);
        Collections.shuffle(blessed);
        unblessed.addAll(blessed);

        List<Mob> affected = new ArrayList<>();
        for (Mob unit : unblessed) {
            if (affected.size() >= wanted) break;

            MobEffectInstance active = unit.getEffect(ModEffects.HOLY.get());
            int amplifier = active == null ? 0 : active.getAmplifier();
            unit.addEffect(new MobEffectInstance(ModEffects.HOLY.get(), Config.HOLY_DURATION.get(),
                    amplifier, false, true, true), mind);
            affected.add(unit);

            if (Config.DEBUG_LOGGING.get()) {
                Phayriosis_cataclysm.LOGGER.info("[Cataclysm] {} empowered {}",
                        mind.getName().getString(), unit.getName().getString());
            }
        }
        return affected;
    }

    /** Whether this mob is one of the configured holy-mind entities. */
    public static boolean isHolyMind(Mob mob) {
        String name = PhayriosisCompat.getEntityId(mob);
        return name != null && Config.HOLY_MIND_MOBS.get().contains(name);
    }
}
