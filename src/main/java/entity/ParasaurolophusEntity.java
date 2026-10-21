package com.akashi.abyss.abyss.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.Nullable;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ParasaurolophusEntity extends Animal implements GeoEntity {

    // 0 — фиолетовый, 1 — розовый, 2 — тёмно-фиолетовый
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(
                    ParasaurolophusEntity.class,
                    EntityDataSerializers.INT
            );

    private static final RawAnimation IDLE =
            RawAnimation.begin().thenLoop("idle");

    private static final RawAnimation WALK =
            RawAnimation.begin().thenLoop("walk");

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    public ParasaurolophusEntity(
            EntityType<? extends Animal> type,
            Level level
    ) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, 0);
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, Math.floorMod(variant, 4));
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason reason,
            @Nullable SpawnGroupData spawnData
    ) {
        SpawnGroupData data =
                super.finalizeSpawn(level, difficulty, reason, spawnData);

        setVariant(getRandom().nextInt(4));
        return data;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("ParasaurolophusVariant", getVariant());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setVariant(input.getIntOr("ParasaurolophusVariant", 0));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 36.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.35D)
                .add(Attributes.STEP_HEIGHT, 2.0D);
    }

    @Override
    public void tick() {
        super.tick();
        DinosaurJumpHelper.tick(this);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.35D));
        goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        goalSelector.addGoal(3,
                new TemptGoal(this, 1.0D,
                        stack -> stack.is(Items.WHEAT), false));
        goalSelector.addGoal(4, new FollowParentGoal(this, 1.0D));
        goalSelector.addGoal(5,
                new WaterAvoidingRandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(6,
                new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.WHEAT);
    }

    @Override
    public AgeableMob getBreedOffspring(
            ServerLevel level,
            AgeableMob partner
    ) {
        ParasaurolophusEntity baby =
                ModEntities.PARASAUROLOPHUS.get()
                        .create(level, EntitySpawnReason.BREEDING);

        if (baby != null) {
            baby.setVariant(getRandom().nextInt(3));
        }

        return baby;
    }

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {
        controllers.add(new AnimationController<>("move", 5,
                state -> state.setAndContinue(
                        state.isMoving() ? WALK : IDLE
                )));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
