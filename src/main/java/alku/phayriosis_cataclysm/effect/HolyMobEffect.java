package alku.phayriosis_cataclysm.effect;

import alku.phayriosis_cataclysm.Config;
import alku.phayriosis_cataclysm.init.ModEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Holy (神圣) - the blessing a Dreadmind bestows on nearby PHA units.
 *
 * <p>What it does, all configurable:</p>
 * <ol>
 *   <li>every attribute of the unit is multiplied by {@code holyAttributeMultiplier}
 *       (150% of the original value by default),</li>
 *   <li>creatures it attacks accumulate vulnerability stacks that raise the damage they take
 *       from PHA creatures,</li>
 *   <li>damage PHA creatures deal to it is rerouted as true damage.</li>
 * </ol>
 * The last two live in {@link alku.phayriosis_cataclysm.event.HolyCombatHandler}.
 *
 * <p>Visually the blessed unit is wrapped in golden light (a stream of totem sparks and the
 * occasional rising end-rod mote) and a beacon chime plays when the blessing lands.</p>
 */
public class HolyMobEffect extends MobEffect {

    /** Colour of the HUD icon / effect border (holy gold). */
    private static final int COLOR = 0xFFC83C;

    /** Attributes that are scaled by the blessing. */
    private static final List<Attribute> BOOSTED_ATTRIBUTES = List.of(
            Attributes.MAX_HEALTH,
            Attributes.ATTACK_DAMAGE,
            Attributes.ATTACK_SPEED,
            Attributes.ATTACK_KNOCKBACK,
            Attributes.ARMOR,
            Attributes.ARMOR_TOUGHNESS,
            Attributes.KNOCKBACK_RESISTANCE,
            Attributes.MOVEMENT_SPEED,
            Attributes.FOLLOW_RANGE);

    public HolyMobEffect() {
        super(MobEffectCategory.BENEFICIAL, COLOR);
    }

    /**
     * Scale the unit's attributes. The bonus comes from the config, so the modifiers are
     * written here instead of being declared in the constructor (their amounts would be
     * frozen otherwise). Re-applying removes the previous modifiers first, which makes a
     * refresh idempotent.
     */
    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        double bonus = Config.HOLY_ATTRIBUTE_MULTIPLIER.get() - 1.0D;
        float maxHealthBefore = entity.getMaxHealth();

        for (Attribute attribute : BOOSTED_ATTRIBUTES) {
            AttributeInstance instance = attributes.getInstance(attribute);
            if (instance == null) continue;

            UUID id = modifierId(attribute);
            instance.removeModifier(id);
            if (bonus > 0.0D) {
                instance.addTransientModifier(new AttributeModifier(id, "Holy blessing", bonus,
                        AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }

        // A unit that was at full health stays at full health after being empowered.
        float gained = entity.getMaxHealth() - maxHealthBefore;
        if (gained > 0.0F) {
            entity.setHealth(Math.min(entity.getMaxHealth(), entity.getHealth() + gained));
        }

        if (entity.level() instanceof ServerLevel level) {
            level.playSound(null, entity.blockPosition(), SoundEvents.BEACON_POWER_SELECT,
                    SoundSource.HOSTILE, 0.7F, 1.4F);
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        for (Attribute attribute : BOOSTED_ATTRIBUTES) {
            AttributeInstance instance = attributes.getInstance(attribute);
            if (instance != null) {
                instance.removeModifier(modifierId(attribute));
            }
        }
        super.removeAttributeModifiers(entity, attributes, amplifier);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return;
        if (!(entity.level() instanceof ServerLevel level)) return;

        // Golden aura: sparks around the body plus a mote drifting upwards.
        if (entity.tickCount % 2 == 0) {
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    entity.getX(), entity.getY() + entity.getBbHeight() * 0.6D, entity.getZ(),
                    2, entity.getBbWidth() * 0.6D, entity.getBbHeight() * 0.4D, entity.getBbWidth() * 0.6D, 0.01D);
        }
        if (entity.tickCount % 10 == 0) {
            level.sendParticles(ParticleTypes.END_ROD,
                    entity.getX(), entity.getY() + entity.getBbHeight() * 0.9D, entity.getZ(),
                    1, entity.getBbWidth() * 0.4D, 0.35D, entity.getBbWidth() * 0.4D, 0.0D);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // the aura is drawn every tick
    }

    /** Whether {@code entity} currently carries the blessing. */
    public static boolean isHoly(LivingEntity entity) {
        return entity != null && entity.hasEffect(ModEffects.HOLY.get());
    }

    /** Stable id per attribute so the blessing never stacks with itself. */
    private static UUID modifierId(Attribute attribute) {
        String key = "phayriosis_cataclysm:holy/" + attribute.getDescriptionId();
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }
}
