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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animatable.processing.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.akashi.abyss.abyss.Abyss;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;

public class TriceratopsEntity extends Animal implements GeoEntity {

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation RUN  = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation EAT  = RawAnimation.begin().thenLoop("eat");

    private static final EntityDataAccessor<Boolean> EATING =
            SynchedEntityData.defineId(TriceratopsEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> RUNNING =
            SynchedEntityData.defineId(TriceratopsEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> VARIANT =
            SynchedEntityData.defineId(TriceratopsEntity.class, EntityDataSerializers.INT);

    public void roar() {
        if (!this.level().isClientSide()) {
            this.playSound(
                    net.minecraft.sounds.SoundEvents.RAVAGER_ROAR,
                    2.5F,
                    0.65F
            );
        }
    }

    private static final RawAnimation ROAR =
            RawAnimation.begin().thenPlay("roar");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public TriceratopsEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        if (!level.isClientSide()) {
            this.setVariant(this.random.nextInt(3));
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.20D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.50D)
                .add(Attributes.STEP_HEIGHT, 2.0D);
    }

    // ---------- синхронизируемые флаги ----------
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(EATING, false);
        builder.define(RUNNING, false);
        builder.define(VARIANT, 0);
    }

    public boolean isEating() { return this.entityData.get(EATING); }
    public void setEating(boolean value) { this.entityData.set(EATING, value); }
    public boolean isRunning() { return this.entityData.get(RUNNING); }

    @Override
    public void tick() {
        super.tick();
        DinosaurJumpHelper.tick(this);

        if (!this.level().isClientSide()) {

            // Проверяем, бежит ли тираннозавр
            boolean run = !this.getNavigation().isDone()
                    && this.getMoveControl().getSpeedModifier() > 1.05D;

            // Обновляем состояние бега
            if (run != this.entityData.get(RUNNING)) {
                this.entityData.set(RUNNING, run);
            }

            // Случайное рычание каждые 20 секунд (шанс 1 из 3)
            if (this.tickCount % 400 == 0
                    && this.random.nextInt(3) == 0) {
                this.roar();
            }
        }
    }

    // ---------- ИИ ----------
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // убегать от опасных мобов (хищники и монстры)
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Monster.class, 14.0F, 1.0D, 1.6D));
        // убегать при получении урона
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.6D));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.1D, stack -> stack.is(Items.WHEAT), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(5, new TriceratopsGrazeGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.WHEAT);
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        AgeableMob baby = ModEntities.TRICERATOPS.get().create(level, EntitySpawnReason.BREEDING);
        if (baby instanceof TriceratopsEntity child && partner instanceof TriceratopsEntity other) {
            child.setVariant(this.random.nextBoolean() ? this.getVariant() : other.getVariant());
        }
        return baby;
    }

    // ---------- GeckoLib ----------
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("main", 8, state -> {
            boolean moving = this.walkAnimation.speed() > 0.02F;
            if (this.isEating()) return state.setAndContinue(EAT);
            if (moving && this.isRunning()) return state.setAndContinue(RUN);
            if (moving) return state.setAndContinue(WALK);
            return state.setAndContinue(IDLE);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public int getVariant() { return this.entityData.get(VARIANT); }
    public void setVariant(int v) { this.entityData.set(VARIANT, Math.floorMod(v, 3)); }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Variant", this.getVariant());
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setVariant(input.getIntOr("Variant", 0));
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return level.getBlockState(pos.below()).is(Abyss.ABYSS_GRASS.get()) ? 10.0F : -10.0F;
    }
}