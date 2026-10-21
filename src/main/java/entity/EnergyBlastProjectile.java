package com.akashi.abyss.abyss.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;


public class EnergyBlastProjectile extends ThrowableProjectile {

    // ============================================================
    // НАСТРОЙКИ
    // ============================================================

    private static final float DAMAGE = 12.0F;

    // Максимальная жизнь снаряда
    private static final int MAX_LIFETIME = 100;

    private static final int BLOCK_BREAK_RADIUS = 3;
    private static final boolean DROP_BLOCKS = false;


    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public EnergyBlastProjectile(
            EntityType<? extends EnergyBlastProjectile> entityType,
            Level level
    ) {
        super(entityType, level);
    }


    public EnergyBlastProjectile(
            EntityType<? extends EnergyBlastProjectile> entityType,
            LivingEntity owner,
            Level level
    ) {

        super(entityType, level);

        setOwner(owner);

        setPos(
                owner.getX(),
                owner.getEyeY() - 0.15D,
                owner.getZ()
        );
    }


    // ============================================================
    // SYNCHRONIZED DATA
    // ============================================================

    @Override
    protected void defineSynchedData(
            SynchedEntityData.Builder builder
    ) {
    }


    // ============================================================
    // БЕЗ ГРАВИТАЦИИ
    // ============================================================

    @Override
    protected double getDefaultGravity() {
        return 0.0D;
    }


    // ============================================================
    // TICK
    // ============================================================

    @Override
    public void tick() {

        super.tick();


        // ========================================================
        // ЭНЕРГЕТИЧЕСКИЙ ХВОСТ
        // ========================================================

        if (level().isClientSide()) {

            Vec3 movement = getDeltaMovement();

            double backX = -movement.x * 0.04D;
            double backY = -movement.y * 0.04D;
            double backZ = -movement.z * 0.04D;


            // ----------------------------------------------------
            // ЯРКОЕ ЯДРО
            // ----------------------------------------------------

            level().addParticle(
                    ParticleTypes.END_ROD,
                    getX(),
                    getY(),
                    getZ(),
                    backX,
                    backY,
                    backZ
            );


            // ----------------------------------------------------
            // ЭЛЕКТРИЧЕСКАЯ ЭНЕРГИЯ
            // ----------------------------------------------------

            level().addParticle(
                    ParticleTypes.ELECTRIC_SPARK,
                    getX(),
                    getY(),
                    getZ(),
                    backX * 2.0D,
                    backY * 2.0D,
                    backZ * 2.0D
            );


            // ----------------------------------------------------
            // ВНЕШНИЙ ЭНЕРГЕТИЧЕСКИЙ СЛОЙ
            // ----------------------------------------------------

            level().addParticle(
                    ParticleTypes.PORTAL,

                    getX() + (random.nextDouble() - 0.5D) * 0.20D,
                    getY() + (random.nextDouble() - 0.5D) * 0.20D,
                    getZ() + (random.nextDouble() - 0.5D) * 0.20D,

                    backX,
                    backY,
                    backZ
            );


            // ----------------------------------------------------
            // ЕЩЁ ОДНА ИСКРА
            // ----------------------------------------------------

            if (random.nextBoolean()) {

                level().addParticle(
                        ParticleTypes.ELECTRIC_SPARK,

                        getX() + (random.nextDouble() - 0.5D) * 0.25D,
                        getY() + (random.nextDouble() - 0.5D) * 0.25D,
                        getZ() + (random.nextDouble() - 0.5D) * 0.25D,

                        0.0D,
                        0.0D,
                        0.0D
                );
            }
        }


        // ========================================================
        // УДАЛЕНИЕ СТАРОГО СНАРЯДА
        // ========================================================

        if (tickCount > MAX_LIFETIME) {
            discard();
        }
    }


    // ============================================================
    // ПОПАДАНИЕ В ENTITY
    // ============================================================

    @Override
    protected void onHitEntity(
            EntityHitResult result
    ) {

        super.onHitEntity(result);


        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }


        if (!(result.getEntity() instanceof LivingEntity target)) {
            return;
        }


