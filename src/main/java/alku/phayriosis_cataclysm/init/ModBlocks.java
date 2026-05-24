package alku.phayriosis_cataclysm.init;

import alku.phayriosis_cataclysm.Phayriosis_cataclysm;
import alku.phayriosis_cataclysm.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Phayriosis_cataclysm.MODID);

    // Infection Core - spawns where player dies to infection
    public static final RegistryObject<Block> INFECTION_CORE = BLOCKS.register("infection_core",
            () -> new InfectionCoreBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(5.0f, 10.0f)
                    .sound(SoundType.SLIME_BLOCK)
                    .lightLevel(state -> 8)
                    .noOcclusion()));

    // Spore Tower - artillery that shoots infection projectiles
    public static final RegistryObject<Block> SPORE_TOWER = BLOCKS.register("spore_tower",
            () -> new SporeTowerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(4.0f, 8.0f)
                    .sound(SoundType.FUNGUS)
                    .lightLevel(state -> 4)
                    .noOcclusion()));

    // Deep Root - underground infection spread block
    public static final RegistryObject<Block> DEEP_ROOT = BLOCKS.register("deep_root",
            () -> new DeepRootBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(2.0f, 4.0f)
                    .sound(SoundType.ROOTS)
                    .requiresCorrectToolForDrops()));

    // Contaminated Water - infection water
    public static final RegistryObject<Block> CONTAMINATED_WATER = BLOCKS.register("contaminated_water",
            () -> new ContaminatedWaterBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(100.0f)
                    .sound(SoundType.EMPTY)
                    .noOcclusion()
                    .replaceable()
                    .noLootTable()));

    // Flesh Mass - basic infection building block
    public static final RegistryObject<Block> FLESH_MASS = BLOCKS.register("flesh_mass",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(2.0f, 3.0f)
                    .sound(SoundType.SLIME_BLOCK)));

    // Hardened Carapace - reinforced infection block (resists siege)
    public static final RegistryObject<Block> HARDENED_CARAPACE = BLOCKS.register("hardened_carapace",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(50.0f, 1200.0f)
                    .sound(SoundType.BASALT)
                    .requiresCorrectToolForDrops()));

    // Tendril Cluster - decorative/functional infection block
    public static final RegistryObject<Block> TENDRIL_CLUSTER = BLOCKS.register("tendril_cluster",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(1.5f, 2.0f)
                    .sound(SoundType.WEEPING_VINES)
                    .noOcclusion()));
}
