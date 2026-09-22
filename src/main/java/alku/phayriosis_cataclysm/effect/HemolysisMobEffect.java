package alku.phayriosis_cataclysm.effect;

import alku.phayriosis_cataclysm.Config;
import alku.phayriosis_cataclysm.core.ModDamageTypes;
import alku.phayriosis_cataclysm.core.TrueDamage;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Hemolysis (溶血) - the toxin PHA creatures inject into a target they cannot
 * simply chew through.
 *
 * <p>Every second the victim loses a percentage of its <em>maximum</em> health as
 * true damage; the percentage is multiplied by the current number of layers
 * (amplifier + 1). Layering is handled by
 * {@link alku.phayriosis_cataclysm.event.HemolysisEventHandler}.</p>
 *
 * <p>The damage is deliberately unavoidable: it is applied through
 * {@code phayriosis_cataclysm:hemolysis}, a damage type registered in every
 * "bypasses_*" tag (armour, enchantments, effects/Resistance, invulnerability,
 * shields, attack cooldown), the victim's absorption is taken out of the equation
 * and its invulnerability frames are cleared right before the hit. Nothing in
 * vanilla's damage pipeline can reduce, absorb or dodge it.</p>
 */
public class HemolysisMobEffect extends MobEffect {

    /** Ticks between two damage ticks; 20 ticks is exactly one second. */
    public static final int DAMAGE_INTERVAL = 20;

    /** Colour of the HUD icon / effect border (dark blood red). */
    private static final int COLOR = 0x8B0F1F;

    public HemolysisMobEffect() {
        super(MobEffectCategory.HARMFUL, COLOR);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return;
        if (entity.isDeadOrDying()) return;

        float amount = damagePerSecond(entity, amplifier);
        if (amount <= 0.0F) return;

        applyTrueDamage(entity, amount);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % DAMAGE_INTERVAL == 0;
    }

    /**
     * Health lost per second: {@code maxHealth * hemolysisDamagePerSecond * layers}.
     *
     * @param amplifier zero-based effect amplifier; layer count is {@code amplifier + 1}
     */
    public static float damagePerSecond(LivingEntity entity, int amplifier) {
        int layers = Math.max(1, amplifier + 1);
        return entity.getMaxHealth() * Config.HEMOLYSIS_DAMAGE_PER_SECOND.get().floatValue() * layers;
    }

    /**
     * Apply unresistable damage to the victim.
     */
    public static void applyTrueDamage(LivingEntity victim, float amount) {
        TrueDamage.apply(victim, amount, ModDamageTypes.source(victim.level(), ModDamageTypes.HEMOLYSIS));
        TrueDamage.damageParticles(victim);
    }
}
