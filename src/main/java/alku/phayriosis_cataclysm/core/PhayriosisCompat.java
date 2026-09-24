package alku.phayriosis_cataclysm.core;

import alku.phayriosis_cataclysm.Config;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;

/**
 * Compatibility layer for detecting Phayriosis mod entities and blocks.
 * Uses registry name matching since we don't have compile-time dependency on Phayriosis.
 *
 * <p>"PHA creatures" are the infection mobs of this modpack: everything registered
 * under the {@code phayriosisreborn} namespace (Phayriosis Reborn's converted,
 * advanced, elite, perennial and primordial units) plus this addon's own
 * {@code phayriosis_cataclysm} mobs. {@link #isInfectionMob(Entity)} is the single
 * definition every feature uses.</p>
 */
public class PhayriosisCompat {

    private static final String PHAYRIOSIS_PREFIX = "phayriosisreborn:";
    private static final String CATACLYSM_PREFIX = "phayriosis_cataclysm:";

    private static final ResourceKey<DamageType> PHAYRIOSIS_WAS_CONVERTED =
            ResourceKey.create(Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath("phayriosisreborn", "wasconverted"));
    private static final ResourceKey<DamageType> PHAYRIOSIS_FOREIGN_BLOOD =
            ResourceKey.create(Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath("phayriosisreborn", "toomuchforeignblood"));

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
        String registryName = getEntityRegistryName(entity);
        return registryName != null && registryName.startsWith(PHAYRIOSIS_PREFIX);
    }

    /**
     * Check if an entity type is from Phayriosis.
     */
    public static boolean isPhayriosisEntityType(EntityType<?> type) {
        String registryName = getEntityRegistryName(type);
        return registryName != null && registryName.startsWith(PHAYRIOSIS_PREFIX);
    }

    /**
     * Check if an entity is an infection mob (from Phayriosis or our addon).
     */
    public static boolean isInfectionMob(Entity entity) {
        String registryName = getEntityRegistryName(entity);
        return registryName != null
                && (registryName.startsWith(PHAYRIOSIS_PREFIX) || registryName.startsWith(CATACLYSM_PREFIX));
    }

    /**
     * Whether this entity belongs to one of the namespaces the pack treats as PHA mods
     * ({@link Config#PHA_NAMESPACES}: Phayriosis, Phayriosis Reborn and this addon by
     * default). This is the registry-id driven definition used by the rules that have to
     * cover <em>every</em> PHA creature rather than only the ones of this addon and
     * Phayriosis Reborn - the infighting rule and the damage cap.
     */
    public static boolean isPhaCreature(Entity entity) {
        String id = getEntityRegistryName(entity);
        if (id == null) return false;

        ResourceLocation location = ResourceLocation.tryParse(id);
        return location != null && Config.PHA_NAMESPACES.get().contains(location.getNamespace());
    }

    /**
     * Check if an entity is a vanilla mob (not from any infection mod).
     */
    public static boolean isVanillaMob(Entity entity) {
        if (entity == null) return false;
        return !isInfectionMob(entity) && !(entity instanceof Player);
    }

    /**
     * The living entity a damage source is credited to, if any. Prefers the causing
     * entity (the mob that swung) and falls back to the direct entity (an arrow). The
     * caller still has to check whether that entity is actually an infection mob.
     */
    public static LivingEntity getAttacker(DamageSource source) {
        if (source == null) return null;
        Entity causing = source.getEntity();
        if (causing instanceof LivingEntity living) return living;
        Entity direct = source.getDirectEntity();
        return direct instanceof LivingEntity living ? living : null;
    }

    /**
     * Best-effort attacker for Phayriosis Reborn's scripted attacks, which are
     * attributed to nobody at all: the closest living infection mob that is
     * currently targeting the victim (a nearby mob that is not targeting anybody is
     * accepted as a fallback).
     */
    public static Mob findTargetingInfectionMob(ServerLevel level, LivingEntity victim) {
        List<Mob> nearby = level.getEntitiesOfClass(
                Mob.class,
                victim.getBoundingBox().inflate(32.0),
                mob -> mob.isAlive() && isInfectionMob(mob)
        );

        Mob fallback = null;
        double fallbackDistance = Double.MAX_VALUE;

        for (Mob mob : nearby) {
            if (mob.getTarget() == victim) {
                return mob;
            }

            double distance = mob.distanceToSqr(victim);
            if (distance < fallbackDistance) {
                fallbackDistance = distance;
                fallback = mob;
            }
        }

        return fallback;
    }

    /**
     * Check if a vanilla entity is a farm animal (higher biomass value).
     */
    public static boolean isFarmAnimal(Entity entity) {
        String registryName = getEntityRegistryName(entity);
        return registryName != null && FARM_ANIMALS.contains(registryName);
    }

    /**
     * Check if an entity is a villager.
     */
    public static boolean isVillager(Entity entity) {
        return entity instanceof Villager;
    }

    /**
     * Registry id of an entity ("phayriosisreborn:dreadmind"), or {@code null} when the
     * entity is null or unregistered. Handy for config-driven entity lists.
     */
    public static String getEntityId(Entity entity) {
        return getEntityRegistryName(entity);
    }

    /**
     * Check if a block is a Phayriosis infection block.
     */
    public static boolean isPhayriosisInfectionBlock(Block block) {
        String name = getBlockRegistryName(block);
        if (name == null || !name.startsWith(PHAYRIOSIS_PREFIX)) return false;

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
        if (isPhayriosisInfectionBlock(block)) return true;
        String name = getBlockRegistryName(block);
        return name != null && name.startsWith(CATACLYSM_PREFIX);
    }

    /**
     * Phayriosis Reborn's scripted tick attacks use holder-only damage sources
     * (no causing/direct entity). {@code wasconverted} covers converted/basic/
     * advanced/elite units and {@code toomuchforeignblood} covers perennial units.
     */
    public static boolean isUnattributedAuraDamage(DamageSource source) {
        return source != null
                && (source.is(PHAYRIOSIS_WAS_CONVERTED) || source.is(PHAYRIOSIS_FOREIGN_BLOOD));
    }

    private static String getEntityRegistryName(Entity entity) {
        return entity == null ? null : getEntityRegistryName(entity.getType());
    }

    private static String getEntityRegistryName(EntityType<?> type) {
        if (type == null) return null;
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(type);
        return key == null ? null : key.toString();
    }

    private static String getBlockRegistryName(Block block) {
        if (block == null) return null;
        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
        return key == null ? null : key.toString();
    }
}
