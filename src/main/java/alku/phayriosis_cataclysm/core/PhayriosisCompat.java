package alku.phayriosis_cataclysm.core;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;

/**
 * Compatibility layer for detecting Phayriosis mod entities and blocks.
 * Uses registry name matching since we don't have compile-time dependency on Phayriosis.
 */
public class PhayriosisCompat {

    private static final String PHAYRIOSIS_PREFIX = "phayriosisreborn:";

    // Known Phayriosis entity patterns
    private static final Set<String> INFECTED_ENTITY_PATTERNS = Set.of(
            "converted_", "primal_", "carrier", "bulb", "nucleus",
            "seeder", "patroller", "phayrector", "nestwarden",
            "scent_", "victim", "bomb", "worm_"
    );

    // Farm animals that give bonus biomass
    private static final Set<String> FARM_ANIMALS = Set.of(
            "minecraft:cow", "minecraft:sheep", "minecraft:pig",
            "minecraft:chicken", "minecraft:goat", "minecraft:rabbit",
            "minecraft:mooshroom"
    );

    /**
     * Check if an entity is from the Phayriosis mod (infected/parasite entity).
     */
    public static boolean isPhayriosisEntity(Entity entity) {
        if (entity == null) return false;
        String registryName = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString();
        return registryName.startsWith(PHAYRIOSIS_PREFIX);
    }

    /**
     * Check if an entity type is from Phayriosis.
     */
    public static boolean isPhayriosisEntityType(EntityType<?> type) {
        if (type == null) return false;
        String registryName = ForgeRegistries.ENTITY_TYPES.getKey(type).toString();
        return registryName.startsWith(PHAYRIOSIS_PREFIX);
    }

    /**
     * Check if an entity is an infection mob (from Phayriosis or our addon).
     */
    public static boolean isInfectionMob(Entity entity) {
        if (entity == null) return false;
        if (isPhayriosisEntity(entity)) return true;
        String registryName = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString();
        return registryName.startsWith("phayriosis_cataclysm:");
    }

    /**
     * Check if an entity is a vanilla mob (not from any infection mod).
     */
    public static boolean isVanillaMob(Entity entity) {
        if (entity == null) return false;
        return !isInfectionMob(entity) && !(entity instanceof Player);
    }

    /**
     * Check if a vanilla entity is a farm animal (higher biomass value).
     */
    public static boolean isFarmAnimal(Entity entity) {
        if (entity == null) return false;
        String registryName = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString();
        return FARM_ANIMALS.contains(registryName);
    }

    /**
     * Check if an entity is a villager.
     */
    public static boolean isVillager(Entity entity) {
        return entity instanceof Villager;
    }

    /**
     * Check if a block is a Phayriosis infection block.
     */
    public static boolean isPhayriosisInfectionBlock(Block block) {
        if (block == null) return false;
        String name = ForgeRegistries.BLOCKS.getKey(block).toString();
        if (!name.startsWith(PHAYRIOSIS_PREFIX)) return false;
        String path = name.substring(PHAYRIOSIS_PREFIX.length()).toLowerCase();
        return path.contains("tumor") || path.contains("infestation") ||
                path.contains("converted_") || path.contains("phayrilesh") ||
                path.contains("phayrium") || path.contains("phayross") ||
                path.contains("phayrost") || path.contains("veinroots") ||
                path.contains("blood_blister") || path.contains("nest") ||
                path.contains("parasitic_webbing") || path.contains("rotting");
    }

    /**
     * Check if a block is any infection-related block (Phayriosis or ours).
     */
    public static boolean isInfectionBlock(Block block) {
        if (block == null) return false;
        if (isPhayriosisInfectionBlock(block)) return true;
        String name = ForgeRegistries.BLOCKS.getKey(block).toString();
        return name.startsWith("phayriosis_cataclysm:");
    }
}
