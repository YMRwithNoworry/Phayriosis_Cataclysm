package alku.phayriosis_cataclysm.core;

import alku.phayriosis_cataclysm.Config;
import alku.phayriosis_cataclysm.Phayriosis_cataclysm;
import alku.phayriosis_cataclysm.init.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * One meteor called down by a Dreadmind.
 *
 * <p>A strike is deliberately built out of vanilla falling blocks instead of a custom entity:
 * the meteor is a cluster of real blocks (magma, netherrack, obsidian ...) that spawns
 * {@code meteorFallHeight} blocks above the target's coordinates and rains down on it. The
 * blocks are ordinary {@link FallingBlockEntity}s with item drops disabled, so they render and
 * land like real falling blocks and stay on the ground afterwards, forming the meteor deposit,
 * while all the gameplay (damage, stun, crater) is handled here on the impact tick.</p>
 */
public final class MeteorStrike {

    /** Safety net: a strike that never lands (unloaded chunk, blocked sky) is cleaned up. */
    private static final int MAX_LIFETIME = 600;

    /** Ticks the blocks are given to settle into the crater after the impact. */
    private static final int SETTLE_TICKS = 40;

    private final ServerLevel level;
    @Nullable
    private final UUID casterId;
    private final Vec3 impact;
    private final long launchedAt;
    private final List<FallingBlockEntity> blocks = new ArrayList<>();

    private long impactedAt = -1L;
    private boolean finished;

    private MeteorStrike(ServerLevel level, @Nullable LivingEntity caster, Vec3 impact, long launchedAt) {
        this.level = level;
        this.casterId = caster == null ? null : caster.getUUID();
        this.impact = impact;
        this.launchedAt = launchedAt;
    }

    /**
     * Call a meteor down on {@code target}'s current coordinates.
     */
    public static MeteorStrike launch(ServerLevel level, @Nullable LivingEntity caster, LivingEntity target) {
        Vec3 impact = new Vec3(target.getX(), target.getY(), target.getZ());
        MeteorStrike strike = new MeteorStrike(level, caster, impact, level.getGameTime());
        strike.spawnFallingBlocks();
        return strike;
    }

    public boolean isFinished() {
        return finished;
    }

    /**
     * Advance the strike by one server tick; drops the finished ones.
     */
    public void tick() {
        if (finished) return;

        long now = level.getGameTime();

        if (impactedAt < 0L) {
            if (blocks.isEmpty() || hasLanded() || now - launchedAt > MAX_LIFETIME) {
                impact(now);
            }
            return;
        }

        if (now - impactedAt >= SETTLE_TICKS || now - launchedAt > MAX_LIFETIME + SETTLE_TICKS) {
            for (FallingBlockEntity block : blocks) {
                if (block.isAlive()) {
                    block.discard();
                }
            }
            finished = true;
        }
    }

    // ------------------------------------------------------------------ launch

    private void spawnFallingBlocks() {
        int count = Config.METEOR_BLOCK_COUNT.get();
        double spread = Config.METEOR_SPREAD.get();
        int height = Config.METEOR_FALL_HEIGHT.get();

        for (int i = 0; i < count; i++) {
            // The first block is the core: no offset, it marks the impact point.
            double dx = i == 0 ? 0.0D : (level.random.nextDouble() * 2.0D - 1.0D) * spread;
            double dz = i == 0 ? 0.0D : (level.random.nextDouble() * 2.0D - 1.0D) * spread;
            double dy = height + level.random.nextInt(8);

            BlockPos spawn = findAir(BlockPos.containing(impact.x + dx, impact.y + dy, impact.z + dz));
            BlockState state = randomMeteorBlock();
            if (spawn == null || state == null) continue;

            FallingBlockEntity falling = FallingBlockEntity.fall(level, spawn, state);
            falling.disableDrop();
            // Drift back towards the aim point so the cluster stays together on the way down.
            falling.setDeltaMovement(-dx / 80.0D, -0.1D, -dz / 80.0D);

            blocks.add(falling);
        }
    }

    /** First air block at or above {@code start}, so a meteor never carves the sky. */
    @Nullable
    private BlockPos findAir(BlockPos start) {
        BlockPos pos = start;
        for (int i = 0; i < 8; i++) {
            if (!level.isLoaded(pos)) return null;
            if (level.getBlockState(pos).isAir()) return pos;
            pos = pos.above();
        }
        return null;
    }

    /** A random block from {@code meteorBlocks}, never a liquid and never a block entity. */
    private BlockState randomMeteorBlock() {
        List<? extends String> ids = Config.METEOR_BLOCKS.get();
        if (ids.isEmpty()) return Blocks.MAGMA_BLOCK.defaultBlockState();

        for (int attempt = 0; attempt < 4; attempt++) {
            ResourceLocation location = ResourceLocation.tryParse(ids.get(level.random.nextInt(ids.size())));
            if (location == null) continue;

            Block block = ForgeRegistries.BLOCKS.getValue(location);
            if (block == null || block == Blocks.AIR) continue;

            BlockState state = block.defaultBlockState();
            if (!state.getFluidState().isEmpty() || state.hasBlockEntity()) continue;
            return state;
        }
        return Blocks.MAGMA_BLOCK.defaultBlockState();
    }

