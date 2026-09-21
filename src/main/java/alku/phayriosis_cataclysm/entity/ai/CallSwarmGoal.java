package alku.phayriosis_cataclysm.entity.ai;

import alku.phayriosis_cataclysm.Config;
import alku.phayriosis_cataclysm.core.InfectionStageManager;
import alku.phayriosis_cataclysm.core.PhayriosisCompat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.List;

/**
 * Call Swarm Goal - when an infection mob detects a player, it emits
 * a screech that alerts all nearby infection mobs to target that player
 * and grants them Speed II for a short duration.
 *
 * Creates terrifying "horde rush" moments. This can be disabled with the
 * enableCallSwarm config and is gated by swarmMinStage.
 */
public class CallSwarmGoal extends Goal {

    private final Mob mob;
    private int cooldownTimer = 0;
    private boolean hasCalled = false;

    public CallSwarmGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!Config.ENABLE_CALL_SWARM.get()) return false;
        if (InfectionStageManager.getCurrentStage() < Config.SWARM_MIN_STAGE.get()) return false;

        if (cooldownTimer > 0) {
            cooldownTimer--;
            return false;
        }

        // Trigger when the mob has a target (player) and is freshly aggravated
        LivingEntity target = mob.getTarget();
        if (target == null || !(target instanceof Player)) {
            hasCalled = false;
            return false;
        }

        // Only call once per engagement
        return !hasCalled;
    }

    @Override
    public void start() {
        hasCalled = true;
        cooldownTimer = Config.SWARM_COOLDOWN.get();
        callSwarm();
    }

    @Override
    public boolean canContinueToUse() {
        return false; // One-shot action
    }

    @Override
    public void stop() {
        // hasCalled is reset naturally when the mob loses its player target.
    }

    public void resetCallFlag() {
        this.hasCalled = false;
    }

    private void callSwarm() {
        if (!(mob.level() instanceof ServerLevel serverLevel)) return;

        LivingEntity target = mob.getTarget();
        if (target == null) return;

        int radius = Config.SWARM_ALERT_RADIUS.get();
        int speedDuration = Config.SWARM_SPEED_DURATION.get();

        // Find all infection mobs in radius
        List<Mob> nearby = serverLevel.getEntitiesOfClass(
                Mob.class,
                mob.getBoundingBox().inflate(radius),
                entity -> entity != mob && PhayriosisCompat.isInfectionMob(entity) && entity.isAlive()
        );

        for (Mob nearbyMob : nearby) {
            // Lock their target to the player
            if (nearbyMob.getTarget() == null || !(nearbyMob.getTarget() instanceof Player)) {
                nearbyMob.setTarget(target);
            }

            // Grant Speed II
            nearbyMob.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED, speedDuration, 1, false, true));
        }

        // The screech sound is intentionally left to an optional client/event layer.
    }
}
