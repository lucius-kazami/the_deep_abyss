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

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

public class SpinosaurusEntity extends Animal implements GeoEntity {

    private static final RawAnimation IDLE =
            RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK =
            RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN =
            RawAnimation.begin().thenLoop("run");
    private static final RawAnimation SWIM =
            RawAnimation.begin().thenLoop("swim");
    private static final RawAnimation ATTACK =
            RawAnimation.begin().thenPlay("attack");

    private static final EntityDataAccessor<Boolean> RUNNING =
            SynchedEntityData.defineId(
                    SpinosaurusEntity.class,
                    EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(
                    SpinosaurusEntity.class,
                    EntityDataSerializers.INT);

    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(
                    SpinosaurusEntity.class,
                    EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache =
            GeckoLibUtil.createInstanceCache(this);

    private int attackAnimationTicks = 0;

    public SpinosaurusEntity(
            EntityType<? extends Animal> type,
            Level level
    ) {
        super(type, level);

        this.moveControl = new SpinosaurusSwimControl(this);
        this.setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER, 0.0F);

        if (!level.isClientSide()) {
            setVariant(random.nextInt(3));
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 180.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ATTACK_DAMAGE, 20.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.STEP_HEIGHT, 2.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
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
        goalSelector.addGoal(0, new SpinosaurusWaterGoal(this));
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3D, true));
        goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8D));
        goalSelector.addGoal(6,
                new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1,
                new NearestAttackableTargetGoal<>(
                        this, Player.class, true));

        targetSelector.addGoal(2,
                new NearestAttackableTargetGoal<>(
                        this,
                        AbstractFish.class,
                        true
                ));

        targetSelector.addGoal(3,
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
                ));

        targetSelector.addGoal(4, new HurtByTargetGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        DinosaurJumpHelper.tick(this);

        if (!level().isClientSide()) {
            boolean running =
                    getTarget() != null && !getNavigation().isDone();

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
            net.minecraft.world.entity.Entity target
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
        return stack.is(Items.COD)
                || stack.is(Items.SALMON)
                || stack.is(Items.BEEF)
                || stack.is(Items.CHICKEN);
    }

    @Override
    public AgeableMob getBreedOffspring(
            ServerLevel level,
            AgeableMob partner
    ) {
        AgeableMob baby = ModEntities.SPINOSAURUS.get()
                .create(level, EntitySpawnReason.BREEDING);

        if (baby instanceof SpinosaurusEntity child
                && partner instanceof SpinosaurusEntity other) {
            child.setVariant(random.nextBoolean()
                    ? getVariant()
                    : other.getVariant());
        }

        return baby;
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

                    if (isInWater()) {
                        return state.setAndContinue(SWIM);
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

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new AmphibiousPathNavigation(this, level);
    }
    private static class SpinosaurusSwimControl extends MoveControl {

        private final SpinosaurusEntity spino;

        public SpinosaurusSwimControl(SpinosaurusEntity spino) {
            super(spino);
            this.spino = spino;
        }

        @Override
        public void tick() {
            if (!spino.isInWater()) {
                super.tick();
                return;
            }

            if (operation != Operation.MOVE_TO) {
                spino.setSpeed(0.0F);
                return;
            }

            double dx = wantedX - spino.getX();
            double dy = wantedY - spino.getY();
            double dz = wantedZ - spino.getZ();

            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (distance < 0.35D) {
                operation = Operation.WAIT;
                spino.setSpeed(0.0F);
                return;
            }

            float yaw = (float) (
                    Mth.atan2(dz, dx) * (180.0D / Math.PI)
            ) - 90.0F;

            spino.setYRot(rotlerp(spino.getYRot(), yaw, 8.0F));
            spino.yBodyRot = spino.getYRot();

            float speed = (float) (
                    speedModifier
                            * spino.getAttributeValue(Attributes.MOVEMENT_SPEED)
            );

            spino.setSpeed(speed);

            Vec3 direction = new Vec3(dx, dy, dz).normalize();

            Vec3 velocity = spino.getDeltaMovement();

            spino.setDeltaMovement(
                    velocity.add(
                            direction.x * speed * 0.12D,
                            direction.y * speed * 0.12D,
                            direction.z * speed * 0.12D
                    ).scale(0.90D)
            );
        }
    }
    private static class SpinosaurusWaterGoal extends Goal {

        private final SpinosaurusEntity spino;
        private int cooldown;

        public SpinosaurusWaterGoal(SpinosaurusEntity spino) {
            this.spino = spino;
            this.setFlags(java.util.EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return spino.isInWater()
                    && spino.getTarget() == null;
        }

        @Override
        public boolean canContinueToUse() {
            return spino.isInWater()
                    && spino.getTarget() == null;
        }

        @Override
        public void tick() {
            if (--cooldown > 0 && !spino.getNavigation().isDone()) {
                return;
            }

            cooldown = 30 + spino.getRandom().nextInt(50);

            for (int attempt = 0; attempt < 12; attempt++) {
                int x = spino.getBlockX()
                        + spino.getRandom().nextInt(25) - 12;

                int y = spino.getBlockY()
                        + spino.getRandom().nextInt(13) - 6;

                int z = spino.getBlockZ()
                        + spino.getRandom().nextInt(25) - 12;

                var pos = new net.minecraft.core.BlockPos(x, y, z);

                if (!spino.level().getFluidState(pos)
                        .is(net.minecraft.tags.FluidTags.WATER)) {
                    continue;
                }

                // Учитываем большой размер спинозавра
                if (!spino.level().getFluidState(pos.above(2))
                        .is(net.minecraft.tags.FluidTags.WATER)) {
                    continue;
                }

                spino.getNavigation().moveTo(
                        x + 0.5D,
                        y + 0.5D,
                        z + 0.5D,
                        1.1D
                );

                return;
            }
        }
    }
}
