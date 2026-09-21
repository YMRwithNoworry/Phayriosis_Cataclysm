package alku.phayriosis_cataclysm.event;

import alku.phayriosis_cataclysm.Config;
import alku.phayriosis_cataclysm.Phayriosis_cataclysm;
import alku.phayriosis_cataclysm.core.InfectionStageManager;
import alku.phayriosis_cataclysm.core.PhayriosisCompat;
import alku.phayriosis_cataclysm.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Handles the 3D infection spread mechanics:
 * 
 * 1. Surface Spread: Infection blocks spread to adjacent vanilla blocks.
 *    Speed is boosted when underground roots meet the threshold.
 * 
 * 2. Deep Root Network: Tracks root block counts per chunk.
 *    When threshold is met, surface spread in that chunk is boosted.
 * 
 * 3. Water Contamination: When infection blocks are near water,
 *    water is converted to contaminated water.
 */
@Mod.EventBusSubscriber(modid = Phayriosis_cataclysm.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpreadHandler {

    // Track root counts per chunk for the deep root network boost
    private static final Map<Long, Integer> rootCountsPerChunk = new HashMap<>();
    private static int scanTimer = 0;
    private static final RandomSource RANDOM = RandomSource.create();

    /**
     * Periodic tick to handle infection spread and water contamination.
     */
    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel level)) return;

        // Only process overworld (but could be expanded)
        if (level.dimension() != Level.OVERWORLD) return;

        scanTimer++;
        if (scanTimer < 100) return; // Every 5 seconds
        scanTimer = 0;

        int stage = InfectionStageManager.getCurrentStage();
        if (stage < Config.SPREAD_MIN_STAGE.get()) return;

        // Scan loaded chunks for infection spread opportunities
        for (ChunkPos chunk : getLoadedChunks(level)) {
            processChunkSpread(level, chunk, stage);
        }
    }

    /**
     * Process infection spread within a chunk.
     */
    private static void processChunkSpread(ServerLevel level, ChunkPos chunk, int stage) {
        // Pick a random block in the chunk to check
        int x = chunk.getMinBlockX() + RANDOM.nextInt(16);
        int z = chunk.getMinBlockZ() + RANDOM.nextInt(16);
        int y = level.getMaxBuildHeight() - RANDOM.nextInt(level.getMaxBuildHeight() - level.getMinBuildHeight());

        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = level.getBlockState(pos);

        // If this is an infection block, try to spread
        if (PhayriosisCompat.isInfectionBlock(state.getBlock())) {
            trySpreadFromBlock(level, pos, stage);
            tryContaminateWater(level, pos, stage);
        }
    }

    /**
     * Try to spread infection from a block to its neighbors.
     */
    private static void trySpreadFromBlock(ServerLevel level, BlockPos pos, int stage) {
        // Higher stages spread faster
        float spreadChance = 0.1f * stage;
        if (RANDOM.nextFloat() > spreadChance) return;

        // Check if we're in a boosted chunk (deep root network)
        long chunkKey = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        int rootCount = rootCountsPerChunk.getOrDefault(chunkKey, 0);
        boolean boosted = rootCount >= Config.ROOTS_PER_CHUNK_FOR_BOOST.get();

        // A boosted chunk gets several independent neighbour attempts.
        int attempts = boosted
                ? Math.max(1, (int) Math.round(Config.SURFACE_SPREAD_BOOST.get()))
                : 1;

        for (int attempt = 0; attempt < attempts; attempt++) {
            BlockPos neighbor = pos.relative(Direction.getRandom(RANDOM));
            if (!level.isLoaded(neighbor)) continue;

            BlockState neighborState = level.getBlockState(neighbor);
            if (!isSpreadable(neighborState)) continue;

            BlockState replacement = getSpreadBlock(neighborState, level, neighbor);
            if (replacement != null) {
                level.setBlock(neighbor, replacement, 3);
                if (replacement.is(ModBlocks.DEEP_ROOT.get())) {
                    onRootPlaced(level, neighbor);
                }
                return;
            }
        }
    }

    /**
     * Contaminate water near infection blocks.
     */
    private static void tryContaminateWater(ServerLevel level, BlockPos pos, int stage) {
        float chance = 0.05f * stage;
        if (RANDOM.nextFloat() > chance) return;

        int radius = Config.WATER_CONTAMINATE_RADIUS.get();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > radius * radius) continue;

                    BlockPos check = pos.offset(dx, dy, dz);
                    if (!level.isLoaded(check)) continue;

                    BlockState state = level.getBlockState(check);
                    if (state.is(Blocks.WATER)) {
                        level.setBlock(check, ModBlocks.CONTAMINATED_WATER.get().defaultBlockState(), 3);
                        return; // one conversion per tick
                    }
                }
            }
        }
    }

    private static boolean isSpreadable(BlockState state) {
        if (state.isAir()) return false;
        return state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) ||
                state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE) ||
                state.is(Blocks.OAK_PLANKS) || state.is(Blocks.OAK_LOG) ||
                state.is(Blocks.DEEPSLATE) || state.is(Blocks.SAND) ||
                state.is(Blocks.GRAVEL) || state.is(Blocks.MOSS_BLOCK) ||
                state.is(Blocks.MYCELIUM) || state.is(Blocks.PODZOL);
    }

    private static BlockState getSpreadBlock(BlockState original, ServerLevel level, BlockPos pos) {
        // Underground blocks become deep root
        if (pos.getY() < level.getSeaLevel() - 5) {
            return ModBlocks.DEEP_ROOT.get().defaultBlockState();
        }
        // Surface blocks become flesh mass
        return ModBlocks.FLESH_MASS.get().defaultBlockState();
    }

    /**
     * Get a set of loaded chunk positions around players in this level.
     * This is deliberately a small, bounded sample so the five-second scan
     * never walks the entire world.
     */
    private static Iterable<ChunkPos> getLoadedChunks(ServerLevel level) {
        Set<ChunkPos> chunks = new HashSet<>();
        int radius = 4;

        for (ServerPlayer player : level.players()) {
            ChunkPos center = player.chunkPosition();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    chunks.add(new ChunkPos(center.x + dx, center.z + dz));
                }
            }
        }

        return chunks;
    }

    /**
     * Called to update root counts when blocks change.
     */
    public static void onRootPlaced(ServerLevel level, BlockPos pos) {
        long chunkKey = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        rootCountsPerChunk.merge(chunkKey, 1, Integer::sum);
    }

    public static void onRootRemoved(ServerLevel level, BlockPos pos) {
        long chunkKey = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        rootCountsPerChunk.computeIfPresent(chunkKey, (k, v) -> v > 1 ? v - 1 : null);
    }
}
