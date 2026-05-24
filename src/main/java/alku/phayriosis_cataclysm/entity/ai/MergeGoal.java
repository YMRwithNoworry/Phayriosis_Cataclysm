package alku.phayriosis_cataclysm.entity.ai;

import alku.phayriosis_cataclysm.Config;
import alku.phayriosis_cataclysm.core.InfectionStageManager;
import alku.phayriosis_cataclysm.entity.FusedHorrorEntity;
import alku.phayriosis_cataclysm.entity.LarvaEntity;
import alku.phayriosis_cataclysm.init.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;

/**
 * Merge Goal - when two Larvae are close together and not being attacked,
 * they stop moving and merge into a FusedHorror after a charge time.
 * 
 * This creates a snowball effect if players don't clear infection mobs quickly.
 */
public class MergeGoal extends Goal {

    private final LarvaEntity larva;
    private LarvaEntity mergePartner = null;
    private int mergeTimer = 0;
    private boolean isMerging = false;

    public MergeGoal(LarvaEntity larva) {
        this.larva = larva;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!larva.canMerge()) return false;
        if (InfectionStageManager.getCurrentStage() < Config.MERGE_MIN_STAGE.get()) return false;

        // Find a nearby larva that can also merge
        mergePartner = findMergePartner();
        return mergePartner != null;
    }

    @Override
    public boolean canContinueToUse() {
        return isMerging && mergePartner != null &&
                mergePartner.isAlive() && !mergePartner.isDeadOrDying() &&
                larva.isAlive() && !larva.isDeadOrDying() &&
                larva.distanceTo(mergePartner) < Config.MERGE_RADIUS.get() * 1.5 &&
                mergeTimer < Config.MERGE_CHARGE_TIME.get();
    }

    @Override
    public void start() {
        isMerging = true;
        mergeTimer = 0;
        larva.setMerging(true);
        larva.getNavigation().stop();

        if (mergePartner != null) {
            mergePartner.setMerging(true);
            mergePartner.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        isMerging = false;
        larva.setMerging(false);
        if (mergePartner != null && mergePartner.isAlive()) {
            mergePartner.setMerging(false);
            mergePartner.setMergeCooldown(100); // cooldown before this larva can try again
        }
        larva.setMergeCooldown(60);
    }

    @Override
    public void tick() {
        if (mergePartner == null || !mergePartner.isAlive()) {
            isMerging = false;
            return;
        }

        mergeTimer++;

        // Both larvae look at each other
        larva.getLookControl().setLookAt(mergePartner.getX(), mergePartner.getEyeY(), mergePartner.getZ());
        mergePartner.getLookControl().setLookAt(larva.getX(), larva.getEyeY(), larva.getZ());

        // Slowly move toward each other
        double dx = mergePartner.getX() - larva.getX();
        double dz = mergePartner.getZ() - larva.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist > 1.5) {
            larva.getNavigation().moveTo(mergePartner, 0.5);
        }

        // Merge complete!
        if (mergeTimer >= Config.MERGE_CHARGE_TIME.get()) {
            performMerge();
        }
    }

    private void performMerge() {
        if (!(larva.level() instanceof ServerLevel serverLevel)) return;

        // Calculate midpoint
        double midX = (larva.getX() + mergePartner.getX()) / 2.0;
        double midY = (larva.getY() + mergePartner.getY()) / 2.0;
        double midZ = (larva.getZ() + mergePartner.getZ()) / 2.0;

        // Remove both larvae
        mergePartner.discard();
        larva.discard();

        // Spawn FusedHorror at midpoint
        FusedHorrorEntity horror = ModEntities.FUSED_HORROR.get().create(serverLevel);
        if (horror != null) {
            horror.setPos(midX, midY, midZ);

            // Apply configured multipliers to attributes
            double hpMult = Config.MERGED_ENTITY_HP_MULTIPLIER.get();
            double dmgMult = Config.MERGED_ENTITY_DAMAGE_MULTIPLIER.get();

            var hpAttr = horror.getAttribute(Attributes.MAX_HEALTH);
            if (hpAttr != null) {
                hpAttr.setBaseValue(hpAttr.getBaseValue() * hpMult);
            }
            var dmgAttr = horror.getAttribute(Attributes.ATTACK_DAMAGE);
            if (dmgAttr != null) {
                dmgAttr.setBaseValue(dmgAttr.getBaseValue() * dmgMult);
            }
            horror.setHealth(horror.getMaxHealth());

            serverLevel.addFreshEntity(horror);
        }

        isMerging = false;
    }

    private LarvaEntity findMergePartner() {
        List<LarvaEntity> nearby = larva.level().getEntitiesOfClass(
                LarvaEntity.class,
                larva.getBoundingBox().inflate(Config.MERGE_RADIUS.get()),
                other -> other != larva && other.canMerge() && !other.isMerging()
        );

        if (nearby.isEmpty()) return null;

        // Return the closest valid partner
        LarvaEntity closest = null;
        double closestDist = Double.MAX_VALUE;
        for (LarvaEntity candidate : nearby) {
            double dist = larva.distanceToSqr(candidate);
            if (dist < closestDist) {
                closestDist = dist;
                closest = candidate;
            }
        }
        return closest;
    }
}