        // Не наносим урон стрелку
        if (target == getOwner()) {
            return;
        }


        // ========================================================
        // DAMAGE
        // ========================================================

        target.hurtServer(
                serverLevel,

                damageSources().thrown(
                        this,
                        getOwner()
                ),

                DAMAGE
        );


        // ========================================================
        // KNOCKBACK
        // ========================================================

        Vec3 movement =
                getDeltaMovement();


        double horizontal =
                Math.sqrt(
                        movement.x * movement.x
                                +
                                movement.z * movement.z
                );


        if (horizontal > 0.001D) {

            target.push(
                    movement.x / horizontal * 1.3D,
                    0.30D,
                    movement.z / horizontal * 1.3D
            );
        }


        // ========================================================
        // ENERGY IMPACT
        // ========================================================

        createEnergyImpact(serverLevel);
    }


    // ============================================================
    // ПОПАДАНИЕ В BLOCK
    // ============================================================

    @Override
    protected void onHitBlock(
            BlockHitResult result
    ) {

        super.onHitBlock(result);

        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        // Эффекты попадания
        createEnergyImpact(serverLevel);

        // Разрушаем блоки вокруг точки попадания
        destroyBlocks(
                serverLevel,
                result.getBlockPos()
        );
    }


    // ============================================================
    // ОБЩЕЕ ПОПАДАНИЕ
    // ============================================================

    @Override
    protected void onHit(
            HitResult result
    ) {

        super.onHit(result);


        if (level().isClientSide()) {
            return;
        }


        discard();
    }


    // ============================================================
    // ENERGY IMPACT
    // ============================================================

    private void createEnergyImpact(
            ServerLevel level
    ) {

        // ========================================================
        // ЦЕНТРАЛЬНАЯ ЭНЕРГЕТИЧЕСКАЯ ВСПЫШКА
        // ========================================================

                level.sendParticles(
                        ParticleTypes.ELECTRIC_SPARK,

                        getX(),
                        getY(),
                        getZ(),

                        12,

                        0.15D,
                        0.15D,
                        0.15D,

                        0.25D
                );


        // ========================================================
        // БЕЛАЯ ЭНЕРГИЯ
        // ========================================================

        level.sendParticles(
                ParticleTypes.END_ROD,

                getX(),
                getY(),
                getZ(),

                20,

                0.35D,
                0.35D,
                0.35D,

                0.12D
        );


        // ========================================================
        // ЭЛЕКТРИЧЕСКИЕ ИСКРЫ
        // ========================================================

        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,

                getX(),
                getY(),
                getZ(),

                30,

                0.50D,
                0.50D,
                0.50D,

                0.20D
        );


        // ========================================================
        // ЭНЕРГЕТИЧЕСКОЕ ОБЛАКО
        // ========================================================

        level.sendParticles(
                ParticleTypes.PORTAL,

                getX(),
                getY(),
                getZ(),

                25,

                0.45D,
                0.45D,
                0.45D,

                0.18D
        );


        // ========================================================
        // ЗВУК УДАРА
        // ========================================================

        level.playSound(
                null,

                getX(),
                getY(),
                getZ(),

                SoundEvents.BEACON_DEACTIVATE,

                SoundSource.PLAYERS,

                0.8F,
                1.7F
        );
    }

    private void destroyBlocks(
            ServerLevel level,
            BlockPos center
    ) {

        int radius = BLOCK_BREAK_RADIUS;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {

                    // Делаем область разрушения сферической
                    if (x * x + y * y + z * z > radius * radius) {
                        continue;
                    }

                    BlockPos pos = center.offset(x, y, z);

                    BlockState state =
                            level.getBlockState(pos);

                    // Воздух пропускаем
                    if (state.isAir()) {
                        continue;
                    }

                    // Не ломаем Bedrock и другие
                    // неразрушимые блоки
                    if (state.getDestroySpeed(level, pos) < 0.0F) {
                        continue;
                    }

                    level.destroyBlock(
                            pos,
                            DROP_BLOCKS
                    );
                }
            }
        }
    }
}