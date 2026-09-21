package alku.phayriosis_cataclysm;

import com.mojang.logging.LogUtils;
import alku.phayriosis_cataclysm.entity.FusedHorrorEntity;
import alku.phayriosis_cataclysm.entity.LarvaEntity;
import alku.phayriosis_cataclysm.init.*;
import alku.phayriosis_cataclysm.network.PacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Phayriosis_cataclysm.MODID)
public class Phayriosis_cataclysm {

    public static final String MODID = "phayriosis_cataclysm";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Phayriosis_cataclysm() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register deferred registers. The addon intentionally adds no items.
        ModBlocks.BLOCKS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModEffects.MOB_EFFECTS.register(modEventBus);

        // Register mod event listeners
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerAttributes);

        // Register config
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC, "phayriosis_cataclysm-common.toml");

        // Register for game events (Forge event bus)
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PacketHandler.register();
            LOGGER.info("[Phayriosis Cataclysm] Network packets registered.");
        });
        LOGGER.info("[Phayriosis Cataclysm] Mod initialized. Awaiting infection...");
    }

    /**
     * Register entity attributes (health, damage, etc.) for custom entities.
     */
    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.LARVA.get(), LarvaEntity.createAttributes().build());
        event.put(ModEntities.FUSED_HORROR.get(), FusedHorrorEntity.createAttributes().build());
        LOGGER.info("[Phayriosis Cataclysm] Entity attributes registered.");
    }
}