    private boolean hasLanded() {
        for (FallingBlockEntity block : blocks) {
            if (block.isRemoved() || block.onGround()) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ impact

    private void impact(long now) {
        impactedAt = now;

        carveCrater();

        LivingEntity caster = resolveCaster();
        double radius = Config.METEOR_IMPACT_RADIUS.get();
        AABB area = new AABB(impact.x - radius, impact.y - radius, impact.z - radius,
                impact.x + radius, impact.y + radius + 3.0D, impact.z + radius);

        float damage = Config.METEOR_DAMAGE.get().floatValue();
        int stunTicks = Config.METEOR_STUN_DURATION.get();

        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, area,
                entity -> isVictim(entity, caster))) {
            if (damage > 0.0F) {
                // The meteor is a single blow: stale hurt-immunity frames must not eat it.
                victim.invulnerableTime = 0;
                victim.hurt(ModDamageTypes.source(level, ModDamageTypes.METEOR, null, caster), damage);
            }
            victim.addEffect(new MobEffectInstance(ModEffects.STUN.get(), stunTicks, 0, false, true, true), caster);
        }

        BlockPos center = BlockPos.containing(impact);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, impact.x, impact.y + 0.8D, impact.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.EXPLOSION, impact.x, impact.y + 0.5D, impact.z,
                4, radius * 0.4D, 0.3D, radius * 0.4D, 0.0D);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, impact.x, impact.y + 0.6D, impact.z,
                60, radius * 0.7D, 0.8D, radius * 0.7D, 0.08D);
        level.sendParticles(ParticleTypes.FLAME, impact.x, impact.y + 0.4D, impact.z,
                40, radius * 0.6D, 0.5D, radius * 0.6D, 0.05D);
        level.sendParticles(ParticleTypes.LAVA, impact.x, impact.y + 0.3D, impact.z,
                20, radius * 0.5D, 0.4D, radius * 0.5D, 0.0D);

        level.playSound(null, center, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 6.0F, 0.6F);
        level.playSound(null, center, SoundEvents.LAVA_POP, SoundSource.HOSTILE, 3.0F, 0.5F);

        if (Config.DEBUG_LOGGING.get()) {
            Phayriosis_cataclysm.LOGGER.info("[Cataclysm] meteor impacted at {} {} {} ({} block(s))",
                    (int) impact.x, (int) impact.y, (int) impact.z, blocks.size());
        }
    }

    /** Every living creature of another faction inside the blast - including players. */
    private boolean isVictim(LivingEntity entity, @Nullable LivingEntity caster) {
        if (entity == null || !entity.isAlive() || entity.isSpectator()) return false;
        if (caster != null && entity == caster) return false;
        return !PhayriosisCompat.isPhaCreature(entity);
    }

    @Nullable
    private LivingEntity resolveCaster() {
        if (casterId == null) return null;
        Entity entity = level.getEntity(casterId);
        return entity instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    // ------------------------------------------------------------------ crater

    /**
     * Carve the shallow bowl and leave molten rock behind in it.
     */
    private void carveCrater() {
        int radius = Config.METEOR_CRATER_RADIUS.get();
        int depth = Config.METEOR_CRATER_DEPTH.get();
        if (radius <= 0 || depth <= 0) return;

        BlockPos center = BlockPos.containing(impact);
        double outer = radius + 0.5D;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > outer) continue;

                int columnDepth = (int) Math.round(depth * (1.0D - distance / outer));
                for (int dy = 0; dy < columnDepth; dy++) {
                    BlockPos pos = center.offset(dx, -dy, dz);
                    if (!level.isLoaded(pos)) continue;
                    if (!canCrater(level.getBlockState(pos), pos)) continue;
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }

                // Molten remains at the bottom of the bowl.
                if (columnDepth > 0 && level.random.nextFloat() < 0.6F) {
                    BlockPos floor = center.offset(dx, -columnDepth + 1, dz);
                    if (level.isLoaded(floor)) {
                        BlockState current = level.getBlockState(floor);
                        if (current.isAir() || current.canBeReplaced()) {
                            level.setBlock(floor, randomMeteorBlock(), 3);
                        }
                    }
                }
            }
        }
    }

    /** Whether the crater may remove this block: never the protected, unbreakable or fluid ones. */
    private boolean canCrater(BlockState state, BlockPos pos) {
        if (state.isAir()) return false;
        if (state.getDestroySpeed(level, pos) < 0.0F) return false;
        if (state.hasBlockEntity()) return false;
        if (!state.getFluidState().isEmpty()) return false;

        List<? extends String> blacklist = Config.METEOR_CRATER_BLACKLIST.get();
        if (blacklist.isEmpty()) return true;

        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        return id == null || !blacklist.contains(id.toString());
    }
}
