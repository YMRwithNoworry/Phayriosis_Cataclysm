package alku.phayriosis_cataclysm.entity;

import alku.phayriosis_cataclysm.Config;
import alku.phayriosis_cataclysm.entity.ai.AdaptiveCarapaceHandler;
import alku.phayriosis_cataclysm.entity.ai.CallSwarmGoal;
import alku.phayriosis_cataclysm.entity.ai.SiegeBreakGoal;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Fused Horror - a large, dangerous infection mob created when two Larvae merge.
 * Has higher HP, damage, and possesses the Adaptive Carapace and Call Swarm abilities.
 */
public class FusedHorrorEntity extends Monster {

    private static final EntityDataAccessor<Integer> CARAPACE_HITS =
            SynchedEntityData.defineId(FusedHorrorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CARAPACE_TIMER =
            SynchedEntityData.defineId(FusedHorrorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> LAST_DAMAGE_TYPE =
            SynchedEntityData.defineId(FusedHorrorEntity.class, EntityDataSerializers.STRING);

    private final AdaptiveCarapaceHandler carapaceHandler = new AdaptiveCarapaceHandler();

    public FusedHorrorEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 20;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 80.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new CallSwarmGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(3, new SiegeBreakGoal(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CARAPACE_HITS, 0);
        this.entityData.define(CARAPACE_TIMER, 0);
        this.entityData.define(LAST_DAMAGE_TYPE, "");
    }

    /**
     * Apply adaptive carapace damage reduction.
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide() && InfectionStageManager_getCurrentStage() >= Config.CARAPACE_MIN_STAGE.get()) {
            amount = carapaceHandler.processDamage(this, source, amount);
        }
        return super.hurt(source, amount);
    }

    private int InfectionStageManager_getCurrentStage() {
        return alku.phayriosis_cataclysm.core.InfectionStageManager.getCurrentStage();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            carapaceHandler.tick(this);
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RAVAGER_AMBIENT;
    }
}
