package alku.phayriosis_cataclysm.entity.ai;

import alku.phayriosis_cataclysm.Config;
import alku.phayriosis_cataclysm.core.ModDamageTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;

/**
 * Adaptive Carapace Handler - tracks consecutive hits of the same damage type
 * and applies damage reduction when the threshold is reached.
 * 
 * Forces players to vary their attack types (melee, ranged, fire, magic).
 * After taking N consecutive hits of the same type, the mob gains massive
 * resistance to that type for a duration.
 */
public class AdaptiveCarapaceHandler {

    private String lastDamageCategory = "";
    private int consecutiveHits = 0;
    private String resistantTo = "";
    private int resistanceTimer = 0;
    private boolean active = false;

    /**
     * Process incoming damage and potentially reduce it.
     * Returns the modified damage amount.
     */
    public float processDamage(LivingEntity entity, DamageSource source, float amount) {
        // Hemolysis is unresistable true damage: the carapace may neither soften it nor
        // treat its ticks as "hits" that would set up an adaptation.
        if (source.is(ModDamageTypes.HEMOLYSIS)) return amount;

        String category = categorizeDamage(source);

        if (resistantTo.equals(category) && active) {
            // Apply damage reduction
            amount *= (1.0f - Config.CARAPACE_DAMAGE_REDUCTION.get().floatValue());
            return amount;
        }

        // Track consecutive hits of same type
        if (category.equals(lastDamageCategory)) {
            consecutiveHits++;
        } else {
            consecutiveHits = 1;
            lastDamageCategory = category;
        }

        // Check if threshold reached
        if (consecutiveHits >= Config.CARAPACE_HITS_TO_TRIGGER.get() && !active) {
            activateCarapace(category, entity);
        }

        return amount;
    }

    /**
     * Tick down the carapace timer.
     */
    public void tick(LivingEntity entity) {
        if (active) {
            resistanceTimer--;
            if (resistanceTimer <= 0) {
                deactivateCarapace();
            }
        }
    }

    private void activateCarapace(String category, LivingEntity entity) {
        active = true;
        resistantTo = category;
        resistanceTimer = Config.CARAPACE_DURATION.get();
        consecutiveHits = 0;
        // Visual/sound feedback could be added here via event
    }

    private void deactivateCarapace() {
        active = false;
        resistantTo = "";
        resistanceTimer = 0;
    }

    /**
     * Categorize damage into broad types for carapace tracking.
     */
    private String categorizeDamage(DamageSource source) {
        if (source.is(DamageTypeTags.IS_PROJECTILE)) return "projectile";
        if (source.is(DamageTypeTags.IS_EXPLOSION)) return "explosion";
        if (source.is(DamageTypeTags.IS_FIRE)) return "fire";
        if (source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC)) return "magic";
        if (source.is(DamageTypeTags.IS_FREEZING)) return "freeze";

        // Melee is the default "contact" damage
        return "melee";
    }

    public boolean isActive() {
        return active;
    }

    public String getResistantTo() {
        return resistantTo;
    }

    public void saveToTag(CompoundTag tag) {
        tag.putString("CarapaceLastCategory", lastDamageCategory);
        tag.putInt("CarapaceHits", consecutiveHits);
        tag.putString("CarapaceResistantTo", resistantTo);
        tag.putInt("CarapaceTimer", resistanceTimer);
        tag.putBoolean("CarapaceActive", active);
    }

    public void loadFromTag(CompoundTag tag) {
        lastDamageCategory = tag.getString("CarapaceLastCategory");
        consecutiveHits = tag.getInt("CarapaceHits");
        resistantTo = tag.getString("CarapaceResistantTo");
        resistanceTimer = tag.getInt("CarapaceTimer");
        active = tag.getBoolean("CarapaceActive");
    }
}
