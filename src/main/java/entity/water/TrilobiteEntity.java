package com.akashi.abyss.abyss.entity.water;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.List;

public class TrilobiteEntity extends PathfinderMob implements GeoEntity {
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(TrilobiteEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DEFENDING =
            SynchedEntityData.defineId(TrilobiteEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.trilobite.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.trilobite.walk");
    private static final RawAnimation SWIM = RawAnimation.begin().thenLoop("animation.trilobite.swim");
    private static final RawAnimation DEFEND = RawAnimation.begin().thenPlayAndHold("animation.trilobite.defend");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private PathNavigation waterNavigation;
    private PathNavigation landNavigation;
    private MoveControl waterControl;
    private MoveControl landControl;
    private boolean waterMode;
    private int defendTicks;
    private int feedTicks;
    private int outOfWaterTicks;

    public TrilobiteEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        // createNavigation вызывается из конструктора Mob до выполнения этого конструктора.
        landNavigation = this.navigation;
        waterNavigation = new WaterBoundPathNavigation(this, level);
        landControl = this.moveControl;
        waterControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.04F, 0.08F, false);
        if (!level.isClientSide()) setVariant(random.nextInt(3));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(NeoForgeMod.SWIM_SPEED, 0.65D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new GroundPathNavigation(this, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, 0);
        builder.define(DEFENDING, false);
    }

    public int getVariant() { return entityData.get(VARIANT); }
    public void setVariant(int variant) { entityData.set(VARIANT, Math.floorMod(variant, 3)); }
    public boolean isDefending() { return entityData.get(DEFENDING); }
    public boolean isFeeding() { return feedTicks > 0; }

    @Override
    protected void registerGoals() {
        // Избегаем игроков в выживании; не убегаем от игроков в креативе.
        goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class,
                5.0F, 1.05D, 1.2D,
                entity -> entity instanceof Player player
                        && !player.isCreative()
                        && !player.isSpectator()) {
            @Override public boolean canUse() { return !isDefending() && super.canUse(); }
            @Override public boolean canContinueToUse() { return !isDefending() && super.canContinueToUse(); }
        });
        goalSelector.addGoal(2, new FindWaterGoal());
        goalSelector.addGoal(3, new FindFoodGoal());
        goalSelector.addGoal(4, new FollowGroupGoal());
        goalSelector.addGoal(5, new BottomCrawlGoal());
        goalSelector.addGoal(6, new ShortSwimGoal());
        goalSelector.addGoal(7, new RandomStrollGoal(this, 0.85D, 45) {
            @Override public boolean canUse() { return !isInWater() && !isDefending() && super.canUse(); }
            @Override public boolean canContinueToUse() { return !isInWater() && !isDefending() && super.canContinueToUse(); }
        });
    }

    @Override
    public void tick() {
        if (!level().isClientSide()) {
            boolean inWater = isInWater();
            if (inWater != waterMode) {
                navigation.stop();
                navigation = inWater ? waterNavigation : landNavigation;
                moveControl = inWater ? waterControl : landControl;
                waterMode = inWater;
            }
        }
        super.tick();
        if (!level().isClientSide()) {
            if (defendTicks > 0) {
                defendTicks--;
                navigation.stop();
                getMoveControl().setWantedPosition(getX(), getY(), getZ(), 0);
                if (defendTicks == 0) entityData.set(DEFENDING, false);
            }
            if (feedTicks > 0) feedTicks--;
            if (isInWater()) outOfWaterTicks = 0;
            else if (++outOfWaterTicks > 600 && outOfWaterTicks % 100 == 0) {
                // По умолчанию на суше не наносим урон: только пытаемся вернуться в воду.
            }
        }
    }

    public void startDefending() {
        if (!level().isClientSide()) {
            defendTicks = 100;
            entityData.set(DEFENDING, true);
            navigation.stop();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        // Первый удар полный, последующие в течение 5 секунд наносят 40% урона.
        boolean alreadyDefending = isDefending();
        boolean hit = super.hurtServer(level, source, alreadyDefending ? amount * 0.4F : amount);
        if (hit && isAlive()) startDefending();
        return hit;
    }

    @Override
    public void travel(Vec3 input) {
        if (isInWater() && isEffectiveAi()) {
            if (isDefending()) {
                setDeltaMovement(getDeltaMovement().multiply(0.3D, 0.3D, 0.3D));
                move(MoverType.SELF, getDeltaMovement());
                return;
            }
            moveRelative(0.065F, input);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.82D).add(0, -0.003D, 0));
        } else super.travel(input);
    }

    @Override public boolean canBreatheUnderwater() { return true; }
    @Override public void baseTick() {
        super.baseTick();
        if (!level().isClientSide()) setAirSupply(getMaxAirSupply());
    }

    // Возвращает клетку воды непосредственно над твёрдым дном.
    private BlockPos bottomWaterAt(int x, int y, int z) {
        for (int dy = 3; dy >= -7; dy--) {
            BlockPos p = new BlockPos(x, y + dy, z);
            if (level().getFluidState(p).is(Fluids.WATER)
                    && level().getBlockState(p.below()).isCollisionShapeFullBlock(level(), p.below())) {
                return p;
            }
        }
        return null;
    }

    private boolean moveTo(BlockPos p, double speed) {
        return p != null && navigation.moveTo(p.getX() + 0.5D, p.getY() + 0.1D,
                p.getZ() + 0.5D, speed);
    }

    private abstract class WaterGoal extends Goal {
        protected BlockPos target;
        protected int timeout;
        WaterGoal() { setFlags(EnumSet.of(Flag.MOVE)); }
        @Override public boolean canContinueToUse() {
            return isInWater() && !isDefending() && timeout-- > 0 && !navigation.isDone();
        }
        @Override public void start() { timeout = 120; moveTo(target, 0.9D); }
        @Override public void stop() { target = null; }
    }

    // Основное поведение: 80% попыток выбора движения — ползание вдоль дна.
    private class BottomCrawlGoal extends WaterGoal {
        @Override public boolean canUse() {
            if (!isInWater() || isDefending() || feedTicks > 0 || random.nextInt(10) >= 8) return false;
            if (random.nextInt(18) != 0) return false;
            BlockPos base = blockPosition();
            for (int i = 0; i < 12; i++) {
                BlockPos p = bottomWaterAt(base.getX() + random.nextInt(13) - 6,
                        base.getY(), base.getZ() + random.nextInt(13) - 6);
                if (p != null && p.distSqr(base) > 3) { target = p; return true; }
            }
            return false;
        }
    }

    // Короткое плавание, цель в воде не более 3 блоков над ближайшим дном.
    private class ShortSwimGoal extends WaterGoal {
        @Override public boolean canUse() {
            if (!isInWater() || isDefending() || feedTicks > 0 || random.nextInt(80) != 0) return false;
            BlockPos base = blockPosition();
            for (int i = 0; i < 10; i++) {
                BlockPos floor = bottomWaterAt(base.getX() + random.nextInt(11) - 5,
                        base.getY(), base.getZ() + random.nextInt(11) - 5);
                if (floor == null) continue;
                BlockPos p = floor.above(1 + random.nextInt(3));
                if (level().getFluidState(p).is(Fluids.WATER)) { target = p; return true; }
            }
            return false;
        }
        @Override public void start() { timeout = 80; moveTo(target, 1.05D); }
    }

    private boolean edible(BlockState state) {
        return state.is(Blocks.SEAGRASS) || state.is(Blocks.TALL_SEAGRASS)
                || state.is(Blocks.KELP) || state.is(Blocks.KELP_PLANT);
    }

    private class FindFoodGoal extends WaterGoal {
        @Override public boolean canUse() {
            if (!isInWater() || isDefending() || feedTicks > 0 || random.nextInt(90) != 0) return false;
            BlockPos origin = blockPosition();
            for (int i = 0; i < 35; i++) {
                BlockPos p = origin.offset(random.nextInt(13) - 6,
                        random.nextInt(7) - 3, random.nextInt(13) - 6);
                if (edible(level().getBlockState(p)) && level().getFluidState(p).is(Fluids.WATER)) {
                    target = p; return true;
                }
            }
            return false;
        }
        @Override public void tick() {
            if (target != null && position().distanceToSqr(Vec3.atCenterOf(target)) < 2.5D) {
                feedTicks = 45;
                navigation.stop();
                timeout = 0;
            }
        }
    }

    private class FollowGroupGoal extends WaterGoal {
        @Override public boolean canUse() {
            if (!isInWater() || isDefending() || feedTicks > 0 || random.nextInt(55) != 0) return false;
            List<TrilobiteEntity> others = level().getEntitiesOfClass(TrilobiteEntity.class,
                    getBoundingBox().inflate(9), other -> other != TrilobiteEntity.this && other.isAlive());
            if (others.isEmpty()) return false;
            TrilobiteEntity closest = null;
            double distance = Double.MAX_VALUE;
            for (TrilobiteEntity other : others) {
                double d = distanceToSqr(other);
                if (d > 9 && d < distance) { distance = d; closest = other; }
            }
            if (closest == null) return false;
            target = closest.blockPosition();
            return true;
        }
        @Override public void start() { timeout = 80; moveTo(target, 0.95D); }
    }

    private class FindWaterGoal extends Goal {
        private BlockPos target;
        private int timeout;
        FindWaterGoal() { setFlags(EnumSet.of(Flag.MOVE)); }
        @Override public boolean canUse() {
            if (isInWater() || isDefending()) return false;
            BlockPos base = blockPosition();
            double best = Double.MAX_VALUE;
            target = null;
            for (int x = -8; x <= 8; x += 2) {
                for (int z = -8; z <= 8; z += 2) {
                    for (int y = -2; y <= 1; y++) {
                        BlockPos p = base.offset(x, y, z);
                        if (!level().getFluidState(p).is(Fluids.WATER)) continue;
                        double d = p.distSqr(base);
                        if (d < best) { best = d; target = p; }
                    }
                }
            }
            return target != null;
        }
        @Override public void start() {
            timeout = 160;
            moveTo(target, 1.1D);
        }
        @Override public boolean canContinueToUse() {
            return !isInWater() && !isDefending() && timeout-- > 0 && !navigation.isDone();
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("main", 4, state -> {
            if (isDefending()) return state.setAndContinue(DEFEND);
            if (getDeltaMovement().horizontalDistanceSqr() > 0.00025D)
                return state.setAndContinue(isInWater() && !onGround() ? SWIM : WALK);
            return state.setAndContinue(IDLE);
        }));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("TrilobiteVariant", getVariant());
        output.putInt("TrilobiteDefendTicks", defendTicks);
    }
    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setVariant(input.getIntOr("TrilobiteVariant", 0));
        defendTicks = input.getIntOr("TrilobiteDefendTicks", 0);
        entityData.set(DEFENDING, defendTicks > 0);
    }
}
