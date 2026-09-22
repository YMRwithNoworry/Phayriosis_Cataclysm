package alku.phayriosis_cataclysm.core;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Applying damage that nothing in the game is allowed to soften.
 *
 * <p>Damage types registered in the {@code bypasses_*} tags already skip armour,
 * protection enchantments, Resistance, shields and invulnerability, but vanilla still
 * drains absorption (golden hearts) first and still swallows a hit during the hurt
 * immunity window. This helper closes those two remaining loopholes, so callers only
 * have to pick the right damage type.</p>
 */
public final class TrueDamage {

    private TrueDamage() {
    }

    /**
     * Hurt {@code victim} for {@code amount} through {@code source}, ignoring absorption
     * and hurt immunity.
     */
    public static void apply(LivingEntity victim, float amount, DamageSource source) {
        if (amount <= 0.0F) return;

        // Absorption is a shield in front of the real health bar and `hurt` always drains
        // it first regardless of the damage type tags. Take it out of the equation so the
        // damage always eats real health, then put it back.
        float absorption = victim.getAbsorptionAmount();
        if (absorption > 0.0F) {
            victim.setAbsorptionAmount(0.0F);
        }

        // `bypasses_cooldown` already ignores hurt immunity, but a stale timer from an
        // unrelated hit would still eat this hit; clear it explicitly.
        victim.invulnerableTime = 0;

        victim.hurt(source, amount);

        if (absorption > 0.0F && victim.isAlive()) {
            victim.setAbsorptionAmount(absorption);
        }
    }

    /**
     * Send a burst of "damage indicator" particles, used by the true damage effects.
     */
    public static void damageParticles(LivingEntity victim) {
        if (victim.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                    victim.getX(), victim.getY() + victim.getBbHeight() * 0.5D, victim.getZ(),
                    3, victim.getBbWidth() * 0.5D, victim.getBbHeight() * 0.35D, victim.getBbWidth() * 0.5D, 0.0D);
        }
    }
}
