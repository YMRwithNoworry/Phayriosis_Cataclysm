package alku.phayriosis_cataclysm.block;

import alku.phayriosis_cataclysm.Config;
import alku.phayriosis_cataclysm.event.SpreadHandler;
import alku.phayriosis_cataclysm.init.ModBlockEntities;
import alku.phayriosis_cataclysm.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

/**
 * Block entity for the Infection Core.
 * Periodically converts nearby blocks to infection blocks and
 * counts down to self-destruction.
 */
public class InfectionCoreBlockEntity extends BlockEntity {

    private int ticksExisted = 0;
    private int infectCooldown = 0;
    private final Random random = new Random();

    // Blocks that can be converted by the infection core
    private static final BlockState[] CONVERSION_TARGETS = {
            // Will convert to flesh_mass, deep_root, or tendril_cluster randomly
    };

    public InfectionCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INFECTION_CORE_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, InfectionCoreBlockEntity be) {
        be.ticksExisted++;
        be.infectCooldown--;

        // Despawn check
        int duration = Config.INFECTION_CORE_DURATION.get();
        if (duration > 0 && be.ticksExisted >= duration) {
            level.removeBlock(pos, false);
            return;
        }

        // Pulse infection outward
        if (be.infectCooldown <= 0) {
            be.infectCooldown = 40; // every 2 seconds
            be.pulseInfect(level, pos);
        }
    }

    private void pulseInfect(Level level, BlockPos center) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        int radius = Config.INFECTION_CORE_INFECT_RADIUS.get();

        // Pick a random block in radius to convert
        for (int attempts = 0; attempts < 3; attempts++) {
            int dx = random.nextInt(radius * 2 + 1) - radius;
            int dy = random.nextInt(radius * 2 + 1) - radius;
            int dz = random.nextInt(radius * 2 + 1) - radius;
            BlockPos target = center.offset(dx, dy, dz);

            if (!level.isLoaded(target)) continue;

            BlockState current = level.getBlockState(target);
            if (isConvertible(current)) {
                BlockState converted = getConversionResult(current, random);
                if (converted != null) {
                    level.setBlock(target, converted, 3);
                    if (converted.is(ModBlocks.DEEP_ROOT.get())) {
                        SpreadHandler.onRootPlaced(serverLevel, target);
                    }
                }
            }
        }
    }

    private boolean isConvertible(BlockState state) {
        if (state.isAir()) return false;
        // Can convert dirt, stone, grass, wood, etc.
        return state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) ||
                state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) ||
                state.is(Blocks.OAK_PLANKS) || state.is(Blocks.OAK_LOG) ||
                state.is(Blocks.DEEPSLATE) || state.is(Blocks.SAND) ||
                state.is(Blocks.GRAVEL) || state.is(Blocks.COARSE_DIRT) ||
                state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.MYCELIUM);
    }

    private BlockState getConversionResult(BlockState original, Random random) {
        // Return infection blocks from Phayriosis or our mod.
        // Since we can't reference Phayriosis blocks directly, we use our own.
        switch (random.nextInt(3)) {
            case 0: return ModBlocks.FLESH_MASS.get().defaultBlockState();
            case 1: return ModBlocks.DEEP_ROOT.get().defaultBlockState();
            case 2: return ModBlocks.TENDRIL_CLUSTER.get().defaultBlockState();
            default: return ModBlocks.FLESH_MASS.get().defaultBlockState();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("TicksExisted", ticksExisted);
        tag.putInt("InfectCooldown", infectCooldown);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ticksExisted = tag.getInt("TicksExisted");
        infectCooldown = tag.getInt("InfectCooldown");
    }
}
