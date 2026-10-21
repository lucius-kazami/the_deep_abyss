package com.akashi.abyss.abyss.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public class BrachiosaurusEntity extends Animal implements GeoEntity {

    private static final RawAnimation IDLE =
            RawAnimation.begin().thenLoop("idle");

    private static final RawAnimation WALK =
            RawAnimation.begin().thenLoop("walk");

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(
                    BrachiosaurusEntity.class,
                    EntityDataSerializers.INT
            );

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    public BrachiosaurusEntity(
            EntityType<? extends Animal> type,
            Level level
    ) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 150.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.13D)
                .add(Attributes.ATTACK_DAMAGE, 18.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.STEP_HEIGHT, 2.0D);
    }

    @Override
    protected void defineSynchedData(
            SynchedEntityData.Builder builder
    ) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, 0);
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, Math.floorMod(variant, 3));
    }


    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnReason,
            @Nullable SpawnGroupData spawnData
    ) {
        SpawnGroupData result = super.finalizeSpawn(
                level, difficulty, spawnReason, spawnData
        );

        // 0 — зелёный, 1 — фиолетовый, 2 — серый
        this.setVariant(this.random.nextInt(3));

        return result;
    }


    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Variant", getVariant());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setVariant(input.getIntOr("Variant", 0));
    }

    @Override
    public void tick() {
        super.tick();
        DinosaurJumpHelper.tick(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // Защищается, но не нападает первым
        this.goalSelector.addGoal(
                1, new MeleeAttackGoal(this, 0.9D, true)
        );

        this.goalSelector.addGoal(2, new BreedGoal(this, 0.8D));

        this.goalSelector.addGoal(
                3,
                new TemptGoal(
                        this,
                        0.8D,
                        stack -> stack.is(Items.WHEAT),
                        false
                )
        );

        this.goalSelector.addGoal(
                4, new FollowParentGoal(this, 0.8D)
        );

        this.goalSelector.addGoal(
                5, new WaterAvoidingRandomStrollGoal(this, 0.55D)
        );

        this.goalSelector.addGoal(
                6, new LookAtPlayerGoal(this, Player.class, 12.0F)
        );

        this.goalSelector.addGoal(
                7, new RandomLookAroundGoal(this)
        );

        this.targetSelector.addGoal(
                1, new HurtByTargetGoal(this)
        );
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
        BrachiosaurusEntity baby =
                ModEntities.BRACHIOSAURUS.get()
                        .create(level, EntitySpawnReason.BREEDING);

        if (baby != null) {
            baby.setVariant(this.random.nextInt(3));
        }

        return baby;
    }

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {
        controllers.add(
                new AnimationController<>(
                        "move",
                        5,
                        state -> state.setAndContinue(
                                state.isMoving() ? WALK : IDLE
                        )
                )
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
