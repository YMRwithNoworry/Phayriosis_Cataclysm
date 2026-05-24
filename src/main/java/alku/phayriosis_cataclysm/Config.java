package alku.phayriosis_cataclysm;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // ========== Stage Progression ==========
    public static final ForgeConfigSpec.IntValue MAX_STAGE = BUILDER
            .comment("Maximum infection stage (1-10)")
            .defineInRange("maxStage", 10, 1, 10);

    public static final ForgeConfigSpec.IntValue TICK_INTERVAL = BUILDER
            .comment("Ticks between stage progression checks (20 = 1 second)")
            .defineInRange("tickInterval", 200, 20, 6000);

    public static final ForgeConfigSpec.DoubleValue PASSIVE_BIOMASS_PER_TICK = BUILDER
            .comment("Passive biomass gained per tick interval")
            .defineInRange("passiveBiomassPerTick", 0.05, 0.0, 10.0);

    // ========== Biomass Sources ==========
    public static final ForgeConfigSpec.DoubleValue BIOMASS_PER_VANILLA_KILL = BUILDER
            .comment("Biomass gained when an infection mob kills a vanilla mob")
            .defineInRange("biomassPerVanillaKill", 5.0, 0.0, 100.0);

    public static final ForgeConfigSpec.DoubleValue BIOMASS_PER_VILLAGER_KILL = BUILDER
            .comment("Biomass gained when an infection mob kills a villager (catalyst)")
            .defineInRange("biomassPerVillagerKill", 25.0, 0.0, 500.0);

    public static final ForgeConfigSpec.DoubleValue BIOMASS_PER_PLAYER_DEATH = BUILDER
            .comment("Biomass gained when a player is killed by infection")
            .defineInRange("biomassPerPlayerDeath", 100.0, 0.0, 1000.0);

    public static final ForgeConfigSpec.DoubleValue BIOMASS_PER_FARM_ANIMAL = BUILDER
            .comment("Biomass gained per farm animal (cow, sheep, pig, chicken) kill")
            .defineInRange("biomassPerFarmAnimal", 8.0, 0.0, 100.0);

    // ========== Stage Thresholds ==========
    public static final ForgeConfigSpec.DoubleValue STAGE_2_THRESHOLD = BUILDER
            .comment("Biomass needed to reach stage 2").defineInRange("stage2Threshold", 50.0, 0.0, 10000.0);
    public static final ForgeConfigSpec.DoubleValue STAGE_3_THRESHOLD = BUILDER
            .comment("Biomass needed to reach stage 3").defineInRange("stage3Threshold", 150.0, 0.0, 10000.0);
    public static final ForgeConfigSpec.DoubleValue STAGE_4_THRESHOLD = BUILDER
            .comment("Biomass needed to reach stage 4").defineInRange("stage4Threshold", 400.0, 0.0, 10000.0);
    public static final ForgeConfigSpec.DoubleValue STAGE_5_THRESHOLD = BUILDER
            .comment("Biomass needed to reach stage 5").defineInRange("stage5Threshold", 800.0, 0.0, 10000.0);
    public static final ForgeConfigSpec.DoubleValue STAGE_6_THRESHOLD = BUILDER
            .comment("Biomass needed to reach stage 6").defineInRange("stage6Threshold", 1500.0, 0.0, 10000.0);
    public static final ForgeConfigSpec.DoubleValue STAGE_7_THRESHOLD = BUILDER
            .comment("Biomass needed to reach stage 7").defineInRange("stage7Threshold", 3000.0, 0.0, 10000.0);
    public static final ForgeConfigSpec.DoubleValue STAGE_8_THRESHOLD = BUILDER
            .comment("Biomass needed to reach stage 8").defineInRange("stage8Threshold", 6000.0, 0.0, 10000.0);
    public static final ForgeConfigSpec.DoubleValue STAGE_9_THRESHOLD = BUILDER
            .comment("Biomass needed to reach stage 9").defineInRange("stage9Threshold", 12000.0, 0.0, 10000.0);
    public static final ForgeConfigSpec.DoubleValue STAGE_10_THRESHOLD = BUILDER
            .comment("Biomass needed to reach stage 10").defineInRange("stage10Threshold", 25000.0, 0.0, 10000.0);

    // ========== Spore Tower ==========
    public static final ForgeConfigSpec.IntValue SPORE_TOWER_RANGE = BUILDER
            .comment("Range in blocks for spore tower to target uninfected areas")
            .defineInRange("sporeTowerRange", 48, 8, 128);

    public static final ForgeConfigSpec.IntValue SPORE_TOWER_INTERVAL = BUILDER
            .comment("Ticks between spore tower shots").defineInRange("sporeTowerInterval", 200, 40, 1200);

    public static final ForgeConfigSpec.IntValue SPORE_INFECT_RADIUS = BUILDER
            .comment("Radius of infection when spore projectile lands").defineInRange("sporeInfectRadius", 2, 1, 5);

    public static final ForgeConfigSpec.IntValue SPORE_TOWER_MIN_STAGE = BUILDER
            .comment("Minimum stage for spore towers to spawn").defineInRange("sporeTowerMinStage", 3, 1, 10);

    // ========== Deep Root Network ==========
    public static final ForgeConfigSpec.IntValue ROOT_SPREAD_DEPTH = BUILDER
            .comment("How deep root blocks can spread downward").defineInRange("rootSpreadDepth", 32, 4, 64);

    public static final ForgeConfigSpec.IntValue ROOTS_PER_CHUNK_FOR_BOOST = BUILDER
            .comment("Root blocks needed in a chunk to boost surface spread").defineInRange("rootsPerChunkForBoost", 16, 1, 64);

    public static final ForgeConfigSpec.DoubleValue SURFACE_SPREAD_BOOST = BUILDER
            .comment("Multiplier for surface infection tick speed when root threshold met")
            .defineInRange("surfaceSpreadBoost", 4.0, 1.0, 20.0);

    // ========== Water Contamination ==========
    public static final ForgeConfigSpec.IntValue WATER_CONTAMINATE_RADIUS = BUILDER
            .comment("Radius of water contamination from infection blocks").defineInRange("waterContaminateRadius", 3, 1, 8);

    public static final ForgeConfigSpec.DoubleValue WATER_SPREAD_SPEED = BUILDER
            .comment("Chance per tick for contaminated water to spread along flow").defineInRange("waterSpreadSpeed", 0.1, 0.01, 1.0);

    // ========== Siege AI ==========
    public static final ForgeConfigSpec.DoubleValue SIEGE_BREAK_CHANCE = BUILDER
            .comment("Chance per tick to start breaking a block when pathfinding fails")
            .defineInRange("siegeBreakChance", 0.05, 0.001, 1.0);

    public static final ForgeConfigSpec.DoubleValue SIEGE_HARDNESS_CAP = BUILDER
            .comment("Max hardness a siege mob can break (obsidian=50, stone=1.5, planks=2.0)")
            .defineInRange("siegeHardnessCap", 3.0, 0.5, 50.0);

    public static final ForgeConfigSpec.IntValue SIEGE_MIN_STAGE = BUILDER
            .comment("Minimum stage for siege AI to activate").defineInRange("siegeMinStage", 4, 1, 10);

    // ========== Merge AI ==========
    public static final ForgeConfigSpec.DoubleValue MERGE_RADIUS = BUILDER
            .comment("Radius to search for merge partners").defineInRange("mergeRadius", 5.0, 1.0, 16.0);

    public static final ForgeConfigSpec.IntValue MERGE_CHARGE_TIME = BUILDER
            .comment("Ticks required for two entities to merge").defineInRange("mergeChargeTime", 60, 10, 200);

    public static final ForgeConfigSpec.IntValue MERGE_MIN_STAGE = BUILDER
            .comment("Minimum stage for merging to occur").defineInRange("mergeMinStage", 2, 1, 10);

    // ========== Call Swarm ==========
    public static final ForgeConfigSpec.IntValue SWARM_ALERT_RADIUS = BUILDER
            .comment("Radius in blocks for swarm call to alert nearby mobs").defineInRange("swarmAlertRadius", 32, 8, 128);

    public static final ForgeConfigSpec.IntValue SWARM_SPEED_DURATION = BUILDER
            .comment("Ticks the Speed II effect lasts during swarm call").defineInRange("swarmSpeedDuration", 200, 40, 600);

    public static final ForgeConfigSpec.IntValue SWARM_COOLDOWN = BUILDER
            .comment("Ticks between swarm calls").defineInRange("swarmCooldown", 600, 100, 3600);

    // ========== Adaptive Carapace ==========
    public static final ForgeConfigSpec.IntValue CARAPACE_HITS_TO_TRIGGER = BUILDER
            .comment("Consecutive same-type hits before carapace activates").defineInRange("carapaceHitsToTrigger", 3, 2, 10);

    public static final ForgeConfigSpec.DoubleValue CARAPACE_DAMAGE_REDUCTION = BUILDER
            .comment("Damage reduction multiplier when carapace is active (0.9 = 90% reduction)")
            .defineInRange("carapaceDamageReduction", 0.8, 0.1, 0.95);

    public static final ForgeConfigSpec.IntValue CARAPACE_DURATION = BUILDER
            .comment("Ticks the carapace stays active").defineInRange("carapaceDuration", 100, 20, 600);

    public static final ForgeConfigSpec.IntValue CARAPACE_MIN_STAGE = BUILDER
            .comment("Minimum stage for adaptive carapace").defineInRange("carapaceMinStage", 5, 1, 10);

    // ========== Infection Core (Death Penalty) ==========
    public static final ForgeConfigSpec.IntValue INFECTION_CORE_DURATION = BUILDER
            .comment("Ticks before infection core despawns (0 = permanent)")
            .defineInRange("infectionCoreDuration", 6000, 0, 24000);

    public static final ForgeConfigSpec.IntValue INFECTION_CORE_INFECT_RADIUS = BUILDER
            .comment("Radius the infection core converts blocks").defineInRange("infectionCoreInfectRadius", 5, 1, 15);

    // ========== Merging ==========
    public static final ForgeConfigSpec.DoubleValue MERGED_ENTITY_HP_MULTIPLIER = BUILDER
            .comment("HP multiplier for merged entities vs base").defineInRange("mergedEntityHpMultiplier", 2.5, 1.0, 10.0);

    public static final ForgeConfigSpec.DoubleValue MERGED_ENTITY_DAMAGE_MULTIPLIER = BUILDER
            .comment("Damage multiplier for merged entities vs base").defineInRange("mergedEntityDamageMultiplier", 2.0, 1.0, 10.0);

    // ========== Debug ==========
    public static final ForgeConfigSpec.BooleanValue DEBUG_LOGGING = BUILDER
            .comment("Enable debug logging").define("debugLogging", false);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    // Cached stage thresholds array
    public static double[] getStageThresholds() {
        return new double[]{
                0, // stage 1 starts at 0
                STAGE_2_THRESHOLD.get(),
                STAGE_3_THRESHOLD.get(),
                STAGE_4_THRESHOLD.get(),
                STAGE_5_THRESHOLD.get(),
                STAGE_6_THRESHOLD.get(),
                STAGE_7_THRESHOLD.get(),
                STAGE_8_THRESHOLD.get(),
                STAGE_9_THRESHOLD.get(),
                STAGE_10_THRESHOLD.get()
        };
    }
}
