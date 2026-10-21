package com.akashi.abyss.abyss.entity;

import com.akashi.abyss.abyss.Abyss;
import com.akashi.abyss.abyss.event.FrostFreezeEvents;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;


public class IceBladeProjectile
        extends ThrowableProjectile
        implements ItemSupplier {

    // ============================================================
    // НАСТРОЙКИ
    // ============================================================

    /**
     * Дополнительный ледяной урон.
     */
    private static final float DAMAGE = 5.0F;


    /**
     * Максимальное время жизни снаряда.
     *
     * 20 ticks = 1 секунда
     * 60 ticks = 3 секунды
     */
    private static final int MAX_LIFE = 60;


    private int life = 0;


    // ============================================================
    // КОНСТРУКТОР
    // ============================================================

    public IceBladeProjectile(
            EntityType<? extends IceBladeProjectile> type,
            Level level
    ) {

        super(type, level);
    }


    // ============================================================
    // ITEM SUPPLIER
    // ============================================================

    /**
     * Этот ItemStack используется IceBladeRenderer.
     *
     * Через модель этого предмета Minecraft получает
     * текстуру ice_blade_projectile.png.
     */
    @Override
    public ItemStack getItem() {

        return new ItemStack(
                Abyss.ICE_BLADE_PROJECTILE_ITEM.get()
        );
    }


    // ============================================================
    // SYNCHED DATA
    // ============================================================

    @Override
    protected void defineSynchedData(
            SynchedEntityData.Builder builder
    ) {

        // Пока дополнительных синхронизируемых данных нет.
    }


    // ============================================================
    // TICK
    // ============================================================

    @Override
    public void tick() {

        super.tick();


        // --------------------------------------------------------
        // ВРЕМЯ ЖИЗНИ
        // --------------------------------------------------------

        life++;


        if (life >= MAX_LIFE) {

            discard();

            return;
        }


        // Частицы создаём только на сервере.

        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }


        // ========================================================
        // ЧАСТИЦЫ ВОКРУГ ЛЕЗВИЯ
        // ========================================================

        serverLevel.sendParticles(
                ParticleTypes.SNOWFLAKE,

                getX(),
                getY(),
                getZ(),

                8,

                0.18,
                0.18,
                0.18,

                0.02
        );


        serverLevel.sendParticles(
                ParticleTypes.ITEM_SNOWBALL,

                getX(),
                getY(),
                getZ(),

                3,

                0.10,
                0.10,
                0.10,

                0.01
        );


        // ========================================================
        // ЛЕДЯНОЙ ХВОСТ
        // ========================================================

        Vec3 movement =
                getDeltaMovement();


        if (movement.lengthSqr() > 0.001D) {

            Vec3 direction =
                    movement.normalize();


            /*
             * Создаём несколько точек позади снаряда.
             */
            for (int i = 1; i <= 4; i++) {

                double distance =
                        i * 0.35D;


                double x =
                        getX()
                                - direction.x * distance;

                double y =
                        getY()
                                - direction.y * distance;

                double z =
                        getZ()
                                - direction.z * distance;


                serverLevel.sendParticles(
                        ParticleTypes.SNOWFLAKE,

                        x,
                        y,
                        z,

                        2,

                        0.06,
                        0.06,
                        0.06,

                        0.01
                );
            }
        }
    }


    // ============================================================
    // ПОПАДАНИЕ ПО СУЩЕСТВУ
    // ============================================================

    @Override
    protected void onHitEntity(
            EntityHitResult result
    ) {

        super.onHitEntity(result);


        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }


        Entity hitEntity =
                result.getEntity();


        // ========================================================
        // ПРОВЕРЯЕМ ЦЕЛЬ
        // ========================================================

        if (!(hitEntity instanceof LivingEntity target)) {

            discard();

            return;
        }


        // ========================================================
        // НЕ ПОПАДАЕМ ПО ВЛАДЕЛЬЦУ
        // ========================================================

        Entity owner =
                getOwner();


        if (target == owner) {

            return;
        }


        // ========================================================
        // +5 ЛЕДЯНОГО УРОНА
        // ========================================================

        target.hurtServer(
                serverLevel,
                serverLevel.damageSources().freeze(),
                DAMAGE
        );


        // ========================================================
        // ЗАМОРОЗКА НА 10 СЕКУНД
        // ========================================================

        FrostFreezeEvents.freeze(
                target
        );


        // ========================================================
        // БОЛЬШАЯ ЛЕДЯНАЯ ВСПЫШКА
        // ========================================================

        serverLevel.sendParticles(
                ParticleTypes.SNOWFLAKE,

                target.getX(),

                target.getY()
                        + target.getBbHeight() / 2.0,

                target.getZ(),

                50,

                0.6,
                0.8,
                0.6,

                0.08
        );


        // ========================================================
        // ОСКОЛКИ ЛЬДА
        // ========================================================

        serverLevel.sendParticles(
                ParticleTypes.ITEM_SNOWBALL,

                target.getX(),
                target.getY() + 1.0,
                target.getZ(),

                20,

                0.4,
                0.6,
                0.4,

                0.08
        );


        // ========================================================
        // УНИЧТОЖАЕМ СНАРЯД
        // ========================================================

        discard();
    }


    // ============================================================
    // ПОПАДАНИЕ В БЛОК
    // ============================================================

    @Override
    protected void onHitBlock(BlockHitResult result) {

        super.onHitBlock(result);

        if (!(level() instanceof ServerLevel serverLevel)) {
            discard();
            return;
        }

        BlockPos hitPos = result.getBlockPos();

        // Точка прямо перед поверхностью, куда попал снаряд
        BlockPos center = hitPos.relative(
                result.getDirection()
        );

        // ========================================================
        // СОЗДАЁМ ЛЕДЯНУЮ ОБЛАСТЬ
        // ========================================================

        createIceExplosion(
                serverLevel,
                center,
                3
        );


        // ========================================================
        // ЧАСТИЦЫ
        // ========================================================

        serverLevel.sendParticles(
                ParticleTypes.SNOWFLAKE,

                center.getX() + 0.5D,
                center.getY() + 0.5D,
                center.getZ() + 0.5D,

                100,

                2.0D,
                2.0D,
                2.0D,

                0.1D
        );


        serverLevel.sendParticles(
                ParticleTypes.ITEM_SNOWBALL,

                center.getX() + 0.5D,
                center.getY() + 0.5D,
                center.getZ() + 0.5D,

                40,

                1.5D,
                1.5D,
                1.5D,

                0.08D
        );


        // Снаряд исчезает
        discard();
    }

    // ============================================================
// ЛЕДЯНАЯ ОБЛАСТЬ ПРИ ПОПАДАНИИ
// ============================================================

    private void createIceExplosion(
            ServerLevel level,
            BlockPos center,
            int radius
    ) {

        int radiusSquared =
                radius * radius;


        for (int x = -radius; x <= radius; x++) {

            for (int y = -radius; y <= radius; y++) {

                for (int z = -radius; z <= radius; z++) {

                    // ------------------------------------------------
                    // ДЕЛАЕМ СФЕРУ, А НЕ КУБ
                    // ------------------------------------------------

                    int distanceSquared =
                            x * x
                                    + y * y
                                    + z * z;


                    if (distanceSquared > radiusSquared) {
                        continue;
                    }


                    BlockPos pos =
                            center.offset(
                                    x,
                                    y,
                                    z
                            );


                    BlockState state =
                            level.getBlockState(pos);


                    // ------------------------------------------------
                    // ВОЗДУХ -> ЛЁД
                    // ------------------------------------------------

                    if (state.isAir()) {

                        level.setBlockAndUpdate(
                                pos,
                                Blocks.ICE.defaultBlockState()
                        );
                    }
                }
            }
        }
    }
}