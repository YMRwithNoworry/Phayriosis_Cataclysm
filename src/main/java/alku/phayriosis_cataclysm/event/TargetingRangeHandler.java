package alku.phayriosis_cataclysm.event;

import alku.phayriosis_cataclysm.Config;
import alku.phayriosis_cataclysm.Phayriosis_cataclysm;
import alku.phayriosis_cataclysm.core.PhayriosisCompat;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * Gives every PHA creature the configured targeting range.
 *
 * <p>Acquisition and retention of targets are driven by the {@code FOLLOW_RANGE}
 * attribute: {@code NearestAttackableTargetGoal} reads it, and
 * {@code HurtByTargetGoal#setAlertOthers} uses it to decide who gets alerted.
 * Phayriosis Reborn never assigns that attribute, so all of its converted,
 * advanced, elite, perennial and primordial units run on the vanilla default of
 * {@code 16} blocks - which makes the horde feel oblivious.</p>
 *
 * <p>Instead of relying on the parent mod, every infection mob is normalised to
 * {@code phaFollowRange} (64 blocks by default) the moment it joins a level, which
 * also covers mobs converted mid-game and mobs restored from a chunk. The value is
 * imposed with a {@link AttributeModifier.Operation#ADDITION transient modifier}:
 * the entity's saved data stays untouched and re-running the normalisation (join
 * events repeat on every chunk load) is idempotent. Change the config and reload
 * the affected chunks to re-apply it.</p>
 */
@Mod.EventBusSubscriber(modid = Phayriosis_cataclysm.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TargetingRangeHandler {

    /** Stable identity so an earlier application can be replaced instead of stacked. */
    private static final UUID MODIFIER_UUID = UUID.fromString("4c1d0f22-9e2b-4a6f-8f7d-1b0c7a5e9d31");
    private static final String MODIFIER_NAME = "PHA targeting range";

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!PhayriosisCompat.isInfectionMob(mob)) return;

        applyTargetingRange(mob);
    }

    /**
     * Force {@code mob}'s FOLLOW_RANGE to {@link Config#PHA_FOLLOW_RANGE}.
     *
     * <p>Safe to call repeatedly: a previous application is removed first, so the
     * mob never drifts and config changes simply overwrite the old value.</p>
     */
    public static void applyTargetingRange(Mob mob) {
        AttributeInstance instance = mob.getAttribute(Attributes.FOLLOW_RANGE);
        if (instance == null) return;

        AttributeModifier previous = instance.getModifier(MODIFIER_UUID);
        if (previous != null) {
            instance.removeModifier(previous);
        }

        double delta = Config.PHA_FOLLOW_RANGE.get() - instance.getValue();
        if (Math.abs(delta) < 1.0E-4D) return;

        instance.addTransientModifier(new AttributeModifier(MODIFIER_UUID, MODIFIER_NAME, delta,
                AttributeModifier.Operation.ADDITION));
    }
}
