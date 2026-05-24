package alku.phayriosis_cataclysm.block;

import alku.phayriosis_cataclysm.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

/**
 * Deep Root - underground infection block that spreads downward through
 * stone and dirt, forming a "root network".
 * When enough roots accumulate in a chunk, surface infection spread is boosted.
 */
public class DeepRootBlock extends Block {

    private static final Random RANDOM = new Random();

    public DeepRootBlock(Properties props) {
        super(props);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        // Spread downward
        if (random.nextFloat() > 0.15f) return; // 15% chance per tick

        int maxDepth = Config.ROOT_SPREAD_DEPTH.get();
        if (pos.getY() < level.getMinBuildHeight() + maxDepth) return;

        // Try to spread to adjacent underground blocks
        BlockPos[] candidates = {
                pos.below(),
                pos.north(), pos.south(), pos.east(), pos.west(),
                pos.below().north(), pos.below().south(),
                pos.below().east(), pos.below().west()
        };

        for (BlockPos candidate : candidates) {
            if (!level.isLoaded(candidate)) continue;
            BlockState target = level.getBlockState(candidate);

            if (isConvertible(target)) {
                level.setBlock(candidate, this.defaultBlockState(), 3);
                return; // one spread per tick
            }
        }
    }

    private boolean isConvertible(BlockState state) {
        return state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE) ||
                state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) ||
                state.is(Blocks.COBBLESTONE) || state.is(Blocks.ANDESITE) ||
                state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE) ||
                state.is(Blocks.TUFF) || state.is(Blocks.CALCITE) ||
                state.is(Blocks.SAND) || state.is(Blocks.GRAVEL) ||
                state.is(Blocks.COARSE_DIRT) || state.is(Blocks.MUD);
    }
}
