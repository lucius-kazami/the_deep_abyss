package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = Abyss.MODID)
public class LightningBowEvents {

    /*
     * Дополнительный урон основной цели.
     */
    private static final float LIGHTNING_BONUS = 10.0F;

    /*
     * Урон существам рядом с основной целью.
     */
    private static final float CHAIN_DAMAGE = 5.0F;

    /*
     * Радиус цепной молнии.
     */
    private static final double CHAIN_RADIUS = 5.0D;

    /*
     * Максимальное количество дополнительных целей.
     */
    private static final int MAX_CHAIN_TARGETS = 4;

    /*
     * Защита от повторного запуска события,
     * когда мы сами наносим электрический урон.
     */
    private static boolean inLightningAttack = false;


    @SubscribeEvent
    public static void onArrowHit(
            LivingIncomingDamageEvent event
    ) {

        /*
         * Не запускаем способность повторно от собственного
         * электрического урона.
         */
        if (inLightningAttack) {
            return;
        }


        // ========================================================
        // ПРОВЕРЯЕМ, ЧТО УРОН НАНЕСЛА СТРЕЛА
        // ========================================================

        DamageSource source = event.getSource();

        if (!(source.getDirectEntity()
                instanceof AbstractArrow arrow)) {

            return;
        }


        // ========================================================
        // ПРОВЕРЯЕМ ЛУК, ИЗ КОТОРОГО ВЫПУЩЕНА СТРЕЛА
        // ========================================================

        ItemStack weapon =
                arrow.getWeaponItem();

        if (weapon == null
                || !weapon.is(Abyss.LIGHTNING_BOW.get())) {

            return;
        }


        // ========================================================
        // ОСНОВНАЯ ЦЕЛЬ
        // ========================================================

        LivingEntity victim =
                event.getEntity();

        if (!(victim.level()
                instanceof ServerLevel server)) {

            return;
        }


        /*
         * Добавляем электрический урон к обычному урону стрелы.
         */
        event.setAmount(
                event.getAmount()
                        + LIGHTNING_BONUS
        );


        // ========================================================
        // ВЛАДЕЛЕЦ СТРЕЛЫ
        // ========================================================

        Entity owner =
                arrow.getOwner();


        // ========================================================
        // ЭЛЕКТРИЧЕСКИЙ УДАР ПО ОСНОВНОЙ ЦЕЛИ
        // ========================================================

        createVisualLightning(
                server,
                victim.getX(),
                victim.getY(),
                victim.getZ()
        );


        server.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,

                victim.getX(),
                victim.getY()
                        + victim.getBbHeight() / 2.0,
                victim.getZ(),

                45,

                0.6,
                0.9,
                0.6,

                0.18
        );


        // ========================================================
        // ЦЕПНАЯ МОЛНИЯ
        // ========================================================

        Vec3 origin =
                victim.getBoundingBox()
                        .getCenter();


        AABB area =
                new AABB(
                        origin,
                        origin
                ).inflate(CHAIN_RADIUS);


        /*
         * Источник урона для дополнительных целей.
         */
        DamageSource lightningSource =
                server.damageSources()
                        .lightningBolt();


        int hitCount = 0;


        inLightningAttack = true;

        try {

            for (LivingEntity target :
                    server.getEntitiesOfClass(
                            LivingEntity.class,
                            area,

                            entity ->
                                    entity != victim
                                            && entity != owner
                                            && entity.isAlive()
                                            && entity.distanceToSqr(origin)
                                            <= CHAIN_RADIUS
                                            * CHAIN_RADIUS
                    )) {


                if (hitCount >= MAX_CHAIN_TARGETS) {
                    break;
                }


                // ================================================
                // РИСУЕМ ЭЛЕКТРИЧЕСКУЮ ДУГУ
                // ================================================

                drawLightningArc(
                        server,
                        origin,
                        target.getBoundingBox()
                                .getCenter()
                );


                // ================================================
                // УРОН
                // ================================================

                target.hurtServer(
                        server,
                        lightningSource,
                        CHAIN_DAMAGE
                );


                // ================================================
                // МОЛНИЯ НА ДОПОЛНИТЕЛЬНОЙ ЦЕЛИ
                // ================================================

                createVisualLightning(
                        server,
                        target.getX(),
                        target.getY(),
                        target.getZ()
                );


                server.sendParticles(
                        ParticleTypes.ELECTRIC_SPARK,

                        target.getX(),
                        target.getY()
                                + target.getBbHeight() / 2.0,
                        target.getZ(),

                        25,

                        0.4,
                        0.7,
                        0.4,

                        0.15
                );


                hitCount++;
            }

        } finally {

            inLightningAttack = false;
        }


        // ========================================================
        // ЗВУК
        // ========================================================

        server.playSound(
                null,

                victim.blockPosition(),

                SoundEvents.LIGHTNING_BOLT_IMPACT,
                SoundSource.PLAYERS,

                1.2F,
                1.3F
        );
    }


    // ============================================================
    // ЭЛЕКТРИЧЕСКАЯ ДУГА МЕЖДУ ДВУМЯ ЦЕЛЯМИ
    // ============================================================

    private static void drawLightningArc(
            ServerLevel level,
            Vec3 start,
            Vec3 end
    ) {

        Vec3 difference =
                end.subtract(start);

        double distance =
                difference.length();


        if (distance <= 0.0D) {
            return;
        }


        Vec3 direction =
                difference.normalize();


        /*
         * Частица примерно каждые 0.25 блока.
         */
        double step = 0.25D;


        for (double d = 0.0D;
             d <= distance;
             d += step) {


            Vec3 point =
                    start.add(
                            direction.scale(d)
                    );


            /*
             * Небольшое случайное отклонение создаёт
             * эффект настоящей неровной молнии.
             */

            double offsetX =
                    (level.random.nextDouble() - 0.5D)
                            * 0.20D;

            double offsetY =
                    (level.random.nextDouble() - 0.5D)
                            * 0.20D;

            double offsetZ =
                    (level.random.nextDouble() - 0.5D)
                            * 0.20D;


            level.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,

                    point.x + offsetX,
                    point.y + offsetY,
                    point.z + offsetZ,

                    1,

                    0.02,
                    0.02,
                    0.02,

                    0.01
            );
        }
    }


    // ============================================================
    // ВИЗУАЛЬНАЯ МОЛНИЯ
    // ============================================================

    private static void createVisualLightning(
            ServerLevel level,
            double x,
            double y,
            double z
    ) {

        LightningBolt lightning =
                new LightningBolt(
                        EntityType.LIGHTNING_BOLT,
                        level
                );


        lightning.setPos(
                x,
                y,
                z
        );


        /*
         * Ванильная молния используется только визуально.
         *
         * Урон наносится нашим кодом.
         */
        lightning.setVisualOnly(true);


        level.addFreshEntity(
                lightning
        );
    }
}