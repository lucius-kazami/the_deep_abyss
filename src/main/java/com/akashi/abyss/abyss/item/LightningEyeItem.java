package com.akashi.abyss.abyss.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LightningEyeItem extends Item {

    /*
     * Дальность электрического луча.
     */
    private static final double RANGE = 30.0D;

    /*
     * Урон каждой цели.
     */
    private static final float DAMAGE = 8.0F;

    /*
     * Расстояние между частицами луча.
     * Чем меньше число — тем плотнее луч.
     */
    private static final double STEP = 0.35D;

    /*
     * Радиус попадания луча по существам.
     */
    private static final double HIT_RADIUS = 1.0D;


    public LightningEyeItem(Properties properties) {
        super(properties);
    }


    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {

        ItemStack stack = player.getItemInHand(hand);

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }


        /*
         * ========================================================
         * НАЧАЛО ЛУЧА
         * ========================================================
         */

        Vec3 start = player.getEyePosition();

        Vec3 direction =
                player.getLookAngle().normalize();

        Vec3 maximumEnd =
                start.add(
                        direction.scale(RANGE)
                );


        /*
         * ========================================================
         * ПРОВЕРКА БЛОКОВ
         * ========================================================
         *
         * Луч не должен проходить через стены.
         */

        HitResult blockHit =
                level.clip(
                        new net.minecraft.world.level.ClipContext(
                                start,
                                maximumEnd,
                                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                                net.minecraft.world.level.ClipContext.Fluid.NONE,
                                player
                        )
                );


        Vec3 end;

        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
        } else {
            end = maximumEnd;
        }


        /*
         * ========================================================
         * РИСУЕМ ЭЛЕКТРИЧЕСКИЙ ЛУЧ
         * ========================================================
         */

        double distance =
                start.distanceTo(end);

        int steps =
                Math.max(
                        1,
                        (int) (distance / STEP)
                );


        Set<LivingEntity> damagedTargets =
                new HashSet<>();


        for (int i = 0; i <= steps; i++) {

            double progress =
                    i / (double) steps;


            Vec3 point =
                    start.lerp(
                            end,
                            progress
                    );


            /*
             * Основная электрическая частица.
             */
            serverLevel.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,

                    point.x,
                    point.y,
                    point.z,

                    2,

                    0.08,
                    0.08,
                    0.08,

                    0.02
            );


            /*
             * Небольшая дополнительная вспышка,
             * чтобы луч выглядел плотнее.
             */
            if (i % 3 == 0) {

                serverLevel.sendParticles(
                        ParticleTypes.END_ROD,

                        point.x,
                        point.y,
                        point.z,

                        1,

                        0.04,
                        0.04,
                        0.04,

                        0.0
                );
            }


            /*
             * ====================================================
             * ИЩЕМ СУЩЕСТВ РЯДОМ С ТОЧКОЙ ЛУЧА
             * ====================================================
             */

            AABB hitBox =
                    new AABB(
                            point.x - HIT_RADIUS,
                            point.y - HIT_RADIUS,
                            point.z - HIT_RADIUS,

                            point.x + HIT_RADIUS,
                            point.y + HIT_RADIUS,
                            point.z + HIT_RADIUS
                    );


            List<LivingEntity> targets =
                    serverLevel.getEntitiesOfClass(
                            LivingEntity.class,
                            hitBox,
                            entity ->
                                    entity != player
                                            && entity.isAlive()
                    );


            for (LivingEntity target : targets) {

                /*
                 * Одна цель получает урон только один раз
                 * за одно применение Глаза.
                 */
                if (!damagedTargets.add(target)) {
                    continue;
                }


                target.hurtServer(
                        serverLevel,
                        serverLevel.damageSources().lightningBolt(),
                        DAMAGE
                );


                /*
                 * Электрическая вспышка непосредственно
                 * на поражённой цели.
                 */
                serverLevel.sendParticles(
                        ParticleTypes.ELECTRIC_SPARK,

                        target.getX(),
                        target.getY() + target.getBbHeight() / 2.0,
                        target.getZ(),

                        30,

                        0.4,
                        0.7,
                        0.4,

                        0.15
                );
            }
        }


        /*
         * ========================================================
         * ЗВУК
         * ========================================================
         */

        serverLevel.playSound(
                null,

                player.getX(),
                player.getY(),
                player.getZ(),

                SoundEvents.LIGHTNING_BOLT_IMPACT,
                SoundSource.PLAYERS,

                1.5F,
                1.4F
        );


        /*
         * ========================================================
         * ПРОЧНОСТЬ
         * ========================================================
         */

        stack.hurtAndBreak(
                1,
                player,

                hand == InteractionHand.MAIN_HAND
                        ? net.minecraft.world.entity.EquipmentSlot.MAINHAND
                        : net.minecraft.world.entity.EquipmentSlot.OFFHAND
        );


        /*
         * ========================================================
         * ПЕРЕЗАРЯДКА
         * ========================================================
         *
         * 20 тиков = 1 секунда.
         */

        player.getCooldowns().addCooldown(
                stack,
                20
        );


        return InteractionResult.SUCCESS;
    }
}