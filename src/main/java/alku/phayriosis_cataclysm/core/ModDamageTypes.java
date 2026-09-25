package alku.phayriosis_cataclysm.core;

import alku.phayriosis_cataclysm.Phayriosis_cataclysm;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Damage types shipped by the addon's own datapack.
 *
 * <p>Each key has a matching {@code data/phayriosis_cataclysm/damage_type/&lt;id&gt;.json}
 * entry; the tags that decide which vanilla mitigation rules apply are declared under
 * {@code data/minecraft/tags/damage_type/}.</p>
 */
public final class ModDamageTypes {

    /**
     * Hemolysis damage. Registered in the {@code bypasses_armor},
     * {@code bypasses_enchantments}, {@code bypasses_effects}, {@code bypasses_resistance},
     * {@code bypasses_invulnerability}, {@code bypasses_cooldown} and (through
     * {@code bypasses_armor}) {@code bypasses_shield} tags, so nothing in the game's normal
     * damage pipeline is allowed to soften it.
     */
    public static final ResourceKey<DamageType> HEMOLYSIS = key("hemolysis");

    /**
     * Damage dealt by PHA creatures to a holy (empowered) target. Every hit against a holy
     * unit is rerouted through this type, so it keeps ignoring armour, enchantments,
     * Resistance, absorption and shields while still carrying the original attacker for
     * aggro and death messages. It shares the {@code infection} death message.
     */
    public static final ResourceKey<DamageType> HOLY_INFECTION = key("holy_infection");

    /**
     * Meteor impact damage. Unlike {@link #HEMOLYSIS} this type is <em>not</em> registered in
     * any {@code bypasses_*} tag: armour, enchantments and Resistance are meant to soften the
     * blow, and absorption works normally. It is attributed to the Dreadmind that called the
     * meteor down, so the kill is credited and the {@code meteor.player} death message is used.
     */
    public static final ResourceKey<DamageType> METEOR = key("meteor");

    private ModDamageTypes() {
    }

    private static ResourceKey<DamageType> key(String id) {
        return ResourceKey.create(Registries.DAMAGE_TYPE,
                ResourceLocation.fromNamespaceAndPath(Phayriosis_cataclysm.MODID, id));
    }

    /**
     * Build a damage source for a datapack damage type without any causing entity.
     * Effect damage of this kind is not attributed to the parasite that applied the
     * effect, which keeps the kill from being credited as a melee hit.
     */
    public static DamageSource source(Level level, ResourceKey<DamageType> type) {
        return source(level, type, null, null);
    }

    /**
     * Build a damage source for a datapack damage type, optionally attributed to the
     * entities that caused it.
     */
    public static DamageSource source(Level level, ResourceKey<DamageType> type,
                                      @Nullable Entity directEntity, @Nullable Entity causingEntity) {
        return new DamageSource(level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(type), directEntity, causingEntity);
    }
}
