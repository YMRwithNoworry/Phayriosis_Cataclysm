package alku.phayriosis_cataclysm.network;

import alku.phayriosis_cataclysm.core.ClientStageData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet sent from server to client to sync infection stage data.
 */
public class StageSyncPacket {

    private final int stage;
    private final double biomass;

    public StageSyncPacket(int stage, double biomass) {
        this.stage = stage;
        this.biomass = biomass;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(stage);
        buf.writeDouble(biomass);
    }

    public static StageSyncPacket decode(FriendlyByteBuf buf) {
        return new StageSyncPacket(buf.readInt(), buf.readDouble());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Update client-side stage data
            ClientStageData.setStage(stage);
            ClientStageData.setBiomass(biomass);
        });
        ctx.get().setPacketHandled(true);
    }
}
