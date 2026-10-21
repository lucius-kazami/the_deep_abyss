package com.akashi.abyss.abyss.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
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

public class GiganotosaurusEntity extends Animal implements GeoEntity {

    private static final RawAnimation IDLE =
            RawAnimation.begin().thenLoop("idle");

    private static final RawAnimation WALK =
            RawAnimation.begin().thenLoop("walk");

    private static final RawAnimation RUN =
            RawAnimation.begin().thenLoop("run");

    private static final RawAnimation ATTACK =
            RawAnimation.begin().thenPlay("attack");

    private static final RawAnimation DEATH =
            RawAnimation.begin().thenPlay("death");

    private static final EntityDataAccessor<Boolean> RUNNING =
            SynchedEntityData.defineId(
                    GiganotosaurusEntity.class,
                    EntityDataSerializers.BOOLEAN
            );

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(
                    GiganotosaurusEntity.class,
                    EntityDataSerializers.INT
            );

    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(
                    GiganotosaurusEntity.class,
                    EntityDataSerializers.BOOLEAN
            );

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    private int attackAnimationTicks = 0;

    public GiganotosaurusEntity(
            EntityType<? extends Animal> type,
            Level level
    ) {
        super(type, level);

        if (!level.isClientSide()) {
            setVariant(random.nextInt(3));
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 220.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.29D)
                .add(Attributes.ATTACK_DAMAGE, 22.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.STEP_HEIGHT, 2.0D);
    }

    @Override
    protected void defineSynchedData(
            SynchedEntityData.Builder builder
    ) {
        super.defineSynchedData(builder);

        builder.define(RUNNING, false);
        builder.define(VARIANT, 0);
        builder.define(ATTACKING, false);
    }

    public int getVariant() {
        return entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        entityData.set(VARIANT, Math.floorMod(variant, 3));
    }

    public boolean isRunning() {
        return entityData.get(RUNNING);
    }

    public boolean isAttacking() {
        return entityData.get(ATTACKING);
    }

    @Override
    protected void registerGoals() {

        goalSelector.addGoal(
                0, new FloatGoal(this)
        );

        goalSelector.addGoal(
                1, new MeleeAttackGoal(this, 1.65D, true)
        );

        goalSelector.addGoal(
                5, new RandomStrollGoal(this, 0.8D)
        );

        goalSelector.addGoal(
                6, new LookAtPlayerGoal(
                        this, Player.class, 16.0F
                )
        );

        goalSelector.addGoal(
                7, new RandomLookAroundGoal(this)
        );

        // Нападает на игроков
        targetSelector.addGoal(
                1,
                new NearestAttackableTargetGoal<>(
                        this,
                        Player.class,
                        true
                )
        );

        // Охотится на крупных травоядных динозавров
        targetSelector.addGoal(
                2,
                new NearestAttackableTargetGoal<>(
                        this,
                        Animal.class,
                        10,
                        true,
                        false,
                        (animal, level) ->
                                animal instanceof TriceratopsEntity
                                        || animal instanceof StegosaurusEntity
                                        || animal instanceof ParasaurolophusEntity
                                        || animal instanceof AnkylosaurusEntity
                                        || animal instanceof BrachiosaurusEntity
                )
        );

        targetSelector.addGoal(
                3, new HurtByTargetGoal(this)
        );

        // Также охотится на обычных животных (но не на других хищников).
        targetSelector.addGoal(
                4, new NearestAttackableTargetGoal<>(
                        this,
                        Animal.class,
                        10,
                        true,
                        false,
                        (animal, level) -> !(animal instanceof GiganotosaurusEntity)
                                && !(animal instanceof TyrannosaurusEntity)
                                && !(animal instanceof SpinosaurusEntity)
                )
        );
    }

    @Override
    public void tick() {
        super.tick();
        DinosaurJumpHelper.tick(this);

        if (!level().isClientSide()) {

            boolean running =
                    getTarget() != null
                            && !getNavigation().isDone();

            if (isRunning() != running) {
                entityData.set(RUNNING, running);
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
    public boolean doHurtTarget(
            ServerLevel level,
            Entity target
    ) {
        boolean success = super.doHurtTarget(level, target);

        if (success) {
            attackAnimationTicks = 16;
            entityData.set(ATTACKING, true);
        }

        return success;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.BEEF)
                || stack.is(Items.CHICKEN)
                || stack.is(Items.PORKCHOP)
                || stack.is(Items.MUTTON);
    }

    @Override
    public AgeableMob getBreedOffspring(
            ServerLevel level,
            AgeableMob partner
    ) {
        AgeableMob baby = ModEntities.GIGANOTOSAURUS.get()
                .create(level, EntitySpawnReason.BREEDING);

        if (baby instanceof GiganotosaurusEntity child
                && partner instanceof GiganotosaurusEntity other) {

            child.setVariant(
                    random.nextBoolean()
                            ? getVariant()
                            : other.getVariant()
            );
        }

        return baby;
    }

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {
        controllers.add(
                new AnimationController<>("main", 4, state -> {

                    if (isDeadOrDying()) {
                        return state.setAndContinue(DEATH);
                    }

                    if (isAttacking()) {
                        return state.setAndContinue(ATTACK);
                    }

                    boolean moving =
                            walkAnimation.speed() > 0.02F;

                    if (moving && isRunning()) {
                        return state.setAndContinue(RUN);
                    }

                    if (moving) {
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
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Variant", getVariant());
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setVariant(input.getIntOr("Variant", 0));
    }
}
