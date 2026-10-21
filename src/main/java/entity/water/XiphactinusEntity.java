package com.akashi.abyss.abyss.entity.water;

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
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class XiphactinusEntity extends WaterAnimal implements GeoEntity {
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(XiphactinusEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ATTACKING =
            SynchedEntityData.defineId(XiphactinusEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE =
            RawAnimation.begin().thenLoop("animation.xiphactinus.idle");
    private static final RawAnimation SWIM =
            RawAnimation.begin().thenLoop("animation.xiphactinus.swim");
    private static final RawAnimation ATTACK =
            RawAnimation.begin().thenPlay("animation.xiphactinus.attack");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private int attackAnimationTicks;
    private int dashCooldown;

    public XiphactinusEntity(EntityType<? extends WaterAnimal> type, Level level) {
        super(type, level);
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.1F, 0.5F, true);
        if (!level.isClientSide()) setVariant(random.nextInt(3));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.65D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.35D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, 0);
        builder.define(ATTACKING, false);
    }

    public int getVariant() { return entityData.get(VARIANT); }
    public void setVariant(int variant) { entityData.set(VARIANT, Math.floorMod(variant, 3)); }
    public boolean isAttacking() { return entityData.get(ATTACKING); }

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
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.6D, true));
        goalSelector.addGoal(3, new RandomSwimmingGoal(this, 1.2D, 20));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<Player>(this, Player.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;

        if (attackAnimationTicks > 0 && --attackAnimationTicks == 0) {
            entityData.set(ATTACKING, false);
        }
        if (dashCooldown > 0) dashCooldown--;

        if (isInWater() && getTarget() != null && getTarget().isAlive() && dashCooldown == 0) {
            Vec3 direction = getTarget().position().subtract(position());
            double distanceSquared = direction.lengthSqr();
            if (distanceSquared > 9.0D && distanceSquared < 225.0D) {
                setDeltaMovement(getDeltaMovement().add(direction.normalize().scale(0.35D)));
                hasImpulse = true;
                dashCooldown = 70;
            }
        }
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean success = super.doHurtTarget(level, target);
        if (success) {
            attackAnimationTicks = 15;
            entityData.set(ATTACKING, true);
        }
        return success;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("main", 4, state -> {
            if (isAttacking()) return state.setAndContinue(ATTACK);
            if (getDeltaMovement().lengthSqr() > 0.0004D) return state.setAndContinue(SWIM);
            return state.setAndContinue(IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("XiphactinusVariant", getVariant());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setVariant(input.getIntOr("XiphactinusVariant", 0));
    }
}
