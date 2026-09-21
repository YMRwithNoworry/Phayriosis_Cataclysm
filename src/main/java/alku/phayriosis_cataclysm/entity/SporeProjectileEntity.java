package alku.phayriosis_cataclysm.entity;

import alku.phayriosis_cataclysm.Config;
import alku.phayriosis_cataclysm.init.ModBlocks;
import alku.phayriosis_cataclysm.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

/**
 * Spore Projectile - fired by Spore Towers.
 * Converts a 3x3 area of blocks to infection blocks on landing.
 */
public class SporeProjectileEntity extends ThrowableProjectile {

    public SporeProjectileEntity(EntityType<? extends ThrowableProjectile> type, Level level) {
        super(type, level);
    }

    public SporeProjectileEntity(EntityType<? extends ThrowableProjectile> type,
                                  double x, double y, double z, Level level) {
        super(type, x, y, z, level);
    }

    public SporeProjectileEntity(EntityType<? extends ThrowableProjectile> type,
                                  LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    public void shoot(double vx, double vy, double vz) {
        this.setDeltaMovement(vx, vy, vz);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level().isClientSide()) return;

        BlockPos hitPos = result.getBlockPos();
        int radius = Config.SPORE_INFECT_RADIUS.get();

        // Convert blocks in radius
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > radius * radius) continue;

                    BlockPos target = hitPos.offset(dx, dy, dz);
                    if (!this.level().isLoaded(target)) continue;

                    BlockState current = this.level().getBlockState(target);
                    if (isConvertible(current)) {
                        BlockState converted = getRandomInfectionBlock();
                        this.level().setBlock(target, converted, 3);
                    } else if (current.is(Blocks.WATER)) {
                        this.level().setBlock(target,
                                ModBlocks.CONTAMINATED_WATER.get().defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        // Don't damage infection mobs, only vanilla ones
        if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity living) {
            String name = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES
                    .getKey(result.getEntity().getType()).toString();
            if (!name.startsWith("phayriosisreborn:") && !name.startsWith("phayriosis_cataclysm:")) {
                living.hurt(this.level().damageSources().magic(), 4.0f);
            }
        }
    }

    private boolean isConvertible(BlockState state) {
        if (state.isAir()) return false;
        return state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) ||
                state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) ||
                state.is(Blocks.OAK_PLANKS) || state.is(Blocks.OAK_LOG) ||
                state.is(Blocks.DEEPSLATE) || state.is(Blocks.SAND) ||
                state.is(Blocks.GRAVEL) || state.is(Blocks.MOSS_BLOCK);
    }

    private BlockState getRandomInfectionBlock() {
        switch (this.random.nextInt(4)) {
            case 0: return ModBlocks.FLESH_MASS.get().defaultBlockState();
            case 1: return ModBlocks.TENDRIL_CLUSTER.get().defaultBlockState();
            case 2: return ModBlocks.DEEP_ROOT.get().defaultBlockState();
            default: return ModBlocks.FLESH_MASS.get().defaultBlockState();
        }
    }

    @Override
    protected void defineSynchedData() {
        // No synced data needed
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
