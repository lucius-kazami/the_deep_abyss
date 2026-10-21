package com.akashi.abyss.abyss.entity.water;

import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.storage.*;
import net.neoforged.neoforge.common.NeoForgeMod;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;

public class OrthocerasEntity extends PathfinderMob implements GeoEntity {
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(OrthocerasEntity.class, EntityDataSerializers.INT);
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.orthoceras.idle");
    private static final RawAnimation SWIM = RawAnimation.begin().thenLoop("animation.orthoceras.swim");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int alarmTicks;
    private int restTicks;

    public OrthocerasEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.08F, 0.12F, false);
        if (!level.isClientSide()) setVariant(random.nextInt(3));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(NeoForgeMod.SWIM_SPEED, 0.65D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, 0);
    }

    public int getVariant() { return entityData.get(VARIANT); }
    public void setVariant(int value) { entityData.set(VARIANT, Math.floorMod(value, 3)); }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new EscapeThreatGoal());
        goalSelector.addGoal(1, new RestGoal());
        goalSelector.addGoal(2, new FollowSchoolGoal());
        goalSelector.addGoal(3, new HuntFishGoal());
        goalSelector.addGoal(4, new RandomSwimmingGoal(this, 0.85D, 35));
    }

    private boolean isThreat(LivingEntity entity) {
        if (entity == this || !entity.isAlive()) return false;
        if (entity instanceof Player player) return !player.isCreative() && !player.isSpectator();
        // Не привязываемся к пакетам классов хищников: достаточно их зарегистрированных имён.
        String id = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE
                .getKey(entity.getType()).getPath();
        return id.equals("mosasaurus") || id.equals("xiphactinus");
    }

    @Override public void aiStep() {
        super.aiStep();
        if (level().isClientSide()) return;
        if (alarmTicks > 0) alarmTicks--;
        if (restTicks > 0) restTicks--;
        if (tickCount % 20 == 0 && alarmTicks == 0 && restTicks == 0
                && random.nextInt(100) < 2) restTicks = 40 + random.nextInt(81);
    }

    private class EscapeThreatGoal extends Goal {
        private LivingEntity threat;
        private Vec3 escape;
        EscapeThreatGoal() { setFlags(EnumSet.of(Flag.MOVE)); }

        @Override public boolean canUse() {
            List<LivingEntity> nearby = level().getEntitiesOfClass(LivingEntity.class,
                    getBoundingBox().inflate(14.0D), OrthocerasEntity.this::isThreat);
            threat = null;
            double best = Double.MAX_VALUE;
            for (LivingEntity candidate : nearby) {
                double distance = distanceToSqr(candidate);
                if (distance < best) { best = distance; threat = candidate; }
            }
            return threat != null || alarmTicks > 0 && escape != null;
        }

        @Override public boolean canContinueToUse() {
            return alarmTicks > 0 && escape != null && isInWater();
        }

        @Override public void start() { alarmTicks = 200; pickEscape(); }
        @Override public void tick() {
            if (threat != null && threat.isAlive() && tickCount % 12 == 0) pickEscape();
            if (escape != null && (navigation.isDone() || tickCount % 25 == 0))
                navigation.moveTo(escape.x, escape.y, escape.z, 1.65D);
        }
        @Override public void stop() { threat = null; navigation.stop(); }

        private void pickEscape() {
            if (threat == null) return;
            Vec3 away = position().subtract(threat.position());
            if (away.lengthSqr() < 0.01D) away = new Vec3(random.nextDouble() - 0.5D, 0, random.nextDouble() - 0.5D);
            away = away.normalize();
            for (int i = 0; i < 12; i++) {
                Vec3 candidate = position().add(away.scale(7 + random.nextInt(7)))
                        .add(random.nextInt(7) - 3, random.nextInt(7) - 3, random.nextInt(7) - 3);
                if (level().getFluidState(net.minecraft.core.BlockPos.containing(candidate))
                        .is(net.minecraft.tags.FluidTags.WATER)) {
                    escape = candidate;
                    navigation.moveTo(candidate.x, candidate.y, candidate.z, 1.65D);
                    return;
                }
            }
        }
    }

    private class RestGoal extends Goal {
        RestGoal() { setFlags(EnumSet.of(Flag.MOVE)); }
        @Override public boolean canUse() { return restTicks > 0 && alarmTicks == 0 && isInWater(); }
        @Override public boolean canContinueToUse() { return canUse(); }
        @Override public void start() { navigation.stop(); }
        @Override public void tick() {
            navigation.stop();
            setDeltaMovement(getDeltaMovement().scale(0.8D));
        }
    }

    private class FollowSchoolGoal extends Goal {
        private OrthocerasEntity leader;
        FollowSchoolGoal() { setFlags(EnumSet.of(Flag.MOVE)); }
        @Override public boolean canUse() {
            if (alarmTicks > 0 || restTicks > 0 || random.nextInt(12) != 0) return false;
            List<OrthocerasEntity> school = level().getEntitiesOfClass(OrthocerasEntity.class,
                    getBoundingBox().inflate(12.0D), e -> e != OrthocerasEntity.this && e.isAlive());
            if (school.isEmpty()) return false;
            // Следуем только за сущностью со старшим ID: исключает взаимное преследование.
            leader = school.stream().filter(e -> e.getId() < getId())
                    .min((a, b) -> Double.compare(distanceToSqr(a), distanceToSqr(b)))
                    .orElse(null);
            return leader != null && distanceToSqr(leader) > 12.0D;
        }
        @Override public boolean canContinueToUse() {
            return leader != null && leader.isAlive() && alarmTicks == 0
                    && distanceToSqr(leader) > 5.0D && distanceToSqr(leader) < 225.0D;
        }
        @Override public void tick() {
            if (leader != null && tickCount % 15 == 0)
                navigation.moveTo(leader, 1.0D);
        }
        @Override public void stop() { leader = null; navigation.stop(); }
    }

    private class HuntFishGoal extends Goal {
        private AbstractFish prey;
        private int cooldown;
        HuntFishGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() {
            if (alarmTicks > 0 || restTicks > 0 || getHealth() < 8.0F || random.nextInt(50) != 0) return false;
            List<AbstractFish> fish = level().getEntitiesOfClass(AbstractFish.class,
                    getBoundingBox().inflate(9.0D), f -> f.isAlive());
            prey = fish.stream().min((a, b) -> Double.compare(distanceToSqr(a), distanceToSqr(b))).orElse(null);
            return prey != null;
        }
        @Override public boolean canContinueToUse() {
            return prey != null && prey.isAlive() && alarmTicks == 0
                    && restTicks == 0 && distanceToSqr(prey) < 144.0D;
        }
        @Override public void start() { cooldown = 0; }
        @Override public void tick() {
            if (prey == null) return;
            getLookControl().setLookAt(prey, 20.0F, 20.0F);
            navigation.moveTo(prey, 1.15D);
            if (cooldown > 0) cooldown--;
            if (cooldown == 0 && distanceToSqr(prey) < 4.0D) {
                doHurtTarget(getServerLevel(OrthocerasEntity.this), prey);
                cooldown = 25;
            }
        }
        @Override public void stop() { prey = null; navigation.stop(); }
    }

    @Override public void travel(Vec3 input) {
        if (isInWater() && isEffectiveAi()) {
            moveRelative(0.065F, input);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.87D));
        } else super.travel(input);
    }

    @Override public boolean canBreatheUnderwater() { return true; }
    @Override public void baseTick() {
        super.baseTick();
        if (!level().isClientSide()) setAirSupply(getMaxAirSupply());
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("main", 4, state ->
                state.setAndContinue(getDeltaMovement().lengthSqr() > 0.0003D ? SWIM : IDLE)));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    @Override protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("OrthocerasVariant", getVariant());
    }
    @Override protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setVariant(input.getIntOr("OrthocerasVariant", 0));
    }
}
