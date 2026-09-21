package alku.phayriosis_cataclysm.block;

import alku.phayriosis_cataclysm.Config;
import alku.phayriosis_cataclysm.core.InfectionStageManager;
import alku.phayriosis_cataclysm.entity.SporeProjectileEntity;
import alku.phayriosis_cataclysm.init.ModBlockEntities;
import alku.phayriosis_cataclysm.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

/**
 * Block entity for the Spore Tower.
 * Periodically scans for uninfected areas and launches spore projectiles.
 * Only active when the global infection stage is high enough.
 */
public class SporeTowerBlockEntity extends BlockEntity {

    private int tickCooldown;
    private final Random random = new Random();

    public SporeTowerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPORE_TOWER_BE.get(), pos, state);
        this.tickCooldown = random.nextInt(100); // stagger initial firing
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SporeTowerBlockEntity be) {
        if (!Config.ENABLE_SPORE_TOWERS.get()) return;

        // Only fire at sufficient stage
        if (InfectionStageManager.getCurrentStage() < Config.SPORE_TOWER_MIN_STAGE.get()) return;

        be.tickCooldown--;
        if (be.tickCooldown > 0) return;

        be.tickCooldown = Config.SPORE_TOWER_INTERVAL.get();
        be.fireSporeProjectile(level, pos);
    }

    private void fireSporeProjectile(Level level, BlockPos towerPos) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // Find a random uninfected target position within range
        BlockPos target = findUninfectedTarget(level, towerPos);
        if (target == null) return;

        // Calculate launch trajectory
        double dx = target.getX() + 0.5 - (towerPos.getX() + 0.5);
        double dy = target.getY() + 0.5 - (towerPos.getY() + 1.5); // launch from top
        double dz = target.getZ() + 0.5 - (towerPos.getZ() + 0.5);

        // Normalize and scale for a nice arc
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double speed = 0.8;
        double vx = dx / dist * speed;
        double vy = dy / dist * speed + 0.3; // arc upward
        double vz = dz / dist * speed;

        SporeProjectileEntity projectile = new SporeProjectileEntity(
                ModEntities.SPORE_PROJECTILE.get(), level);
        projectile.setPos(towerPos.getX() + 0.5, towerPos.getY() + 1.5, towerPos.getZ() + 0.5);
        projectile.shoot(vx, vy, vz);
        level.addFreshEntity(projectile);

        level.playSound(null, towerPos, SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 1.0f, 0.8f);
    }

    private BlockPos findUninfectedTarget(Level level, BlockPos center) {
        int range = Config.SPORE_TOWER_RANGE.get();

        for (int attempts = 0; attempts < 20; attempts++) {
            int dx = random.nextInt(range * 2) - range;
            int dz = random.nextInt(range * 2) - range;
            int dy = random.nextInt(20) - 10;

            BlockPos candidate = center.offset(dx, dy, dz);
            if (!level.isLoaded(candidate)) continue;

            // Check surface position
            BlockPos surface = findSurface(level, candidate);
            if (surface != null && !isInfected(level, surface)) {
                return surface;
            }
        }
        return null;
    }

    private BlockPos findSurface(Level level, BlockPos pos) {
        // Walk down to find the surface
        for (int y = Math.min(pos.getY() + 10, level.getMaxBuildHeight()); y > level.getMinBuildHeight(); y--) {
            BlockPos check = new BlockPos(pos.getX(), y, pos.getZ());
            if (!level.getBlockState(check).isAir() && level.getBlockState(check.above()).isAir()) {
                return check.above();
            }
        }
        return null;
    }

    private boolean isInfected(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockState below = level.getBlockState(pos.below());

        // Check if the block or its neighbors are infection blocks
        return isPhayriosisBlock(state) || isPhayriosisBlock(below) ||
                isOurInfectionBlock(state) || isOurInfectionBlock(below);
    }

    private boolean isPhayriosisBlock(BlockState state) {
        String name = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString();
        return name.startsWith("phayriosisreborn:");
    }

    private boolean isOurInfectionBlock(BlockState state) {
        String name = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString();
        return name.startsWith("phayriosis_cataclysm:");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("TickCooldown", tickCooldown);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        tickCooldown = tag.getInt("TickCooldown");
    }
}
