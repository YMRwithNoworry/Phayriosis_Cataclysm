package alku.phayriosis_cataclysm.entity.ai;

import alku.phayriosis_cataclysm.Config;
import alku.phayriosis_cataclysm.core.InfectionStageManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;

/**
 * Siege Break Goal - when an infection mob detects a player but cannot
 * find a path, it will break obstructing blocks (wood, glass, etc.)
 * Only activates at sufficient infection stage.
 * 
 * Inspired by zombie siege mechanics but more intelligent:
 * - Targets soft blocks between mob and player
 * - Only activates when pathfinding fails
 * - Respects hardness cap (can't break obsidian/modded hardened blocks)
 */
public class SiegeBreakGoal extends Goal {

    private final Mob mob;
    private BlockPos targetBlock = null;
    private int breakProgress = 0;
    private int breakTime = 0;
    private int cooldown = 0;

    public SiegeBreakGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (InfectionStageManager.getCurrentStage() < Config.SIEGE_MIN_STAGE.get()) return false;
        if (cooldown > 0) {
            cooldown--;
            return false;
        }

        // Only activate if there's a nearby player we can't reach
        Player target = findNearbyPlayer();
        if (target == null) return false;

        // Check if we can't path to the player
        if (this.mob.getNavigation().isDone() || !hasLineOfSight(target)) {
            // Try to find a block to break
            targetBlock = findObstructingBlock(target);
            if (targetBlock != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (targetBlock == null) return false;
        if (this.mob.level().getBlockState(targetBlock).isAir()) return false;
        Player target = findNearbyPlayer();
        return target != null && this.mob.distanceTo(target) < 16.0;
    }

    @Override
    public void start() {
        this.breakProgress = 0;
        this.breakTime = calculateBreakTime();
        this.mob.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.targetBlock = null;
        this.breakProgress = 0;
        this.cooldown = 40; // short cooldown before trying again
    }

    @Override
    public void tick() {
        if (targetBlock == null) return;

        // Look at the target block
        this.mob.getLookControl().setLookAt(
                targetBlock.getX() + 0.5, targetBlock.getY() + 0.5, targetBlock.getZ() + 0.5);

        // Move toward it
        this.mob.getNavigation().moveTo(
                targetBlock.getX() + 0.5, targetBlock.getY(), targetBlock.getZ() + 0.5, 1.0);

        // Break the block over time
        double dist = this.mob.distanceToSqr(
                targetBlock.getX() + 0.5, targetBlock.getY() + 0.5, targetBlock.getZ() + 0.5);
        if (dist < 6.0) {
            breakProgress++;
            this.mob.level().destroyBlockProgress(mob.getId(), targetBlock, breakProgress);

            if (breakProgress >= breakTime) {
                this.mob.level().destroyBlock(targetBlock, true, mob);
                this.targetBlock = null;
            }
        }
    }

    private Player findNearbyPlayer() {
        return this.mob.level().getNearestPlayer(mob, 16.0);
    }

    private boolean hasLineOfSight(Player player) {
        return this.mob.getSensing().hasLineOfSight(player);
    }

    /**
     * Find a block between the mob and the player that can be broken.
     */
    private BlockPos findObstructingBlock(Player target) {
        Level level = this.mob.level();
        BlockPos mobPos = this.mob.blockPosition();
        BlockPos targetPos = target.blockPosition();

        // Scan blocks in a line between mob and target
        double dx = targetPos.getX() - mobPos.getX();
        double dy = targetPos.getY() - mobPos.getY();
        double dz = targetPos.getZ() - mobPos.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist > 16) return null;

        // Normalize direction
        dx /= dist;
        dy /= dist;
        dz /= dist;

        // Check blocks along the line
        for (int i = 1; i <= (int) dist; i++) {
            BlockPos check = mobPos.offset(
                    (int) (dx * i), (int) (dy * i), (int) (dz * i));
            BlockState state = level.getBlockState(check);

            if (!state.isAir() && state.getDestroySpeed(level, check) >= 0) {
                float hardness = state.getDestroySpeed(level, check);
                if (hardness >= 0 && hardness <= Config.SIEGE_HARDNESS_CAP.get()) {
                    return check;
                }
            }
        }

        // Also check surrounding blocks
        AABB searchBox = new AABB(
                Math.min(mobPos.getX(), targetPos.getX()) - 1,
                Math.min(mobPos.getY(), targetPos.getY()) - 1,
                Math.min(mobPos.getZ(), targetPos.getZ()) - 1,
                Math.max(mobPos.getX(), targetPos.getX()) + 2,
                Math.max(mobPos.getY(), targetPos.getY()) + 2,
                Math.max(mobPos.getZ(), targetPos.getZ()) + 2
        );

        BlockPos best = null;
        float bestDist = Float.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                new BlockPos((int) searchBox.minX, (int) searchBox.minY, (int) searchBox.minZ),
                new BlockPos((int) searchBox.maxX, (int) searchBox.maxY, (int) searchBox.maxZ))) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) continue;

            float hardness = state.getDestroySpeed(level, pos);
            if (hardness >= 0 && hardness <= Config.SIEGE_HARDNESS_CAP.get()) {
                float d = (float) pos.distSqr(targetPos);
                if (d < bestDist) {
                    bestDist = d;
                    best = pos.immutable();
                }
            }
        }

        return best;
    }

    private int calculateBreakTime() {
        if (targetBlock == null) return 20;
        BlockState state = this.mob.level().getBlockState(targetBlock);
        float hardness = state.getDestroySpeed(this.mob.level(), targetBlock);
        // Roughly match player break speed but slightly slower
        return Math.max(10, (int) (hardness * 15));
    }
}
