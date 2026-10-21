package com.akashi.abyss.abyss.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import com.akashi.abyss.abyss.entity.water.ElasmosaurusEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public class MosasaurusEntity extends WaterAnimal implements GeoEntity {


    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(
                    MosasaurusEntity.class,
                    EntityDataSerializers.INT
            );

    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(
                    MosasaurusEntity.class,
                    EntityDataSerializers.BOOLEAN
            );

    private static final EntityDataAccessor<Boolean> BABY =
            SynchedEntityData.defineId(
                    MosasaurusEntity.class,
                    EntityDataSerializers.BOOLEAN
            );

    private static final int GROW_TIME = 24000;

    private int growingTicks = 0;

    private static final RawAnimation IDLE =
            RawAnimation.begin().thenLoop("animation.mosasaurus.idle");

    private static final RawAnimation SWIM =
            RawAnimation.begin().thenLoop("animation.mosasaurus.swim");

    private static final RawAnimation ATTACK =
            RawAnimation.begin().thenPlay("animation.mosasaurus.attack");


    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    private int attackAnimationTicks;
    private int dashCooldown;

    public MosasaurusEntity(
            EntityType<? extends WaterAnimal> type,
            Level level
    ) {
        super(type, level);

        // Управление плаванием во всех трёх направлениях
        this.moveControl = new SmoothSwimmingMoveControl(
                this,
                85,
                10,
                0.1F, 0.5F, true
        );

        if (!level.isClientSide()) {
            setVariant(random.nextInt(3));
        }
    }
    public boolean isMosasaurusBaby() {
        return entityData.get(BABY);
    }

    public void setMosasaurusBaby(boolean baby) {
        entityData.set(BABY, baby);
        growingTicks = 0;
        refreshDimensions();
    }


    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 80.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.10D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, 0);
        builder.define(ATTACKING, false);
        builder.define(BABY, false);
    }

    public int getVariant() {
        return entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        entityData.set(VARIANT, Math.floorMod(variant, 3));
    }

    public boolean isAttacking() {
        return entityData.get(ATTACKING);
    }

    @Override
    public void travel(Vec3 movementInput) {
        if (isInWater() && isEffectiveAi()) {
            moveRelative(0.08F, movementInput);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.9D));
        } else {
            super.travel(movementInput);
        }
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }


    @Override
    protected void registerGoals() {

        // Преследование и атака игрока
        goalSelector.addGoal(
                1,
                new MeleeAttackGoal(this, 2.2D, true)
        );

        // Быстрое свободное плавание по океану
        goalSelector.addGoal(
                3,
                new RandomSwimmingGoal(this, 2.0D, 15)
        );

        // Ответная атака
        targetSelector.addGoal(
                1,

                new HurtByTargetGoal(this) {
                    @Override
                    public boolean canUse() {
                        return !MosasaurusEntity.this.isMosasaurusBaby()
                                && super.canUse();
                    }
                }
        );

        // Поиск игроков


        targetSelector.addGoal(
                2,
                new NearestAttackableTargetGoal<Player>(
                        this,
                        Player.class,
                        true
                ) {
                    @Override
                    public boolean canUse() {
                        return !MosasaurusEntity.this.isMosasaurusBaby()
                                && super.canUse();
                    }

                    @Override
                    public boolean canContinueToUse() {
                        return !MosasaurusEntity.this.isMosasaurusBaby()
                                && super.canContinueToUse();
                    }
                }
        );
        // Взрослый Мозазавр охотится на Эласмозавров
        this.targetSelector.addGoal(3,
                new NearestAttackableTargetGoal<ElasmosaurusEntity>(
                        this,
                        ElasmosaurusEntity.class,
                        true
                ) {
                    @Override
                    public boolean canUse() {
                        return !MosasaurusEntity.this.isMosasaurusBaby()
                                && super.canUse();
                    }

                    @Override
                    public boolean canContinueToUse() {
                        return !MosasaurusEntity.this.isMosasaurusBaby()
                                && super.canContinueToUse();
                    }
                }
        );
    }

    @Override
    public void tick() {
        super.tick();

// Детёныш постепенно взрослеет
        if (isMosasaurusBaby()) {
            growingTicks++;

            if (growingTicks >= GROW_TIME) {
                setMosasaurusBaby(false);
            }
        }


        if (attackAnimationTicks > 0) {
            attackAnimationTicks--;

            if (attackAnimationTicks == 0) {
                entityData.set(ATTACKING, false);
            }
        }

        if (dashCooldown > 0) {
            dashCooldown--;
        }

        // Ускорение во время охоты
        if (isInWater()
                && getTarget() != null
                && getTarget().isAlive()
                && !isMosasaurusBaby()
                && dashCooldown == 0) {

            Vec3 direction = getTarget()
                    .position()
                    .subtract(position());

            double distanceSquared = direction.lengthSqr();

            if (distanceSquared > 16.0D
                    && distanceSquared < 400.0D) {

                Vec3 push = direction.normalize().scale(0.65D);

                setDeltaMovement(
                        getDeltaMovement().add(push)
                );

                hasImpulse = true;
                dashCooldown = 50;
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {

        // Детёныши не атакуют
        if (isMosasaurusBaby()) {
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
                        return state.setAndContinue(SWIM);
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
    protected void addAdditionalSaveData(
            net.minecraft.world.level.storage.ValueOutput output
    ) {
        super.addAdditionalSaveData(output);

        output.putBoolean("MosasaurusBaby", isMosasaurusBaby());
        output.putInt("MosasaurusGrowingTicks", growingTicks);
        output.putInt("MosasaurusVariant", getVariant());
    }

    @Override
    protected void readAdditionalSaveData(
            net.minecraft.world.level.storage.ValueInput input
    ) {
        super.readAdditionalSaveData(input);

        entityData.set(
                BABY,
                input.getBooleanOr("MosasaurusBaby", false)
        );

        growingTicks = input.getIntOr("MosasaurusGrowingTicks", 0);

        setVariant(input.getIntOr("MosasaurusVariant", 0));

        refreshDimensions();
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        EntityDimensions dimensions = super.getDefaultDimensions(pose);

        if (isMosasaurusBaby()) {
            return dimensions.scale(0.35F);
        }

        return dimensions;
    }

}
