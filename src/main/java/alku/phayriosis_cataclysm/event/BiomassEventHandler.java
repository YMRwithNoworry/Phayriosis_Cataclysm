package alku.phayriosis_cataclysm.event;

import alku.phayriosis_cataclysm.Config;
import alku.phayriosis_cataclysm.Phayriosis_cataclysm;
import alku.phayriosis_cataclysm.core.InfectionStageManager;
import alku.phayriosis_cataclysm.core.PhayriosisCompat;
import alku.phayriosis_cataclysm.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles biomass accumulation from mob kills and player deaths.
 * 
 * When an infection mob kills a vanilla mob -> biomass is added.
 * When a player dies to an infection mob -> massive biomass + infection core spawns.
 */
@Mod.EventBusSubscriber(modid = Phayriosis_cataclysm.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BiomassEventHandler {

    /**
     * Track when a living entity dies and check if an infection mob caused it.
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        Level level = victim.level();
        if (level.isClientSide()) return;

        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();

        // Check if the attacker is an infection mob
        if (attacker == null || !PhayriosisCompat.isInfectionMob(attacker)) return;

        ServerLevel serverLevel = (ServerLevel) level;

        if (victim instanceof Player player) {
            // Player killed by infection - major event!
            handlePlayerDeathByInfection(serverLevel, player);
        } else if (PhayriosisCompat.isVillager(victim)) {
            // Villager killed - high value target
            InfectionStageManager.addBiomass(serverLevel, Config.BIOMASS_PER_VILLAGER_KILL.get(), "villager_kill");
        } else if (PhayriosisCompat.isFarmAnimal(victim)) {
            // Farm animal - catalyst
            InfectionStageManager.addBiomass(serverLevel, Config.BIOMASS_PER_FARM_ANIMAL.get(), "farm_animal_kill");
        } else if (PhayriosisCompat.isVanillaMob(victim)) {
            // Regular vanilla mob
            InfectionStageManager.addBiomass(serverLevel, Config.BIOMASS_PER_VANILLA_KILL.get(), "vanilla_mob_kill");
        }
    }

    /**
     * When a player dies to an infection mob:
     * 1. Spawn an infection core at their death location
     * 2. Add massive biomass
     */
    private static void handlePlayerDeathByInfection(ServerLevel level, Player player) {
        BlockPos deathPos = player.blockPosition();

        // Add biomass
        InfectionStageManager.addBiomass(level, Config.BIOMASS_PER_PLAYER_DEATH.get(), "player_death");

        // Spawn infection core at death location
        spawnInfectionCore(level, deathPos);

        Phayriosis_cataclysm.LOGGER.info("[Cataclysm] Player {} died to infection at {}! Infection core spawned.",
                player.getName().getString(), deathPos.toShortString());
    }

    /**
     * Spawn an infection core block at the given position.
     * Tries to find a suitable surface if the position is in the air.
     */
    private static void spawnInfectionCore(ServerLevel level, BlockPos pos) {
        // Find solid ground
        BlockPos spawnPos = pos;
        for (int dy = 0; dy > -10; dy--) {
            BlockPos check = pos.offset(0, dy, 0);
            if (!level.getBlockState(check).isAir() && level.getBlockState(check.above()).isAir()) {
                spawnPos = check.above();
                break;
            }
        }

        // Place the infection core
        level.setBlock(spawnPos, ModBlocks.INFECTION_CORE.get().defaultBlockState(), 3);
    }
}
