package alku.phayriosis_cataclysm.event;

import alku.phayriosis_cataclysm.Phayriosis_cataclysm;
import alku.phayriosis_cataclysm.core.PhayriosisCompat;
import alku.phayriosis_cataclysm.effect.StunMobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Enforcement side of the Stun effect ({@link StunMobEffect}).
 *
 * <p>The effect itself already zeroes the victim's movement attributes and switches a mob's AI
 * off; this handler closes the remaining gaps:</p>
 * <ol>
 *   <li>{@link LivingEvent.LivingTickEvent} pins the victim every tick (input, momentum,
 *       jumping) - the event fires at the very start of {@code LivingEntity#tick()}, so it also
 *       clears jump input before the entity gets to act on it,</li>
 *   <li>{@link LivingAttackEvent} cancels any attack a stunned creature still tries to land,
 *       be it a melee swing, an arrow or a modded ability that goes through {@code hurt()},</li>
 *   <li>{@link TickEvent.PlayerTickEvent} does the same for players at the end of their tick,
 *       which is the only point where the server can still take a jump or a knockback back,</li>
 *   <li>{@link LivingDeathEvent} hands the AI back to a mob that dies while stunned, so the
 *       {@code NoAI} flag can never outlive the effect.</li>
 * </ol>
 */
@Mod.EventBusSubscriber(modid = Phayriosis_cataclysm.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StunHandler {

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!StunMobEffect.isStunned(entity)) return;

        StunMobEffect.hold(entity);
    }

    /**
     * Players are the one entity the server does not steer itself, so their tick is pinned
     * again on the way out: whatever the jump input or a knockback added this tick is dropped
     * before it can carry them anywhere.
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide()) return;
        if (!StunMobEffect.isStunned(event.player)) return;

        StunMobEffect.hold(event.player);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity attacker = PhayriosisCompat.getAttacker(event.getSource());
        if (attacker != null && StunMobEffect.isStunned(attacker)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        StunMobEffect.releaseAi(event.getEntity());
    }
}
