package alku.phayriosis_cataclysm.event;

import alku.phayriosis_cataclysm.Config;
import alku.phayriosis_cataclysm.Phayriosis_cataclysm;
import alku.phayriosis_cataclysm.core.PhayriosisCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Metamorphosis of low-tier PHA creatures.
 *
 * <p>Whenever a PHA creature whose maximum health is below
 * {@code metamorphosisMaxHealth} (40 by default - i.e. everything weaker than an abominable unit)
 * lands an attack, it has a {@code metamorphosisChance} (30%) chance to stop being what it is and
 * become an abominable unit instead. Humanoids become the Abominable Human
 * ({@code phayriosis:abominable_human}), quadrupeds the Abominable Bear, endermen the Abominable
 * Enderman and water creatures the Abominable Fish; every target id is configurable.</p>
 *
 * <p>The morph is a straight replacement on the spot: the new unit spawns at full health where
 * the old one stood, inherits the attack target it was fighting (or the creature it just bit) and
 * keeps its persistence flag, while the old body is discarded without a death message and without
 * dropping anything. An abominable unit is never a source, so the family cannot morph into
 * itself.</p>
 */
@Mod.EventBusSubscriber(modid = Phayriosis_cataclysm.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MetamorphosisHandler {

    /** Whole-name hints that mark a low-tier unit as a water creature. */
    private static final Set<String> AQUATIC_HINTS = Set.of(
            "fish", "squid", "dolphin", "turtle", "axolotl", "guardian", "kraken", "lisk", "slug");

    /** Whole-name hints for quadruped/beast low-tier units. */
    private static final Set<String> BEAST_HINTS = Set.of("bear", "hound", "boar", "mule", "donkey");

    /** Name parts (between underscores) that mark a beast; whole parts avoid matching piglin/cataclysm. */
    private static final Set<String> BEAST_PARTS = Set.of(
            "cow", "pig", "sheep", "goat", "horse", "llama", "camel", "wolf", "cat", "rabbit",
            "panda", "fox", "hoglin", "ravager", "sniffer", "bull", "mooshroom", "frog");

    /**
     * Every attack a low-tier PHA creature lands is a dice roll. Runs last so attacks that other
     * rules already cancelled (PHA infighting, a stunned attacker) never morph anything.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.isCanceled()) return;
        if (!Config.ENABLE_METAMORPHOSIS.get()) return;

        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;
        if (!(victim.level() instanceof ServerLevel level)) return;

        LivingEntity attacker = PhayriosisCompat.getAttacker(event.getSource());
        if (attacker == null || attacker == victim) return;
        if (!isLowTier(attacker)) return;

        float chance = Config.METAMORPHOSIS_CHANCE.get().floatValue();
        if (chance <= 0.0F || level.random.nextFloat() >= chance) return;

        morph(level, attacker, victim);
    }

    /**
     * Whether this creature is a low-tier PHA unit, i.e. one that can still metamorphose.
     */
    public static boolean isLowTier(LivingEntity entity) {
        if (entity == null || !entity.isAlive() || entity.isDeadOrDying()) return false;
        if (!PhayriosisCompat.isPhaCreature(entity)) return false;
        if (isAbominable(entity)) return false;

        return entity.getMaxHealth() < Config.METAMORPHOSIS_MAX_HEALTH.get().floatValue();
    }

    /** Abominable units are the goal state, never a source. */
    private static boolean isAbominable(Entity entity) {
        String id = PhayriosisCompat.getEntityId(entity);
        if (id == null) return false;

        return id.equals(Config.METAMORPHOSIS_HUMAN_RESULT.get())
                || id.equals(Config.METAMORPHOSIS_BEAST_RESULT.get())
                || id.equals(Config.METAMORPHOSIS_ENDERMAN_RESULT.get())
                || id.equals(Config.METAMORPHOSIS_AQUATIC_RESULT.get());
    }

    /**
     * The unit {@code source} turns into: the abominable family, picked from what the creature
     * used to be. Public so the mapping can be exercised directly (tests, commands).
     */
    @Nullable
    public static EntityType<?> resultType(LivingEntity source) {
        String id = PhayriosisCompat.getEntityId(source);
        String path = id == null ? "" : pathOf(id);

        String configured = Config.METAMORPHOSIS_HUMAN_RESULT.get();
        if (path.contains("enderman")) {
            configured = Config.METAMORPHOSIS_ENDERMAN_RESULT.get();
        } else if (containsAny(path, AQUATIC_HINTS)) {
            configured = Config.METAMORPHOSIS_AQUATIC_RESULT.get();
        } else if (containsAny(path, BEAST_HINTS) || hasPart(path, BEAST_PARTS)) {
            configured = Config.METAMORPHOSIS_BEAST_RESULT.get();
        }

        ResourceLocation location = ResourceLocation.tryParse(configured);
        if (location == null) return null;
        return EntityType.byString(location.toString()).orElse(null);
    }

    /**
     * Replace {@code source} with its abominable form in place.
     */
    private static void morph(ServerLevel level, LivingEntity source, LivingEntity victim) {
        EntityType<?> type = resultType(source);
        if (type == null) {
            if (Config.DEBUG_LOGGING.get()) {
                Phayriosis_cataclysm.LOGGER.info("[Cataclysm] {} found nothing to morph into",
                        source.getName().getString());
            }
            return;
        }
        if (type == source.getType()) return;

        Entity created = type.create(level);
        if (!(created instanceof LivingEntity replacement)) {
            if (created != null) created.discard();
            return;
        }

        replacement.moveTo(source.getX(), source.getY(), source.getZ(), source.getYRot(), source.getXRot());
        replacement.setYHeadRot(source.getYRot());
        replacement.setDeltaMovement(source.getDeltaMovement());
        replacement.fallDistance = source.fallDistance;
        level.addFreshEntity(replacement);

        // The new body picks the fight up where the old one left it.
        LivingEntity target = victim;
        if (replacement instanceof Mob replacementMob) {
            if (source instanceof Mob sourceMob) {
                if (sourceMob.getTarget() != null) target = sourceMob.getTarget();
                if (sourceMob.isPersistenceRequired()) replacementMob.setPersistenceRequired();
            }
            if (target != null && target != replacement && target.isAlive()) {
                replacementMob.setTarget(target);
            }
        }
        replacement.setHealth(replacement.getMaxHealth());

        BlockPos pos = source.blockPosition();
        double x = source.getX();
        double y = source.getY() + source.getBbHeight() * 0.5D;
        double z = source.getZ();
        level.sendParticles(ParticleTypes.SCULK_SOUL, x, y, z, 30, 0.4D, 0.6D, 0.4D, 0.02D);
        level.sendParticles(ParticleTypes.CRIMSON_SPORE, x, y, z, 40, 0.4D, 0.5D, 0.4D, 0.0D);
        level.sendParticles(ParticleTypes.POOF, x, y, z, 15, 0.3D, 0.3D, 0.3D, 0.01D);
        level.playSound(null, pos, SoundEvents.ZOMBIE_INFECT, SoundSource.HOSTILE, 1.0F, 0.7F);
        level.playSound(null, pos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.HOSTILE, 0.7F, 0.6F);

        // The old body is replaced, not killed: no loot, no death message, no XP.
        source.discard();

        if (Config.DEBUG_LOGGING.get()) {
            Phayriosis_cataclysm.LOGGER.info("[Cataclysm] {} morphed into {}",
                    source.getName().getString(), replacement.getName().getString());
        }
    }

    /** Registry path of an entity id ("phayriosisreborn:converted_cow" -> "converted_cow"). */
    private static String pathOf(String registryId) {
        ResourceLocation location = ResourceLocation.tryParse(registryId);
        return location == null ? registryId : location.getPath();
    }

    private static boolean containsAny(String path, Set<String> hints) {
        for (String hint : hints) {
            if (path.contains(hint)) return true;
        }
        return false;
    }

    /** Whole underscore-separated parts, so "cat" never matches "cataclysm". */
    private static boolean hasPart(String path, Set<String> parts) {
        for (String part : path.split("_")) {
            if (parts.contains(part)) return true;
        }
        return false;
    }
}
