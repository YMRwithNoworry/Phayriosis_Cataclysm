package alku.phayriosis_cataclysm.client;

import alku.phayriosis_cataclysm.Phayriosis_cataclysm;
import alku.phayriosis_cataclysm.init.ModEntities;
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
     * For now we use simple renderers; custom models can be added later.
     */
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Register a simple renderer for the spore projectile
        event.registerEntityRenderer(ModEntities.SPORE_PROJECTILE.get(),
                net.minecraft.client.renderer.entity.ThrownItemRenderer::new);

        // Register a simple mob renderer for Larva
        // Uses a zombie-like renderer as a placeholder
        event.registerEntityRenderer(ModEntities.LARVA.get(),
                context -> new net.minecraft.client.renderer.entity.ZombieRenderer(context));

        // Register a simple mob renderer for FusedHorror
        // Uses a ravager-like renderer as a placeholder
        event.registerEntityRenderer(ModEntities.FUSED_HORROR.get(),
                context -> new net.minecraft.client.renderer.entity.RavagerRenderer(context));
    }
}
