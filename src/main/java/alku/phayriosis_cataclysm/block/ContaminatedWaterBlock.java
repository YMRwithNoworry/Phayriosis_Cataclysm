package alku.phayriosis_cataclysm.block;

import alku.phayriosis_cataclysm.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Random;

/**
 * Contaminated Water - replaces water sources near infection.
 * Damages entities that touch it and spreads along water flow.
 */
public class ContaminatedWaterBlock extends Block {

    public ContaminatedWaterBlock(Properties props) {
        super(props);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.empty(); // entities can walk through
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Block.box(0, 0, 0, 16, 14, 16); // slightly lower than full block
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    /**
     * Entity touching contaminated water gets debuffs.
     */
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide()) return;
        if (entity instanceof LivingEntity living) {
            // Apply nausea and poison
            if (level.getRandom().nextFloat() < 0.3f) {
                living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0));
                living.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
            }
        }
    }

    /**
     * Spread contaminated water along water flow and convert adjacent water.
     */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if (random.nextFloat() > Config.WATER_SPREAD_SPEED.get()) return;

        // Try to spread to adjacent water blocks
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            if (!level.isLoaded(neighbor)) continue;

            BlockState neighborState = level.getBlockState(neighbor);
            if (neighborState.is(Blocks.WATER)) {
                level.setBlock(neighbor, this.defaultBlockState(), 3);
                return;
            }
        }
    }

    /**
     * Also spread on scheduled tick (triggered when infection blocks are near water).
     */
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        this.randomTick(state, level, pos, random);
    }
}
