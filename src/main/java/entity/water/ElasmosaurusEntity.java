package com.akashi.abyss.abyss.entity.water;

import com.akashi.abyss.abyss.entity.MosasaurusEntity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;

import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.tags.FluidTags;

public class ElasmosaurusEntity extends WaterAnimal implements GeoEntity {

    // ==========================================
    // СИНХРОНИЗИРУЕМЫЕ ДАННЫЕ
    // ==========================================

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(
                    ElasmosaurusEntity.class,
                    EntityDataSerializers.INT
            );

    private static final EntityDataAccessor<Boolean> BABY =
            SynchedEntityData.defineId(
                    ElasmosaurusEntity.class,
                    EntityDataSerializers.BOOLEAN
            );

    // 10 минут = 12000 тиков
    private static final int GROW_TIME = 12000;

    private int growingTicks = 0;

    // ==========================================
    // АНИМАЦИИ GECKOLIB
    // ==========================================

    private static final RawAnimation IDLE =
            RawAnimation.begin()
                    .thenLoop("animation.elasmosaurus.idle");

    private static final RawAnimation SWIM =
            RawAnimation.begin()
                    .thenLoop("animation.elasmosaurus.swim");

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    // ==========================================
    // КОНСТРУКТОР
    // ==========================================

    public ElasmosaurusEntity(
            EntityType<? extends WaterAnimal> type,
            Level level
    ) {
        super(type, level);

        // Плавное движение под водой
        this.moveControl = new SmoothSwimmingMoveControl(
                this,
                85,
                10,
                0.1F, 0.5F, true
        );

        // Случайная окраска при создании
        if (!level.isClientSide()) {
            setVariant(random.nextInt(3));
        }
    }

    // ==========================================
    // ХАРАКТЕРИСТИКИ
    // ==========================================

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.85D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }

    // ==========================================
    // СИНХРОНИЗАЦИЯ
    // ==========================================

    @Override
    protected void defineSynchedData(
            SynchedEntityData.Builder builder
    ) {
        super.defineSynchedData(builder);

        builder.define(VARIANT, 0);
        builder.define(BABY, false);
    }

    // ==========================================
    // ТРИ ВАРИАНТА ОКРАСКИ
    // ==========================================

    public int getVariant() {
        return entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        entityData.set(
                VARIANT,
                Math.floorMod(variant, 3)
        );
    }

    // ==========================================
    // ДЕТЁНЫШ И ВЗРОСЛЕНИЕ
    // ==========================================

    public boolean isElasmosaurusBaby() {
        return entityData.get(BABY);
    }

    public void setElasmosaurusBaby(boolean baby) {
        entityData.set(BABY, baby);

        growingTicks = 0;

        refreshDimensions();
    }
    public static boolean canSpawn(
            EntityType<ElasmosaurusEntity> type,
            ServerLevelAccessor level,
            EntitySpawnReason reason,
            BlockPos pos,
            RandomSource random
    ) {
        return level.getFluidState(pos).is(FluidTags.WATER)
                && level.getFluidState(pos.above()).is(FluidTags.WATER);
    }

    // ==========================================
    // ВОДНАЯ НАВИГАЦИЯ
    // ==========================================

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

    // ==========================================
    // ИСКУССТВЕННЫЙ ИНТЕЛЛЕКТ
    // ==========================================


    @Override
    protected void registerGoals() {

        // 1. Избегаем взрослых Мозазавров
        goalSelector.addGoal(
                1,
                new AvoidEntityGoal<MosasaurusEntity>(
                        this,
                        MosasaurusEntity.class,
                        24.0F,
                        1.3D,
                        1.8D,
                        entity -> entity instanceof MosasaurusEntity mosasaurus
                                && !mosasaurus.isMosasaurusBaby()
                )
        );

        // 2. Самозащита при нападении
        goalSelector.addGoal(
                2,
                new MeleeAttackGoal(
                        this,
                        1.2D,
                        true
                )
        );

        // 3. Спокойное плавание
        goalSelector.addGoal(
                4,
                new RandomSwimmingGoal(
                        this,
                        1.0D,
                        30
                )
        );

        // Атакуем только в ответ на полученный урон
        targetSelector.addGoal(
                1,
                new HurtByTargetGoal(this)
        );
    }

    // ==========================================
    // ОБНОВЛЕНИЕ СОСТОЯНИЯ
    // ==========================================

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide()) {
            return;
        }

        // Детёныш взрослеет за 10 минут
        if (isElasmosaurusBaby()) {

            growingTicks++;

            if (growingTicks >= GROW_TIME) {
                setElasmosaurusBaby(false);
            }
        }

        // На суше не преследуем противников
        if (!isInWater() && getTarget() != null) {
            setTarget(null);
        }

        // Не атакуем Мозазавра, а стараемся уплыть
        if (getTarget() instanceof MosasaurusEntity) {
            setTarget(null);
        }

        // Детёныши не вступают в бой
        if (isElasmosaurusBaby() && getTarget() != null) {
            setTarget(null);
        }
    }

    // ==========================================
    // САМОЗАЩИТА
    // ==========================================

    @Override
    public boolean doHurtTarget(
            ServerLevel level,
            Entity target
    ) {

        // Детёныши не атакуют
        if (isElasmosaurusBaby()) {
            return false;
        }

        // Не атакуем на суше
        if (!isInWater()) {
            return false;
        }

        // Не атакуем Мозазавров
        if (target instanceof MosasaurusEntity) {
            return false;
        }

        return super.doHurtTarget(level, target);
    }

    // ==========================================
    // АНИМАЦИИ
    // ==========================================

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {
        controllers.add(
                new AnimationController<>(
                        "main",
                        4,
                        state -> {

                            // Плавание при движении
                            if (getDeltaMovement().lengthSqr() > 0.0004D) {
                                return state.setAndContinue(SWIM);
                            }

                            // Спокойная анимация
                            return state.setAndContinue(IDLE);
                        }
                )
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // ==========================================
    // СОХРАНЕНИЕ ДАННЫХ
    // ==========================================

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);

        output.putBoolean(
                "ElasmosaurusBaby",
                isElasmosaurusBaby()
        );

        output.putInt(
                "ElasmosaurusGrowingTicks",
                growingTicks
        );

        output.putInt(
                "ElasmosaurusVariant",
                getVariant()
        );
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);

        entityData.set(
                BABY,
                input.getBooleanOr(
                        "ElasmosaurusBaby",
                        false
                )
        );

        growingTicks = input.getIntOr(
                "ElasmosaurusGrowingTicks",
                0
        );

        setVariant(
                input.getIntOr(
                        "ElasmosaurusVariant",
                        0
                )
        );

        refreshDimensions();
    }

    // ==========================================
    // РАЗМЕР ДЕТЁНЫША
    // ==========================================

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {

        EntityDimensions dimensions =
                super.getDefaultDimensions(pose);

        if (isElasmosaurusBaby()) {
            return dimensions.scale(0.35F);
        }

        return dimensions;
    }
}
