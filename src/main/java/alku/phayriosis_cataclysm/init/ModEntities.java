package alku.phayriosis_cataclysm.init;

import alku.phayriosis_cataclysm.Phayriosis_cataclysm;
import alku.phayriosis_cataclysm.entity.FusedHorrorEntity;
import alku.phayriosis_cataclysm.entity.LarvaEntity;
import alku.phayriosis_cataclysm.entity.SporeProjectileEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Phayriosis_cataclysm.MODID);

    // Spore projectile - fired by spore towers
    public static final RegistryObject<EntityType<SporeProjectileEntity>> SPORE_PROJECTILE =
            ENTITY_TYPES.register("spore_projectile",
                    () -> EntityType.Builder.<SporeProjectileEntity>of(SporeProjectileEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(64)
                            .updateInterval(5)
                            .build("spore_projectile"));

    // Larva - low-tier infection mob that can merge
    public static final RegistryObject<EntityType<LarvaEntity>> LARVA =
            ENTITY_TYPES.register("larva",
                    () -> EntityType.Builder.<LarvaEntity>of(LarvaEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 0.4f)
                            .clientTrackingRange(48)
                            .build("larva"));

    // Fused Horror - merged entity from two larvae
    public static final RegistryObject<EntityType<FusedHorrorEntity>> FUSED_HORROR =
            ENTITY_TYPES.register("fused_horror",
                    () -> EntityType.Builder.<FusedHorrorEntity>of(FusedHorrorEntity::new, MobCategory.MONSTER)
                            .sized(1.4f, 1.8f)
                            .clientTrackingRange(64)
                            .build("fused_horror"));
}
