package alku.phayriosis_cataclysm;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

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

    public static final ForgeConfigSpec.IntValue SPREAD_MIN_STAGE = BUILDER
            .comment("Minimum stage for surface infection spread")
            .defineInRange("spreadMinStage", 2, 1, 10);

    public static final ForgeConfigSpec.IntValue REGEN_MIN_STAGE = BUILDER
            .comment("Minimum stage for infection mobs to regenerate")
            .defineInRange("regenMinStage", 5, 1, 10);

    public static final ForgeConfigSpec.IntValue FIRE_RESISTANCE_MIN_STAGE = BUILDER
            .comment("Minimum stage for infection mobs to resist fire")
            .defineInRange("fireResistanceMinStage", 8, 1, 10);

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
            .comment("Biomass needed to reach stage 2").defineInRange("stage2Threshold", 50.0, 0.0, 100000.0);
    public static final ForgeConfigSpec.DoubleValue STAGE_3_THRESHOLD = BUILDER
            .comment("Biomass needed to reach stage 3").defineInRange("stage3Threshold", 150.0, 0.0, 100000.0);
    public static final ForgeConfigSpec.DoubleValue STAGE_4_THRESHOLD = BUILDER
            .comment("Biomass needed to reach stage 4").defineInRange("stage4Threshold", 400.0, 0.0, 100000.0);
    public static final ForgeConfigSpec.DoubleValue STAGE_5_THRESHOLD = BUILDER
            .comment("Biomass needed to reach stage 5").defineInRange("stage5Threshold", 800.0, 0.0, 100000.0);
    public static final ForgeConfigSpec.DoubleValue STAGE_6_THRESHOLD = BUILDER
            .comment("Biomass needed to reach stage 6").defineInRange("stage6Threshold", 1500.0, 0.0, 100000.0);
    public static final ForgeConfigSpec.DoubleValue STAGE_7_THRESHOLD = BUILDER
            .comment("Biomass needed to reach stage 7").defineInRange("stage7Threshold", 3000.0, 0.0, 100000.0);
    public static final ForgeConfigSpec.DoubleValue STAGE_8_THRESHOLD = BUILDER
            .comment("Biomass needed to reach stage 8").defineInRange("stage8Threshold", 6000.0, 0.0, 100000.0);
    public static final ForgeConfigSpec.DoubleValue STAGE_9_THRESHOLD = BUILDER
            .comment("Biomass needed to reach stage 9").defineInRange("stage9Threshold", 12000.0, 0.0, 100000.0);
    public static final ForgeConfigSpec.DoubleValue STAGE_10_THRESHOLD = BUILDER
            .comment("Biomass needed to reach stage 10").defineInRange("stage10Threshold", 25000.0, 0.0, 100000.0);

    // ========== Spore Tower ==========
    public static final ForgeConfigSpec.BooleanValue ENABLE_SPORE_TOWERS = BUILDER
            .comment("Whether spore towers can fire at all")
            .define("enableSporeTowers", true);

    public static final ForgeConfigSpec.IntValue SPORE_TOWER_RANGE = BUILDER
            .comment("Range in blocks for spore tower to target uninfected areas")
            .defineInRange("sporeTowerRange", 48, 8, 128);

    public static final ForgeConfigSpec.IntValue SPORE_TOWER_INTERVAL = BUILDER
            .comment("Ticks between spore tower shots").defineInRange("sporeTowerInterval", 200, 40, 1200);

    public static final ForgeConfigSpec.IntValue SPORE_INFECT_RADIUS = BUILDER
            .comment("Radius of infection when spore projectile lands").defineInRange("sporeInfectRadius", 2, 1, 5);

    public static final ForgeConfigSpec.IntValue SPORE_TOWER_MIN_STAGE = BUILDER
            .comment("Minimum stage for spore towers to fire").defineInRange("sporeTowerMinStage", 3, 1, 10);

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
    public static final ForgeConfigSpec.BooleanValue ENABLE_SIEGE_AI = BUILDER
            .comment("Whether infection mobs can break blocks")
            .define("enableSiegeAI", true);

    public static final ForgeConfigSpec.DoubleValue SIEGE_BREAK_CHANCE = BUILDER
            .comment("Chance per tick to start breaking a block when pathfinding fails")
            .defineInRange("siegeBreakChance", 0.05, 0.001, 1.0);

    public static final ForgeConfigSpec.DoubleValue SIEGE_HARDNESS_CAP = BUILDER
            .comment("Max hardness a siege mob can break (obsidian=50, stone=1.5, planks=2.0)")
            .defineInRange("siegeHardnessCap", 3.0, 0.5, 50.0);

    public static final ForgeConfigSpec.IntValue SIEGE_MIN_STAGE = BUILDER
            .comment("Minimum stage for siege AI to activate").defineInRange("siegeMinStage", 4, 1, 10);

    // ========== Merge AI ==========
    public static final ForgeConfigSpec.BooleanValue ENABLE_MERGING = BUILDER
            .comment("Whether larvae can merge into Fused Horrors")
            .define("enableMerging", true);

    public static final ForgeConfigSpec.DoubleValue MERGE_RADIUS = BUILDER
            .comment("Radius to search for merge partners").defineInRange("mergeRadius", 5.0, 1.0, 16.0);

    public static final ForgeConfigSpec.IntValue MERGE_CHARGE_TIME = BUILDER
            .comment("Ticks required for two entities to merge").defineInRange("mergeChargeTime", 60, 10, 200);

    public static final ForgeConfigSpec.IntValue MERGE_MIN_STAGE = BUILDER
            .comment("Minimum stage for merging to occur").defineInRange("mergeMinStage", 2, 1, 10);

    // ========== Call Swarm ==========
    public static final ForgeConfigSpec.BooleanValue ENABLE_CALL_SWARM = BUILDER
            .comment("Whether infection mobs alert nearby parasites when they acquire a player target")
            .define("enableCallSwarm", true);

    public static final ForgeConfigSpec.IntValue SWARM_MIN_STAGE = BUILDER
            .comment("Minimum stage for Call Swarm to activate")
            .defineInRange("swarmMinStage", 2, 1, 10);

    public static final ForgeConfigSpec.IntValue SWARM_ALERT_RADIUS = BUILDER
            .comment("Radius in blocks for swarm call to alert nearby mobs").defineInRange("swarmAlertRadius", 32, 8, 128);

    public static final ForgeConfigSpec.IntValue SWARM_SPEED_DURATION = BUILDER
            .comment("Ticks the Speed II effect lasts during swarm call").defineInRange("swarmSpeedDuration", 200, 40, 600);

    public static final ForgeConfigSpec.IntValue SWARM_COOLDOWN = BUILDER
            .comment("Ticks between swarm calls").defineInRange("swarmCooldown", 600, 100, 3600);

    // ========== Targeting Range ==========
    public static final ForgeConfigSpec.DoubleValue PHA_FOLLOW_RANGE = BUILDER
            .comment("Targeting/sight range in blocks of every PHA creature (the FOLLOW_RANGE attribute).",
                    "Phayriosis Reborn never assigns FOLLOW_RANGE, so its units would otherwise run on the",
                    "vanilla default of 16 blocks; this value is applied to all infection mobs when they",
                    "enter the world (and re-applied on chunk load), so it also overrides the addon's own",
                    "mobs. Existing mobs keep their old range until they reload.")
            .defineInRange("phaFollowRange", 64.0, 8.0, 256.0);

    // ========== Adaptive Carapace ==========
    public static final ForgeConfigSpec.BooleanValue ENABLE_ADAPTIVE_CARAPACE = BUILDER
            .comment("Whether high-stage infection mobs can adapt to repeated damage types")
            .define("enableAdaptiveCarapace", true);

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

    // ========== Holy Empowerment (Dreadmind) ==========
    public static final ForgeConfigSpec.BooleanValue ENABLE_HOLY_EMPOWERMENT = BUILDER
            .comment("Whether Dreadminds periodically empower nearby PHA units with the Holy effect")
            .define("enableHolyEmpowerment", true);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HOLY_MIND_MOBS = BUILDER
            .comment("Entities that perform the holy empowerment, by registry name.",
                    "Defaults cover both PHA versions (the Dreadmind family).")
            .defineList("holyMindMobs", List.of(
                    "phayriosisreborn:dreadmind",
                    "phayriosis:primitive_dreadmind",
                    "phayriosis:morbid_dreadmind"), o -> o instanceof String);

    public static final ForgeConfigSpec.IntValue HOLY_PULSE_INTERVAL = BUILDER
            .comment("Ticks between two holy pulses of each Dreadmind (600 = 30 seconds)")
            .defineInRange("holyPulseInterval", 600, 20, 24000);

    public static final ForgeConfigSpec.IntValue HOLY_UNITS_PER_PULSE = BUILDER
            .comment("How many nearby PHA units a single pulse empowers")
            .defineInRange("holyUnitsPerPulse", 3, 1, 32);

    public static final ForgeConfigSpec.DoubleValue HOLY_SEARCH_RADIUS = BUILDER
            .comment("Radius in blocks a Dreadmind looks for units to empower")
            .defineInRange("holySearchRadius", 32.0, 4.0, 128.0);

    public static final ForgeConfigSpec.IntValue HOLY_DURATION = BUILDER
            .comment("Duration of the Holy effect in ticks (refreshed by every pulse)")
            .defineInRange("holyDuration", 900, 20, 72000);

    public static final ForgeConfigSpec.DoubleValue HOLY_ATTRIBUTE_MULTIPLIER = BUILDER
            .comment("All of an empowered unit's attributes are multiplied by this value (1.5 = 150%)")
            .defineInRange("holyAttributeMultiplier", 1.5, 1.0, 10.0);

    public static final ForgeConfigSpec.DoubleValue HOLY_VULNERABILITY_PER_HIT = BUILDER
            .comment("Each hit from a holy unit increases the damage its victim takes from PHA creatures",
                    "by this fraction (0.1 = +10% per hit)")
            .defineInRange("holyVulnerabilityPerHit", 0.1, 0.0, 5.0);

    public static final ForgeConfigSpec.IntValue HOLY_VULNERABILITY_MAX_STACKS = BUILDER
            .comment("Maximum vulnerability stacks a victim can accumulate (+200% at 20 stacks)")
            .defineInRange("holyVulnerabilityMaxStacks", 20, 1, 200);

    public static final ForgeConfigSpec.IntValue HOLY_VULNERABILITY_DURATION = BUILDER
            .comment("Ticks the vulnerability stacks survive after the last holy hit (0 = never expire)")
            .defineInRange("holyVulnerabilityDuration", 600, 0, 72000);

    // ========== Meteor Strike (Dreadmind) ==========
    public static final ForgeConfigSpec.BooleanValue ENABLE_METEOR_STRIKE = BUILDER
            .comment("Whether Dreadminds periodically call a meteor down on enemy elite units")
            .define("enableMeteorStrike", true);

    public static final ForgeConfigSpec.IntValue METEOR_COOLDOWN = BUILDER
            .comment("Ticks between two meteor strikes of the same Dreadmind (400 = 20 seconds)")
            .defineInRange("meteorCooldown", 400, 20, 24000);

    public static final ForgeConfigSpec.DoubleValue METEOR_SEARCH_RADIUS = BUILDER
            .comment("Radius in blocks a Dreadmind looks for an enemy elite unit to bombard")
            .defineInRange("meteorSearchRadius", 48.0, 4.0, 128.0);

    public static final ForgeConfigSpec.DoubleValue METEOR_ELITE_MIN_HEALTH = BUILDER
            .comment("A creature of another faction counts as an elite target when its maximum health",
                    "is at least this (40 = iron golem / warden class). Players are never picked as the",
                    "target, although they are still caught by the impact radius.")
            .defineInRange("meteorEliteMinHealth", 40.0, 1.0, 2048.0);

    public static final ForgeConfigSpec.DoubleValue METEOR_DAMAGE = BUILDER
            .comment("Damage the impact deals to every enemy inside the impact radius.",
                    "Armour, enchantments and Resistance still apply; PHA creatures are immune.")
            .defineInRange("meteorDamage", 50.0, 0.0, 10000.0);

    public static final ForgeConfigSpec.IntValue METEOR_STUN_DURATION = BUILDER
            .comment("Ticks the Stun effect lasts (1000 = 50 seconds)")
            .defineInRange("meteorStunDuration", 1000, 20, 72000);

    public static final ForgeConfigSpec.DoubleValue METEOR_IMPACT_RADIUS = BUILDER
            .comment("Radius in blocks of the impact: every non-PHA creature inside is hit and stunned")
            .defineInRange("meteorImpactRadius", 4.0, 1.0, 32.0);

    public static final ForgeConfigSpec.IntValue METEOR_FALL_HEIGHT = BUILDER
            .comment("How many blocks above the target the meteor blocks spawn")
            .defineInRange("meteorFallHeight", 40, 8, 200);

    public static final ForgeConfigSpec.IntValue METEOR_BLOCK_COUNT = BUILDER
            .comment("How many falling blocks one meteor is made of")
            .defineInRange("meteorBlockCount", 16, 1, 64);

    public static final ForgeConfigSpec.DoubleValue METEOR_SPREAD = BUILDER
            .comment("Horizontal spread in blocks of the falling blocks around the target")
            .defineInRange("meteorSpread", 3.0, 0.0, 16.0);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> METEOR_BLOCKS = BUILDER
            .comment("Blocks a meteor is made of, by registry name (lava-like solids look best).",
                    "Liquids and blocks with a block entity are skipped automatically.")
            .defineList("meteorBlocks", List.of(
                    "minecraft:magma_block",
                    "minecraft:netherrack",
                    "minecraft:blackstone",
                    "minecraft:basalt",
                    "minecraft:obsidian",
                    "minecraft:cobbled_deepslate",
                    "minecraft:tuff",
                    "minecraft:gilded_blackstone",
                    "minecraft:crying_obsidian",
                    "minecraft:nether_bricks"), o -> o instanceof String);

    public static final ForgeConfigSpec.IntValue METEOR_CRATER_RADIUS = BUILDER
            .comment("Radius in blocks of the crater the impact carves into the ground (0 = no crater)")
            .defineInRange("meteorCraterRadius", 2, 0, 8);

    public static final ForgeConfigSpec.IntValue METEOR_CRATER_DEPTH = BUILDER
            .comment("Depth in blocks of the crater (0 = no crater)")
            .defineInRange("meteorCraterDepth", 2, 0, 8);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> METEOR_CRATER_BLACKLIST = BUILDER
            .comment("Blocks a crater may never remove, by registry name. On top of this list the crater",
                    "always skips air, fluids, unbreakable blocks and anything with a block entity.")
            .defineList("meteorCraterBlacklist", List.of(
                    "minecraft:bedrock",
                    "minecraft:barrier",
                    "minecraft:end_portal_frame"), o -> o instanceof String);

    // ========== Hemolysis ==========
    public static final ForgeConfigSpec.BooleanValue ENABLE_HEMOLYSIS = BUILDER
            .comment("Whether PHA creatures can inflict Hemolysis (溶血) on whatever they keep attacking")
            .define("enableHemolysis", true);

    public static final ForgeConfigSpec.IntValue HEMOLYSIS_HITS_TO_TRIGGER = BUILDER
            .comment("How many consecutive hits a PHA creature must land on the same target to inflict Hemolysis")
            .defineInRange("hemolysisHitsToTrigger", 5, 1, 100);

    public static final ForgeConfigSpec.DoubleValue HEMOLYSIS_DAMAGE_RATIO_THRESHOLD = BUILDER
            .comment("Hemolysis is only inflicted when the damage those hits actually dealt is below this",
                    "multiple of the attacker's attack power (4.0 = 400%).",
                    "Targets that mitigate the hits (armour, Resistance, absorption, shields) are the ones",
                    "that end up bleeding; fragile targets simply take the damage and are spared.")
            .defineInRange("hemolysisDamageRatioThreshold", 4.0, 0.0, 100.0);

    public static final ForgeConfigSpec.IntValue HEMOLYSIS_DURATION = BUILDER
            .comment("Base duration of Hemolysis in ticks (20 ticks = 1 second)")
            .defineInRange("hemolysisDuration", 600, 20, 72000);

    public static final ForgeConfigSpec.DoubleValue HEMOLYSIS_DAMAGE_PER_SECOND = BUILDER
            .comment("True damage dealt every second, as a fraction of the victim's max health, per layer",
                    "(0.02 = 2% per layer). The damage is multiplied by the current number of layers.")
            .defineInRange("hemolysisDamagePerSecond", 0.02, 0.0, 1.0);

    public static final ForgeConfigSpec.IntValue HEMOLYSIS_MAX_LAYERS = BUILDER
            .comment("Maximum number of Hemolysis layers a single target can accumulate")
            .defineInRange("hemolysisMaxLayers", 10, 1, 127);

    public static final ForgeConfigSpec.IntValue HEMOLYSIS_STREAK_WINDOW = BUILDER
            .comment("Ticks allowed between two hits before the attack streak resets")
            .defineInRange("hemolysisStreakWindow", 200, 1, 6000);

    public static final ForgeConfigSpec.BooleanValue HEMOLYSIS_COUNT_SCRIPTED_DAMAGE = BUILDER
            .comment("Also treat Phayriosis Reborn's unattributed scripted damage (the instant contact",
                    "aura used by converted/advanced/perennial units) as an attack.",
                    "Disabled by default because that aura ticks far more often than a real swing does.")
            .define("hemolysisCountScriptedDamage", false);

    // ========== PHA Infighting ==========
    /**
     * How strictly PHA creatures are kept from fighting each other.
     */
    public enum InfightingScope {
        /** Vanilla behaviour: the PHA mobs may attack each other. */
        OFF,
        /** Creatures of different PHA mods never target or damage each other (default). */
        CROSS_MOD,
        /** No PHA creature ever attacks another PHA creature, not even its own kin. */
        ALL_PHA
    }

    public static final ForgeConfigSpec.EnumValue<InfightingScope> PHA_INFIGHTING = BUILDER
            .comment("Whether creatures of the two PHA mods (and this addon's own mobs) may attack each other.",
                    "OFF     = vanilla behaviour, they may fight.",
                    "CROSS_MOD = mobs from different PHA namespaces never target or damage each other (default).",
                    "ALL_PHA = no PHA creature ever attacks another PHA creature.",
                    "The rule cancels target acquisition, melee/projectile attacks and Phayriosis Reborn's",
                    "unattributed contact aura against protected creatures.")
            .defineEnum("phaInfighting", InfightingScope.CROSS_MOD);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> PHA_NAMESPACES = BUILDER
            .comment("Registry namespaces that count as PHA mods for the rule above.",
                    "phayriosisreborn = Phayriosis Reborn 0.3.x, phayriosis = Phayriosis 1.3.x,",
                    "phayriosis_cataclysm = this addon.")
            .defineList("phaNamespaces", List.of(
                    "phayriosisreborn",
                    "phayriosis",
                    "phayriosis_cataclysm"), o -> o instanceof String);

    // ========== PHA Damage Cap ==========
    public static final ForgeConfigSpec.BooleanValue ENABLE_DAMAGE_CAP = BUILDER
            .comment("Whether every PHA creature has a per-hit damage ceiling.")
            .define("enableDamageCap", true);

    public static final ForgeConfigSpec.DoubleValue PHA_DAMAGE_CAP = BUILDER
            .comment("Most health a single hit may take off a PHA creature (20 = 10 hearts).",
                    "The creatures covered are the ones registered under the phaNamespaces above, so",
                    "the cap covers both Phayriosis versions and this addon's own mobs, and nothing",
                    "else (players and vanilla mobs are untouched).",
                    "The ceiling bites on the incoming hit before armour absorbs anything and is",
                    "checked once more on the final amount that reaches the health bar, so armour",
                    "piercing, criticals, true damage, holy vulnerability and damage added by other",
                    "mods can never push one hit past it either.")
            .defineInRange("phaDamageCap", 20.0, 1.0, 10000.0);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> DAMAGE_CAP_BYPASS_DAMAGE_TYPES = BUILDER
            .comment("Damage types that ignore the cap above, by registry id",
                    "(e.g. minecraft:out_of_world, minecraft:generic_kill). The bare registry path",
                    "without a namespace ('out_of_world') is accepted as well; that is the path, not",
                    "the death-message id ('outOfWorld' for the same type).",
                    "Empty by default: nothing is allowed past the cap.")
            .defineList("damageCapBypassDamageTypes", List.<String>of(), o -> o instanceof String);

    // ========== Phayriosis Reborn Compatibility ==========
    public static final ForgeConfigSpec.BooleanValue PARASITE_AURA_DAMAGE_ENABLED = BUILDER
            .comment("Allows Phayriosis Reborn parasites to deal unattributed scripted contact damage",
                    "('wasconverted' and 'toomuchforeignblood' - the instant-damage aura reported by players).",
                    "Set to false to disable it.")
            .define("parasiteAuraDamageEnabled", true);

    // ========== Debug ==========
    public static final ForgeConfigSpec.BooleanValue DEBUG_LOGGING = BUILDER
            .comment("Enable debug logging").define("debugLogging", false);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    /**
     * Stage thresholds, normalised so a malformed config cannot make later stages
     * unreachable (or make stage progression run backwards).
     */
    public static double[] getStageThresholds() {
        double[] thresholds = {
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

        for (int i = 1; i < thresholds.length; i++) {
            if (thresholds[i] < thresholds[i - 1]) {
                thresholds[i] = thresholds[i - 1];
            }
        }

        return thresholds;
    }
}
