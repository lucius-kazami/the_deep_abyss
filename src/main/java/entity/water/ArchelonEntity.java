package com.akashi.abyss.abyss.entity.water;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

public class ArchelonEntity extends PathfinderMob implements GeoEntity {

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(
                    ArchelonEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> BABY =
            SynchedEntityData.defineId(
                    ArchelonEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(
                    ArchelonEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int GROW_TIME = 12000;

    private int growingTicks = 0;
    private int attackAnimationTicks = 0;

    private PathNavigation waterNavigation;
    private PathNavigation landNavigation;

    private MoveControl waterMoveControl;
    private MoveControl landMoveControl;

    private static final RawAnimation IDLE =
            RawAnimation.begin().thenLoop("animation.archelon.idle");

    private static final RawAnimation SWIM =
            RawAnimation.begin().thenLoop("animation.archelon.swim");

    private static final RawAnimation ATTACK =
            RawAnimation.begin().thenPlay("animation.archelon.attack");

    private static final RawAnimation WALK =
            RawAnimation.begin().thenLoop("animation.archelon.walk");

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);


    public ArchelonEntity(
            EntityType<? extends PathfinderMob> type,
            Level level
    ) {
        super(type, level);

        this.waterNavigation =
                new WaterBoundPathNavigation(this, level);

        this.landNavigation =
                new GroundPathNavigation(this, level);

        this.waterMoveControl =
                new SmoothSwimmingMoveControl(
                        this, 85, 10, 0.1F, 0.5F, true
                );

        this.landMoveControl = new MoveControl(this);

        this.navigation = landNavigation;
        this.moveControl = landMoveControl;

        if (!level.isClientSide()) {
            setVariant(random.nextInt(3));
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.18D)
                .add(NeoForgeMod.SWIM_SPEED, 0.8D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    @Override
    protected void defineSynchedData(
            SynchedEntityData.Builder builder
    ) {
        super.defineSynchedData(builder);

        builder.define(VARIANT, 0);
        builder.define(BABY, false);
        builder.define(ATTACKING, false);
    }

    public int getVariant() {
        return entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        entityData.set(VARIANT, Math.floorMod(variant, 3));
    }

    public boolean isArchelonBaby() {
        return entityData.get(BABY);
    }

    public void setArchelonBaby(boolean baby) {
        entityData.set(BABY, baby);
        growingTicks = 0;
        refreshDimensions();
    }

    public boolean isAttacking() {
        return entityData.get(ATTACKING);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new GroundPathNavigation(this, level);
    }

    @Override
    public void aiStep() {

        if (!level().isClientSide()) {

            if (isInWater()) {

                if (navigation != waterNavigation) {

                    navigation.stop();
                    navigation = waterNavigation;
                    moveControl = waterMoveControl;
                }

            } else {

                if (navigation != landNavigation) {

                    navigation.stop();
                    navigation = landNavigation;
                    moveControl = landMoveControl;
                }
            }
        }

        super.aiStep();
    }
    @Override
    protected void registerGoals() {

        goalSelector.addGoal(
                1,
                new RandomSwimmingGoal(this, 1.0D, 20) {
                    @Override
                    public boolean canUse() {
                        return ArchelonEntity.this.isInWater()
                                && super.canUse();
                    }

                    @Override
                    public boolean canContinueToUse() {
                        return ArchelonEntity.this.isInWater()
                                && super.canContinueToUse();
                    }
                }
        );

        goalSelector.addGoal(
                2,
                new RandomStrollGoal(this, 0.7D, 80) {
                    @Override
                    public boolean canUse() {
                        return !ArchelonEntity.this.isInWater()
                                && super.canUse();
                    }

                    @Override
                    public boolean canContinueToUse() {
                        return !ArchelonEntity.this.isInWater()
                                && super.canContinueToUse();
                    }
                }
        );

        targetSelector.addGoal(
                1,
                new HurtByTargetGoal(this)
        );
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {

            if (isArchelonBaby()) {
                growingTicks++;

                if (growingTicks >= GROW_TIME) {
                    setArchelonBaby(false);
                }
            }

            if (attackAnimationTicks > 0) {
                attackAnimationTicks--;

                if (attackAnimationTicks == 0) {
                    entityData.set(ATTACKING, false);
                }
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {

        if (isArchelonBaby()) {
            return false;
        }

        boolean success = super.doHurtTarget(level, target);

        if (success) {
            attackAnimationTicks = 20;
            entityData.set(ATTACKING, true);
        }

        return success;
    }

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {
        controllers.add(
                new AnimationController<>("main", 4, state -> {

                    if (isAttacking()) {
                        return state.setAndContinue(ATTACK);
                    }

                    if (getDeltaMovement().lengthSqr() > 0.0004D) {

                        if (isInWater()) {
                            return state.setAndContinue(SWIM);
                        }

                        return state.setAndContinue(WALK);
                    }

                    return state.setAndContinue(IDLE);
                })
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        EntityDimensions dimensions =
                super.getDefaultDimensions(pose);

        if (isArchelonBaby()) {
            return dimensions.scale(0.35F);
        }

        return dimensions;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);

        output.putBoolean("ArchelonBaby", isArchelonBaby());
        output.putInt("ArchelonGrowingTicks", growingTicks);
        output.putInt("ArchelonVariant", getVariant());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);

        entityData.set(
                BABY,
                input.getBooleanOr("ArchelonBaby", false)
        );

        growingTicks = input.getIntOr(
                "ArchelonGrowingTicks", 0
        );

        setVariant(input.getIntOr("ArchelonVariant", 0));

        refreshDimensions();
    }
    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public void baseTick() {
        super.baseTick();

        if (!level().isClientSide()) {
            setAirSupply(getMaxAirSupply());
        }
    }
    @Override
    public void travel(Vec3 movementInput) {

        if (isInWater() && isEffectiveAi()) {

            moveRelative(0.08F, movementInput);

            move(MoverType.SELF, getDeltaMovement());

            setDeltaMovement(
                    getDeltaMovement().scale(0.9D)
            );

            // Небольшая плавучесть
            if (getTarget() == null) {
                setDeltaMovement(
                        getDeltaMovement().add(0.0D, 0.005D, 0.0D)
                );
            }

        } else {
            super.travel(movementInput);
        }
    }
    @Override
    public boolean isPushedByFluid() {
        return false;
    }
}
