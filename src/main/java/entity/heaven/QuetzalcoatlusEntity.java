package com.akashi.abyss.abyss.entity.heaven;

import net.minecraft.network.syncher.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class QuetzalcoatlusEntity extends PathfinderMob implements GeoEntity {
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(QuetzalcoatlusEntity.class, EntityDataSerializers.INT);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.quetzalcoatlus.idle");
    private static final RawAnimation FLY = RawAnimation.begin().thenLoop("animation.quetzalcoatlus.fly");
    private static final RawAnimation GLIDE = RawAnimation.begin().thenLoop("animation.quetzalcoatlus.glide");
    private static final RawAnimation REST =
            RawAnimation.begin().thenLoop("animation.quetzalcoatlus.sit");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.quetzalcoatlus.walk");
    private static final EntityDataAccessor<Boolean> BABY =
            SynchedEntityData.defineId(QuetzalcoatlusEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int GROWTH_TICKS = 24000; // 20 minutes loaded in-game
    private int growthTicks;
    private static final EntityDataAccessor<Integer> FLIGHT_MODE =
            SynchedEntityData.defineId(QuetzalcoatlusEntity.class, EntityDataSerializers.INT);
    // 0 = ground, 1 = flapping, 2 = gliding, 3 = resting
    private int flightTicks;
    private int groundTicks;
    private int restTicks;
    private int attackCooldown;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public QuetzalcoatlusEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, false);
        if (!level.isClientSide()) setVariant(random.nextInt(3));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.FLYING_SPEED, 0.45D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D);
    }

    @Override protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation nav = new FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        return nav;
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(1, new SurvivalGoal());
        goalSelector.addGoal(2, new HuntSmallPreyGoal());
        goalSelector.addGoal(3, new FlightCycleGoal());
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, 0);
        builder.define(FLIGHT_MODE, 0);
        builder.define(BABY, false);
    }
    public int getVariant() { return entityData.get(VARIANT); }
    public void setVariant(int value) { entityData.set(VARIANT, Math.floorMod(value, 3)); }

    public boolean isQuetzalcoatlusBaby() { return entityData.get(BABY); }
    public void setQuetzalcoatlusBaby(boolean baby) {
        entityData.set(BABY, baby);
        growthTicks = baby ? GROWTH_TICKS : 0;
        if (baby) {
            setFlightMode(0);
            getNavigation().stop();
        }
    }

    public int getFlightMode() { return entityData.get(FLIGHT_MODE); }
    private void setFlightMode(int mode) { entityData.set(FLIGHT_MODE, mode); }

    @Override public void aiStep() {
        super.aiStep();
        if (level().isClientSide()) return;
        if (isQuetzalcoatlusBaby()) {
            if (growthTicks > 0) growthTicks--;
            if (growthTicks <= 0) setQuetzalcoatlusBaby(false);
        }
        if (attackCooldown > 0) attackCooldown--;
        if (restTicks > 0) restTicks--;
        if (onGround()) {
            groundTicks++;
            flightTicks = 0;
            if (restTicks > 0) setFlightMode(3);
            else setFlightMode(0);
        } else {
            flightTicks++;
            groundTicks = 0;
            // In sustained flight use long glides between wing beats.
            setFlightMode(flightTicks % 100 < 65 ? 2 : 1);
        }
    }

    private boolean clearAir(Vec3 pos) {
        BlockPos at = BlockPos.containing(pos);
        return level().isEmptyBlock(at) && level().isEmptyBlock(at.above())
                && level().isEmptyBlock(at.above(2));
    }

    private class FlightCycleGoal extends Goal {
        private Vec3 destination;
        private int duration;
        private boolean landing;

        FlightCycleGoal() { setFlags(EnumSet.of(Flag.MOVE)); }
        @Override public boolean canUse() { return true; }
        @Override public boolean canContinueToUse() { return true; }
        @Override public void tick() {
            if (isQuetzalcoatlusBaby()) { navigation.stop(); return; }
            if (restTicks > 0 && onGround()) { navigation.stop(); return; }
            if (onGround()) {
                if (groundTicks > 100 + random.nextInt(200) && random.nextInt(45) == 0) {
                    // Azhdarchid-style launch: brief upward push, then powered flight.
                    navigation.stop();

                    setDeltaMovement(getDeltaMovement().add(0, 0.85D, 0));
                    hasImpulse = true;

                    destination = position().add(
                            random.nextInt(17) - 8,
                            12 + random.nextInt(8),
                            random.nextInt(17) - 8
                    );

                    landing = false;
                    duration = 400 + random.nextInt(500);

                    navigation.moveTo(
                            destination.x,
                            destination.y,
                            destination.z,
                            1.2D
                    );
                } else if (navigation.isDone() && random.nextInt(45) == 0) {
                    Vec3 walk = position().add(random.nextInt(15)-7, 0, random.nextInt(15)-7);
                    navigation.moveTo(walk.x, walk.y, walk.z, 0.55D);
                }
                return;
            }
            if (duration-- <= 0) landing = true;
            if (landing) {
                // Descend towards solid ground, avoiding water and lava.
                if (destination == null || position().distanceToSqr(destination) < 9 || tickCount % 65 == 0)
                    destination = findLandingSpot();
                if (destination != null) navigation.moveTo(destination.x, destination.y, destination.z, 0.65D);
                if (onGround()) { landing = false; restTicks = 100 + random.nextInt(180); destination = null; }
                return;
            }
            if (destination == null || position().distanceToSqr(destination) < 16 || tickCount % 110 == 0) {
                for (int i = 0; i < 12; i++) {
                    Vec3 candidate = position().add(random.nextInt(37)-18, random.nextInt(15)-5, random.nextInt(37)-18);
                    if (clearAir(candidate) && candidate.y > level().getMinY()+8 && candidate.y < level().getMaxY()-8) {
                        destination = candidate; break;
                    }
                }
            }
            if (destination != null) navigation.moveTo(destination.x, destination.y, destination.z, 0.95D);
            // Collision recovery: attempt to rise above nearby obstacles.
            if (horizontalCollision && tickCount % 10 == 0) {
                navigation.stop(); destination = position().add(0, 7, 0);
            }
        }
        private Vec3 findLandingSpot() {
            for (int i = 0; i < 16; i++) {
                int x = blockPosition().getX() + random.nextInt(29)-14;
                int z = blockPosition().getZ() + random.nextInt(29)-14;
                BlockPos p = new BlockPos(x, blockPosition().getY(), z);
                for (int d = 0; d < 30 && p.getY() > level().getMinY()+2; d++,p=p.below()) {
                    if (level().getBlockState(p.below()).isSolid() && clearAir(Vec3.atBottomCenterOf(p))
                            && level().isEmptyBlock(p.above(3))) return Vec3.atBottomCenterOf(p);
                }
            }
            return null;
        }
    }

    private class SurvivalGoal extends Goal {
        private LivingEntity danger;
        SurvivalGoal() { setFlags(EnumSet.of(Flag.MOVE)); }
        @Override public boolean canUse() {
            if (isQuetzalcoatlusBaby() || random.nextInt(12) != 0) return false;
            List<LivingEntity> nearby = level().getEntitiesOfClass(LivingEntity.class,
                    getBoundingBox().inflate(12), e -> e != QuetzalcoatlusEntity.this && e.isAlive()
                    && (e instanceof Player player && !player.isCreative() && !player.isSpectator()
                    || e.getBbWidth() > 2.8F));
            danger = nearby.stream().min((a,b) -> Double.compare(distanceToSqr(a),distanceToSqr(b))).orElse(null);
            return danger != null && (getHealth() < 25 || distanceToSqr(danger) < 25);
        }
        @Override public boolean canContinueToUse() { return danger != null && danger.isAlive() && distanceToSqr(danger) < 300; }
        @Override public void tick() {
            Vec3 away = position().subtract(danger.position());
            if (away.lengthSqr() < 0.1) away = new Vec3(1,0,0);
            Vec3 target = position().add(away.normalize().scale(16)).add(0,9,0);
            if (onGround()) setDeltaMovement(getDeltaMovement().add(0,0.65,0));
            navigation.moveTo(target.x,target.y,target.z,1.3D);
        }
        @Override public void stop() { danger = null; navigation.stop(); }
    }

    private class HuntSmallPreyGoal extends Goal {
        private LivingEntity prey;
        HuntSmallPreyGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() {
            if (isQuetzalcoatlusBaby() || getHealth() < 15 || random.nextInt(100) != 0) return false;
            List<LivingEntity> candidates = level().getEntitiesOfClass(LivingEntity.class,
                    getBoundingBox().inflate(14), e -> e != QuetzalcoatlusEntity.this
                    && e.isAlive() && !(e instanceof Player) && e.getBbWidth() <= 0.9F
                    && e.getBbHeight() <= 1.4F);
            prey = candidates.stream().min((a,b) -> Double.compare(distanceToSqr(a),distanceToSqr(b))).orElse(null);
            return prey != null;
        }
        @Override public boolean canContinueToUse() { return prey != null && prey.isAlive() && distanceToSqr(prey) < 400; }
        @Override public void tick() {
            getLookControl().setLookAt(prey,30,30);
            navigation.moveTo(prey,1.05D);
            if (distanceToSqr(prey) < 9 && attackCooldown == 0) {
                doHurtTarget(getServerLevel(QuetzalcoatlusEntity.this),prey);
                attackCooldown = 35;
            }
        }
        @Override public void stop() { prey = null; navigation.stop(); }
    }

    @Override public void travel(Vec3 input) {
        if (isEffectiveAi() && !onGround()) {
            moveRelative(0.05F, input);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.91D));
        } else super.travel(input);
    }

    @Override protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, BlockPos pos) {}
    @Override public boolean causeFallDamage(double distance, float multiplier, net.minecraft.world.damagesource.DamageSource source) { return false; }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("main", 4, state ->
                state.setAndContinue(getFlightMode() == 3 ? REST :
                        getFlightMode() == 2 ? GLIDE :
                        getFlightMode() == 1 ? FLY :
                        (getDeltaMovement().horizontalDistanceSqr() > 0.0005D ? WALK : IDLE))));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("QuetzalcoatlusVariant", getVariant());
        output.putBoolean("QuetzalcoatlusBaby", isQuetzalcoatlusBaby());
        output.putInt("QuetzalcoatlusGrowthTicks", growthTicks);
    }
    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setVariant(input.getIntOr("QuetzalcoatlusVariant", 0));
        boolean baby = input.getBooleanOr("QuetzalcoatlusBaby", false);
        entityData.set(BABY, baby);
        growthTicks = baby ? input.getIntOr("QuetzalcoatlusGrowthTicks", GROWTH_TICKS) : 0;
    }
}
