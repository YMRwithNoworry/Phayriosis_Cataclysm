package alku.phayriosis_cataclysm.event;

import alku.phayriosis_cataclysm.Config;
import alku.phayriosis_cataclysm.Phayriosis_cataclysm;
import alku.phayriosis_cataclysm.core.InfectionStageData;
import alku.phayriosis_cataclysm.core.InfectionStageManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles per-tick stage progression and passive biomass gain.
 * Syncs stage data to players on login.
 */
@Mod.EventBusSubscriber(modid = Phayriosis_cataclysm.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StageTickHandler {

    private static int tickCounter = 0;

    /**
     * Server tick - passive biomass accumulation and stage checks.
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickCounter++;
        if (tickCounter < Config.TICK_INTERVAL.get()) return;
        tickCounter = 0;

        ServerLevel overworld = net.minecraftforge.server.ServerLifecycleHooks
                .getCurrentServer().overworld();
        if (overworld == null) return;

        // Passive biomass gain (simulates natural infection growth)
        double passive = Config.PASSIVE_BIOMASS_PER_TICK.get();
        if (passive > 0) {
            InfectionStageManager.addBiomass(overworld, passive, "passive");
        }

        // Check if stage changed and broadcast
        InfectionStageData data = InfectionStageData.get(overworld);
        if (data.pollStageChanged()) {
            InfectionStageManager.broadcastStageChange(overworld, data.getStage(), data.getBiomass());
        }
    }

    /**
     * Sync stage data to players when they join the server.
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerLevel overworld = player.server.overworld();
            InfectionStageManager.syncToPlayer(overworld, player);
        }
    }
}
