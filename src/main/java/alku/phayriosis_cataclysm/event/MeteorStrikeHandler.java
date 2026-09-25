package alku.phayriosis_cataclysm.event;

import alku.phayriosis_cataclysm.Config;
import alku.phayriosis_cataclysm.Phayriosis_cataclysm;
import alku.phayriosis_cataclysm.core.MeteorStrike;
import alku.phayriosis_cataclysm.core.PhayriosisCompat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Dreadmind meteor strikes.
 *
 * <p>Every {@code meteorCooldown} ticks (20 seconds by default) each Dreadmind on the field
 * picks one <em>enemy elite unit</em> within {@code meteorSearchRadius} and calls a meteor down
 * on its coordinates. The Dreadmind family is the same list the holy empowerment uses
 * ({@link HolyMindHandler#isHolyMind}), so every creature the pack treats as a Dreadmind gets
 * the ability.</p>
 *
 * <p>An enemy elite unit is a living creature that</p>
 * <ul>
 *   <li>does not belong to a PHA namespace ({@link PhayriosisCompat#isPhaCreature}) - the
 *       infection never bombards its own,</li>
 *   <li>is not a player (players are never picked as the <em>target</em>, but they are still
 *       caught by the impact radius),</li>
 *   <li>and has at least {@code meteorEliteMinHealth} (40) maximum health, i.e. the iron golem
 *       / warden class of "elite".</li>
 * </ul>
 *
 * <p>The Dreadmind's current target is preferred when it qualifies, which makes the meteor feel
 * like a reaction to the fight it is already in.</p>
 */
@Mod.EventBusSubscriber(modid = Phayriosis_cataclysm.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MeteorStrikeHandler {

    /** Every meteor that is still falling or still settling. */
    private static final List<MeteorStrike> ACTIVE = new ArrayList<>();

    /** Next game time (per Dreadmind) at which a meteor may be called. */
    private static final Map<UUID, Long> READY_AT = new HashMap<>();

    /** The Dreadmind scan - and with it the target search - runs once a second. */
    private static final int SCAN_INTERVAL = 20;

    /** Cooldown entries of Dreadminds that have not struck for this long are forgotten. */
    private static final long FORGET_AFTER = 12000L;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickStrikes();

        if (!Config.ENABLE_METEOR_STRIKE.get()) return;
        if (event.getServer().getTickCount() % SCAN_INTERVAL != 0) return;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            scan(level);
        }
    }

    /** Keep every meteor in the air moving; finished ones are dropped. */
    private static void tickStrikes() {
        if (ACTIVE.isEmpty()) return;

        for (Iterator<MeteorStrike> iterator = ACTIVE.iterator(); iterator.hasNext(); ) {
            MeteorStrike strike = iterator.next();
            strike.tick();
            if (strike.isFinished()) {
                iterator.remove();
            }
        }
    }

    private static void scan(ServerLevel level) {
        long now = level.getGameTime();
        long cooldown = Config.METEOR_COOLDOWN.get();

        READY_AT.entrySet().removeIf(entry -> entry.getValue() < now - FORGET_AFTER);

        // Collect the casters first: a meteor adds its falling blocks to the level, and adding
        // entities while walking level.getAllEntities() traps that iterator in an endless loop.
        List<Mob> minds = new ArrayList<>();
        for (Entity entity : level.getAllEntities()) {
            if (!(entity instanceof Mob mind) || !mind.isAlive()) continue;
            if (HolyMindHandler.isHolyMind(mind)) {
                minds.add(mind);
            }
        }

        for (Mob mind : minds) {
            Long readyAt = READY_AT.get(mind.getUUID());
            if (readyAt != null && now < readyAt) continue;

            LivingEntity target = findTarget(level, mind);
            if (target == null) continue;

            ACTIVE.add(MeteorStrike.launch(level, mind, target));
            READY_AT.put(mind.getUUID(), now + cooldown);

            if (Config.DEBUG_LOGGING.get()) {
                Phayriosis_cataclysm.LOGGER.info("[Cataclysm] {} called a meteor down on {}",
                        mind.getName().getString(), target.getName().getString());
            }
        }
    }

    /**
     * The elite a Dreadmind bombs: its current target when that one qualifies, otherwise the
     * closest qualifying creature. Public so the rule can be exercised directly (tests,
     * commands).
     */
    @Nullable
    public static LivingEntity findTarget(ServerLevel level, Mob mind) {
        LivingEntity current = mind.getTarget();
        if (isEliteTarget(current, mind)) return current;

        double radius = Config.METEOR_SEARCH_RADIUS.get();
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class,
                mind.getBoundingBox().inflate(radius), entity -> isEliteTarget(entity, mind));

        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : candidates) {
            double distance = candidate.distanceToSqr(mind);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        return best;
    }

    /**
     * An enemy elite unit: another faction, not a player, and tough enough to matter.
     */
    public static boolean isEliteTarget(@Nullable LivingEntity entity, LivingEntity mind) {
        if (entity == null || entity == mind) return false;
        if (!entity.isAlive() || entity.isSpectator()) return false;
        if (entity instanceof Player) return false;
        if (PhayriosisCompat.isPhaCreature(entity)) return false;

        return entity.getMaxHealth() >= Config.METEOR_ELITE_MIN_HEALTH.get();
    }

    /** How many meteors are currently in the air (used by the debug log and tests). */
    public static int activeStrikes() {
        return ACTIVE.size();
    }
}
