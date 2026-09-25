package alku.phayriosis_cataclysm.effect;

import alku.phayriosis_cataclysm.init.ModEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Stun (\u6655\u7729) - the aftermath of a Dreadmind's meteor.
 *
 * <p>The victim is completely locked down while the effect lasts: it cannot move, jump or
 * attack. Three layers make that hold even against modded AI:</p>
 * <ol>
 *   <li>{@link #NEUTRALISED} attributes (movement, flying, attack damage, attack knockback and
 *       jump strength) are multiplied by 0, so nothing can accelerate the victim any more,</li>
 *   <li>a mob's AI is switched off through {@code Mob#setNoAi} - the flag the victim had before
 *       is remembered in its persistent data, so it is restored exactly when the effect ends
 *       (and even survives a chunk unload in between),</li>
 *   <li>{@link alku.phayriosis_cataclysm.event.StunHandler} kills the leftovers every tick:
 *       input, residual momentum, jumps and any attack a stunned creature still tries to make.</li>
 * </ol>
 */
public class StunMobEffect extends MobEffect {

    /** Colour of the HUD icon / effect border (dizzy yellow). */
    private static final int COLOR = 0xFFD54A;

    /** Persistent-data keys that keep the AI switch reversible across saves and chunk loads. */
    private static final String AI_FROZEN_TAG = "PhayriosisCataclysmStunAiFrozen";
    private static final String AI_PREVIOUS_TAG = "PhayriosisCataclysmStunAiPrevious";

    /** Attributes that are zeroed while the victim is stunned. */
    private static final List<Attribute> NEUTRALISED = List.of(
            Attributes.MOVEMENT_SPEED,
            Attributes.FLYING_SPEED,
            Attributes.ATTACK_DAMAGE,
            Attributes.ATTACK_KNOCKBACK,
            Attributes.JUMP_STRENGTH);

    public StunMobEffect() {
        super(MobEffectCategory.HARMFUL, COLOR);
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        for (Attribute attribute : NEUTRALISED) {
            AttributeInstance instance = attributes.getInstance(attribute);
            if (instance == null) continue;

            UUID id = modifierId(attribute);
            instance.removeModifier(id);
            instance.addTransientModifier(new AttributeModifier(id, "Stunned", -1.0D,
                    AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        for (Attribute attribute : NEUTRALISED) {
            AttributeInstance instance = attributes.getInstance(attribute);
            if (instance != null) {
                instance.removeModifier(modifierId(attribute));
            }
        }
        releaseAi(entity);
        super.removeAttributeModifiers(entity, attributes, amplifier);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) return;

        hold(entity);

        if (entity.level() instanceof ServerLevel level && entity.tickCount % 5 == 0) {
            level.sendParticles(ParticleTypes.CRIT,
                    entity.getX(), entity.getY() + entity.getBbHeight() * 0.9D, entity.getZ(),
                    2, entity.getBbWidth() * 0.4D, 0.2D, entity.getBbWidth() * 0.4D, 0.0D);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // the victim has to be pinned every single tick
    }

    /** Whether this entity is currently stunned. */
    public static boolean isStunned(LivingEntity entity) {
        return entity != null && entity.hasEffect(ModEffects.STUN.get());
    }

    /**
     * Pin the entity in place for this tick: no AI, no input, no jumps and no leftover
     * momentum that could carry it away.
     */
    public static void hold(LivingEntity entity) {
        if (entity instanceof Mob mob) {
            freezeAi(mob);
            if (mob.getNavigation() != null) {
                mob.getNavigation().stop();
            }
        }

        entity.setJumping(false);
        entity.xxa = 0.0F;
        entity.yya = 0.0F;
        entity.zza = 0.0F;
        entity.setSprinting(false);

        Vec3 motion = entity.getDeltaMovement();
        if (motion.x != 0.0D || motion.z != 0.0D || motion.y > 0.0D) {
            // Falling is still allowed, everything else is not.
            entity.setDeltaMovement(0.0D, Math.min(motion.y, 0.0D), 0.0D);
            entity.hasImpulse = true;
        }
    }

    /**
     * Switch a mob's AI off, remembering the value it had before. The memory lives in the
     * mob's persistent data, so a stun that spans a chunk unload still ends cleanly.
     */
    private static void freezeAi(Mob mob) {
        CompoundTag data = mob.getPersistentData();
        if (!data.getBoolean(AI_FROZEN_TAG)) {
            data.putBoolean(AI_FROZEN_TAG, true);
            data.putBoolean(AI_PREVIOUS_TAG, mob.isNoAi());
        }

        if (!mob.isNoAi()) {
            mob.setNoAi(true);
            mob.setTarget(null);
        }
    }

    /** Give the mob its AI back; a no-op for everything the stun never touched. */
    public static void releaseAi(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        if (!data.getBoolean(AI_FROZEN_TAG)) return;

        boolean wasDisabled = data.getBoolean(AI_PREVIOUS_TAG);
        data.remove(AI_FROZEN_TAG);
        data.remove(AI_PREVIOUS_TAG);

        if (entity instanceof Mob mob && !wasDisabled && mob.isNoAi()) {
            mob.setNoAi(false);
        }
    }

    /** Stable id per attribute so the stun never stacks with itself. */
    private static UUID modifierId(Attribute attribute) {
        String key = "phayriosis_cataclysm:stun/" + attribute.getDescriptionId();
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }
}
