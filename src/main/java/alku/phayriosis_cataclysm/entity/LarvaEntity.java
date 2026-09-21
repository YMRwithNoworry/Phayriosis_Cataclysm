package alku.phayriosis_cataclysm.entity;

import alku.phayriosis_cataclysm.entity.ai.CallSwarmGoal;
import alku.phayriosis_cataclysm.entity.ai.MergeGoal;
import alku.phayriosis_cataclysm.entity.ai.SiegeBreakGoal;
import alku.phayriosis_cataclysm.init.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

/**
 * Larva - a small, weak infection mob that spawns in groups.
 * Can merge with nearby larvae to form a FusedHorror.
 * Has a merge cooldown to prevent instant merging.
 */
public class LarvaEntity extends Monster {

    private static final EntityDataAccessor<Integer> MERGE_COOLDOWN =
            SynchedEntityData.defineId(LarvaEntity.class, EntityDataSerializers.INT);

    private int mergeTimer = 0;
    private boolean isMerging = false;

    public LarvaEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 3;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ARMOR, 2.0)
                // Targeting range: every PHA creature is normalised to Config.PHA_FOLLOW_RANGE
                // when it joins the world; this is the pre-join default and should match it.
                .add(Attributes.FOLLOW_RANGE, 64.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MergeGoal(this));
        this.goalSelector.addGoal(2, new CallSwarmGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(4, new SiegeBreakGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(MERGE_COOLDOWN, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            int cooldown = this.entityData.get(MERGE_COOLDOWN);
            if (cooldown > 0) {
                this.entityData.set(MERGE_COOLDOWN, cooldown - 1);
            }
        }
    }

    public boolean canMerge() {
        return this.entityData.get(MERGE_COOLDOWN) <= 0 && !this.isDeadOrDying() && !this.isMerging;
    }

    public void setMergeCooldown(int ticks) {
        this.entityData.set(MERGE_COOLDOWN, ticks);
    }

    public void setMerging(boolean merging) {
        this.isMerging = merging;
    }

    public boolean isMerging() {
        return this.isMerging;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("MergeCooldown", this.entityData.get(MERGE_COOLDOWN));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(MERGE_COOLDOWN, tag.getInt("MergeCooldown"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SLIME_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SLIME_DEATH;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SLIME_SQUISH;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                         MobSpawnType spawnType, @Nullable SpawnGroupData data,
                                         @Nullable CompoundTag tag) {
        return super.finalizeSpawn(level, difficulty, spawnType, data, tag);
    }
}
