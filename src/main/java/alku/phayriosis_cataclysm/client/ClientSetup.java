package alku.phayriosis_cataclysm.client;

import alku.phayriosis_cataclysm.Phayriosis_cataclysm;
import alku.phayriosis_cataclysm.init.ModEntities;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side setup for rendering entities and other client-only features.
 */
@Mod.EventBusSubscriber(modid = Phayriosis_cataclysm.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    /**
     * Register entity renderers.
     * Custom models can be added later; this keeps the mod loadable without
     * requiring GeckoLib models or placeholder vanilla renderer generics.
     */
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SPORE_PROJECTILE.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.LARVA.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.FUSED_HORROR.get(), NoopRenderer::new);
    }
}
