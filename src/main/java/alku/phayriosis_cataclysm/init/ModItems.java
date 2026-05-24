package alku.phayriosis_cataclysm.init;

import alku.phayriosis_cataclysm.Phayriosis_cataclysm;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Phayriosis_cataclysm.MODID);

    // Block items
    public static final RegistryObject<Item> INFECTION_CORE_ITEM = ITEMS.register("infection_core",
            () -> new BlockItem(ModBlocks.INFECTION_CORE.get(), new Item.Properties()));

    public static final RegistryObject<Item> SPORE_TOWER_ITEM = ITEMS.register("spore_tower",
            () -> new BlockItem(ModBlocks.SPORE_TOWER.get(), new Item.Properties()));

    public static final RegistryObject<Item> DEEP_ROOT_ITEM = ITEMS.register("deep_root",
            () -> new BlockItem(ModBlocks.DEEP_ROOT.get(), new Item.Properties()));

    public static final RegistryObject<Item> FLESH_MASS_ITEM = ITEMS.register("flesh_mass",
            () -> new BlockItem(ModBlocks.FLESH_MASS.get(), new Item.Properties()));

    public static final RegistryObject<Item> HARDENED_CARAPACE_ITEM = ITEMS.register("hardened_carapace",
            () -> new BlockItem(ModBlocks.HARDENED_CARAPACE.get(), new Item.Properties()));

    public static final RegistryObject<Item> TENDRIL_CLUSTER_ITEM = ITEMS.register("tendril_cluster",
            () -> new BlockItem(ModBlocks.TENDRIL_CLUSTER.get(), new Item.Properties()));

    // Biomass Sample - dropped by infection mobs, used for crafting
    public static final RegistryObject<Item> BIOMASS_SAMPLE = ITEMS.register("biomass_sample",
            () -> new Item(new Item.Properties()));

    // Spore Sac - throwable item that creates infection
    public static final RegistryObject<Item> SPORE_SAC = ITEMS.register("spore_sac",
            () -> new Item(new Item.Properties().stacksTo(16)));

    // Neural Fragment - rare drop from high-tier infection mobs
    public static final RegistryObject<Item> NEURAL_FRAGMENT = ITEMS.register("neural_fragment",
            () -> new Item(new Item.Properties().stacksTo(64)));
}
