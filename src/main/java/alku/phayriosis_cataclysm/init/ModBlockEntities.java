package alku.phayriosis_cataclysm.init;

import alku.phayriosis_cataclysm.Phayriosis_cataclysm;
import alku.phayriosis_cataclysm.block.InfectionCoreBlockEntity;
import alku.phayriosis_cataclysm.block.SporeTowerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Phayriosis_cataclysm.MODID);

    public static final RegistryObject<BlockEntityType<InfectionCoreBlockEntity>> INFECTION_CORE_BE =
            BLOCK_ENTITIES.register("infection_core",
                    () -> BlockEntityType.Builder.of(InfectionCoreBlockEntity::new,
                            ModBlocks.INFECTION_CORE.get()).build(null));

    public static final RegistryObject<BlockEntityType<SporeTowerBlockEntity>> SPORE_TOWER_BE =
            BLOCK_ENTITIES.register("spore_tower",
                    () -> BlockEntityType.Builder.of(SporeTowerBlockEntity::new,
                            ModBlocks.SPORE_TOWER.get()).build(null));
}
