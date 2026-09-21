package alku.phayriosis_cataclysm.core;

import alku.phayriosis_cataclysm.Config;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Persistent world-level data tracking infection biomass and stage progression.
 * One instance per dimension; the OVERWORLD instance drives global progression.
 */
public class InfectionStageData extends SavedData {

    private static final String DATA_NAME = "phayriosis_cataclysm_stage";

    private double biomass = 0.0;
    private int stage = 1;
    private boolean dirty = false;

    public InfectionStageData() {
    }

    public static InfectionStageData load(CompoundTag tag) {
        InfectionStageData data = new InfectionStageData();
        data.biomass = Math.max(0.0, tag.getDouble("Biomass"));
        data.stage = data.calculateStage();
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putDouble("Biomass", biomass);
        tag.putInt("Stage", stage);
        return tag;
    }

    public double getBiomass() {
        return biomass;
    }

    public int getStage() {
        return stage;
    }

    public void addBiomass(double amount) {
        if (amount <= 0) return;
        this.biomass += amount;
        int newStage = calculateStage();
        if (newStage != this.stage) {
            this.stage = newStage;
            this.dirty = true;
        }
        setDirty();
    }

    public void setBiomass(double biomass) {
        this.biomass = biomass;
        int newStage = calculateStage();
        if (newStage != this.stage) {
            this.stage = newStage;
            this.dirty = true;
        }
        setDirty();
    }

    /**
     * Returns true if the stage just changed this tick.
     */
    public boolean pollStageChanged() {
        if (dirty) {
            dirty = false;
            return true;
        }
        return false;
    }

    private int calculateStage() {
        double[] thresholds = Config.getStageThresholds();
        int maxStage = Math.max(1, Math.min(Config.MAX_STAGE.get(), thresholds.length));
        int calculated = 1;

        for (int stageIndex = 1; stageIndex < maxStage; stageIndex++) {
            if (this.biomass >= thresholds[stageIndex]) {
                calculated = stageIndex + 1;
            } else {
                break;
            }
        }

        return calculated;
    }

    /**
     * Force the stored stage to a value. Mainly useful for reload/testing.
     */
    public void setStage(int stage) {
        int maxStage = Math.max(1, Math.min(Config.MAX_STAGE.get(), 10));
        int clamped = Math.max(1, Math.min(stage, maxStage));
        double[] thresholds = Config.getStageThresholds();
        this.biomass = thresholds[Math.min(clamped - 1, thresholds.length - 1)];

        if (clamped != this.stage) {
            this.stage = clamped;
            this.dirty = true;
        }
        setDirty();
    }

    /**
     * Get or create the infection stage data for the overworld.
     */
    public static InfectionStageData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                InfectionStageData::load,
                InfectionStageData::new,
                DATA_NAME
        );
    }
}
