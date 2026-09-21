package alku.phayriosis_cataclysm.core;

import alku.phayriosis_cataclysm.Config;
import alku.phayriosis_cataclysm.Phayriosis_cataclysm;
import alku.phayriosis_cataclysm.network.PacketHandler;
import alku.phayriosis_cataclysm.network.StageSyncPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * Central manager for infection stage progression.
 * Handles biomass addition and broadcasts stage changes to clients.
 */
public class InfectionStageManager {

    private static int lastBroadcastStage = 1;
    private static double lastBroadcastBiomass = 0;

    /**
     * Add biomass and potentially advance the stage.
     * Should only be called on the server thread.
     */
    public static void addBiomass(ServerLevel level, double amount, String source) {
        if (level == null || level.isClientSide()) return;

        ServerLevel overworld = getOverworld(level);
        if (overworld == null) return;

        InfectionStageData data = InfectionStageData.get(overworld);
        int oldStage = data.getStage();
        data.addBiomass(amount);

        if (Config.DEBUG_LOGGING.get()) {
            Phayriosis_cataclysm.LOGGER.info("[Biomass] +{} from '{}'. Total: {} | Stage: {}->{}",
                    amount, source, data.getBiomass(), oldStage, data.getStage());
        }

        // Broadcast if stage changed or enough biomass changed
        int newStage = data.getStage();
        if (newStage != oldStage) {
            broadcastStageChange(overworld, newStage, data.getBiomass());
        } else if (Math.abs(data.getBiomass() - lastBroadcastBiomass) > 10.0) {
            syncToClients(overworld, data);
        }
    }

    /**
     * Broadcast a stage change event to all players and trigger stage effects.
     */
    public static void broadcastStageChange(ServerLevel level, int newStage, double biomass) {
        lastBroadcastStage = newStage;
        lastBroadcastBiomass = biomass;
        syncToClients(level, InfectionStageData.get(level));

        Phayriosis_cataclysm.LOGGER.info("[Phayriosis Cataclysm] Infection stage changed to {}! (Biomass: {})", newStage, biomass);
    }

    /**
     * Set the overworld stage directly. Mostly useful for testing and admin commands.
     */
    public static void setCurrentStage(int stage) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        ServerLevel overworld = server.overworld();
        if (overworld == null) return;

        InfectionStageData data = InfectionStageData.get(overworld);
        int oldStage = data.getStage();
        data.setStage(stage);
        if (data.getStage() != oldStage) {
            broadcastStageChange(overworld, data.getStage(), data.getBiomass());
        }
    }

    private static ServerLevel getOverworld(ServerLevel level) {
        MinecraftServer server = level.getServer();
        if (server == null) return null;
        return server.overworld();
    }

    /**
     * Sync current stage data to all connected players.
     */
    public static void syncToClients(ServerLevel level, InfectionStageData data) {
        MinecraftServer server = level.getServer();
        if (server == null) return;

        StageSyncPacket packet = new StageSyncPacket(data.getStage(), data.getBiomass());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            PacketHandler.sendToPlayer(packet, player);
        }
        lastBroadcastBiomass = data.getBiomass();
    }

    /**
     * Sync stage data to a single player (e.g., on login).
     */
    public static void syncToPlayer(ServerLevel level, ServerPlayer player) {
        ServerLevel overworld = getOverworld(level);
        if (overworld == null) return;
        InfectionStageData data = InfectionStageData.get(overworld);
        PacketHandler.sendToPlayer(new StageSyncPacket(data.getStage(), data.getBiomass()), player);
    }

    /**
     * Get the current stage for the overworld.
     */
    public static int getCurrentStage() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return 1;
        ServerLevel overworld = server.overworld();
        if (overworld == null) return 1;
        return InfectionStageData.get(overworld).getStage();
    }

    /**
     * Get current biomass for the overworld.
     */
    public static double getCurrentBiomass() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return 0;
        ServerLevel overworld = server.overworld();
        if (overworld == null) return 0;
        return InfectionStageData.get(overworld).getBiomass();
    }
}
