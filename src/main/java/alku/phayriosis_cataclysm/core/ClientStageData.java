package alku.phayriosis_cataclysm.core;

/**
 * Client-side holder for infection stage data.
 * Updated via network packets from the server.
 */
public class ClientStageData {

    private static int stage = 1;
    private static double biomass = 0;

    public static void setStage(int stage) {
        ClientStageData.stage = stage;
    }

    public static void setBiomass(double biomass) {
        ClientStageData.biomass = biomass;
    }

    public static int getStage() {
        return stage;
    }

    public static double getBiomass() {
        return biomass;
    }
}
