package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = Abyss.MODID)
public class LightningGearEvents {

    // ============================================================
    // НАСТРОЙКИ
    // ============================================================

    /*
     * Радиус электризации воды.
     */
    private static final double WATER_RADIUS = 8.0D;

    /*
     * Урон существам в воде.
     */
    private static final float WATER_DAMAGE = 8.0F;

    /*
     * Ответный электрический урон тому,
     * кто ударил владельца брони.
     */
    private static final float RETALIATION_DAMAGE = 6.0F;

    /*
     * Скорость полёта брони молнии.
     *
     * Стандартная скорость = 0.05F.
     */
    private static final float LIGHTNING_FLY_SPEED = 0.08F;

    /*
     * Обычная скорость полёта.
     */
    private static final float NORMAL_FLY_SPEED = 0.05F;


    // ============================================================
    // ПРОВЕРКА ПОЛНОГО КОМПЛЕКТА
    // ============================================================

    private static boolean hasFullSet(Player player) {

        ItemStack helmet =
                player.getItemBySlot(EquipmentSlot.HEAD);

        ItemStack chest =
                player.getItemBySlot(EquipmentSlot.CHEST);

        ItemStack legs =
                player.getItemBySlot(EquipmentSlot.LEGS);

        ItemStack boots =
                player.getItemBySlot(EquipmentSlot.FEET);


        return helmet.is(Abyss.LIGHTNING_HELMET.get())
                && chest.is(Abyss.LIGHTNING_CHESTPLATE.get())
                && legs.is(Abyss.LIGHTNING_LEGGINGS.get())
                && boots.is(Abyss.LIGHTNING_BOOTS.get());
    }


    // ============================================================
    // ТИК ИГРОКА
    // ============================================================

    @SubscribeEvent
    public static void onPlayerTick(
            PlayerTickEvent.Post event
    ) {

        Player player = event.getEntity();


        /*
         * Всё выполняем только на сервере.
         */
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }


        ServerLevel level =
                (ServerLevel) serverPlayer.level();


        // ========================================================
        // ПРОВЕРЯЕМ ПОЛНЫЙ КОМПЛЕКТ
        // ========================================================

        if (!hasFullSet(player)) {

            removeLightningFlight(serverPlayer);

            return;
        }


        // ========================================================
        // SPEED III
        // ========================================================

        /*
         * amplifier:
         *
         * 0 = Speed I
         * 1 = Speed II
         * 2 = Speed III
         */

        player.addEffect(
                new MobEffectInstance(
                        MobEffects.SPEED,
                        40,
                        2,
                        false,
                        false,
                        true
                )
        );


        // ========================================================
        // ЭЛЕКТРИЧЕСКИЙ СЛЕД ПРИ БЕГЕ
        // ========================================================

        if (!player.getAbilities().flying
                && player.onGround()
                && player.getDeltaMovement()
                .horizontalDistanceSqr() > 0.05D
                && player.tickCount % 3 == 0) {

            level.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,

                    player.getX(),
                    player.getY() + 0.1,
                    player.getZ(),

                    5,

                    0.25,
                    0.05,
                    0.25,

                    0.05
            );
        }


        // ========================================================
        // ПОЛЁТ ВО ВРЕМЯ ДОЖДЯ
        // ========================================================

        /*
         * isRainingAt означает, что дождь действительно
         * попадает на позицию игрока.
         *
         * Под крышей полёт может отключиться.
         */

        boolean raining =
                level.isRainingAt(
                        player.blockPosition()
                );


        if (raining) {

            /*
             * Разрешаем полёт.
             */
            if (!serverPlayer.getAbilities().mayfly) {

                serverPlayer.getAbilities().mayfly = true;

                serverPlayer.onUpdateAbilities();
            }


            /*
             * Увеличиваем скорость полёта.
             */
            if (serverPlayer.getAbilities().getFlyingSpeed()
                    != LIGHTNING_FLY_SPEED) {

                serverPlayer.getAbilities()
                        .setFlyingSpeed(
                                LIGHTNING_FLY_SPEED
                        );

                serverPlayer.onUpdateAbilities();
            }


            // ====================================================
            // ЭЛЕКТРИЧЕСКИЙ СЛЕД ВО ВРЕМЯ ПОЛЁТА
            // ====================================================

            if (serverPlayer.getAbilities().flying) {

                /*
                 * Постоянный электрический след.
                 */

                level.sendParticles(
                        ParticleTypes.ELECTRIC_SPARK,

                        player.getX(),
                        player.getY() + 0.5,
                        player.getZ(),

                        8,

                        0.35,
                        0.35,
                        0.35,

                        0.08
                );


                /*
                 * Более мощная вспышка каждые 5 тиков.
                 */

                if (player.tickCount % 5 == 0) {

                    level.sendParticles(
                            ParticleTypes.ELECTRIC_SPARK,

                            player.getX(),
                            player.getY() + 1.0,
                            player.getZ(),

                            18,

                            0.6,
                            0.8,
                            0.6,

                            0.15
                    );
                }
            }

        } else {

            /*
             * Дождь закончился —
             * забираем способность летать.
             */

            removeLightningFlight(
                    serverPlayer
            );
        }


        // ========================================================
        // ЭЛЕКТРИЗАЦИЯ ВОДЫ
        // ========================================================

        /*
         * Проверяем раз в 20 тиков.
         *
         * 20 тиков ~= 1 секунда.
         */

        if (player.tickCount % 20 == 0
                && player.isInWater()) {

            electrifyWater(
                    level,
                    serverPlayer
            );
        }
    }


    // ============================================================
    // ОТКЛЮЧЕНИЕ ПОЛЁТА
    // ============================================================

    private static void removeLightningFlight(
            ServerPlayer player
    ) {

        /*
         * Creative и Spectator не трогаем,
         * потому что у них собственный полёт.
         */

        if (player.isCreative()
                || player.isSpectator()) {

            return;
        }


        boolean changed = false;


        /*
         * Убираем возможность полёта.
         */

        if (player.getAbilities().mayfly) {

            player.getAbilities().mayfly = false;

            player.getAbilities().flying = false;

            changed = true;
        }


        /*
         * Возвращаем обычную скорость полёта.
         */

        if (player.getAbilities().getFlyingSpeed()
                != NORMAL_FLY_SPEED) {

            player.getAbilities()
                    .setFlyingSpeed(
                            NORMAL_FLY_SPEED
                    );

            changed = true;
        }


        /*
         * Отправляем изменения клиенту.
         */

        if (changed) {

            player.onUpdateAbilities();
        }
    }


    // ============================================================
    // ЭЛЕКТРИЗАЦИЯ ВОДЫ
    // ============================================================

    private static void electrifyWater(
            ServerLevel level,
            ServerPlayer player
    ) {

        /*
         * Создаём область вокруг игрока.
         */

        AABB area =
                player.getBoundingBox()
                        .inflate(WATER_RADIUS);


        /*
         * Получаем всех живых существ
         * в радиусе.
         */

        List<LivingEntity> targets =
                level.getEntitiesOfClass(
                        LivingEntity.class,
                        area,

                        entity ->
                                entity != player
                                        && entity.isAlive()
                );


        for (LivingEntity target : targets) {

            /*
             * Электричество действует только
             * на существ, которые тоже находятся в воде.
             */

            if (!target.isInWater()) {
                continue;
            }


            // ====================================================
            // УРОН
            // ====================================================

            target.hurtServer(
                    level,
                    level.damageSources()
                            .lightningBolt(),
                    WATER_DAMAGE
            );


            // ====================================================
            // ВИЗУАЛЬНАЯ МОЛНИЯ
            // ====================================================

            createVisualLightning(
                    level,

                    target.getX(),
                    target.getY(),
                    target.getZ()
            );


            // ====================================================
            // ЧАСТИЦЫ НА ЦЕЛИ
            // ====================================================

            level.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,

                    target.getX(),
                    target.getY()
                            + target.getBbHeight() / 2.0,
                    target.getZ(),

                    25,

                    0.5,
                    0.6,
                    0.5,

                    0.15
            );
        }


        // ========================================================
        // ЭЛЕКТРИЧЕСКАЯ ВСПЫШКА ВОКРУГ ИГРОКА
        // ========================================================

        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,

                player.getX(),
                player.getY() + 1.0,
                player.getZ(),

                35,

                2.5,
                1.0,
                2.5,

                0.15
        );
    }


    // ============================================================
    // ПОЛУЧЕНИЕ УРОНА
    // ============================================================

    @SubscribeEvent
    public static void onDamage(
            LivingIncomingDamageEvent event
    ) {

        /*
         * Нас интересует только игрок.
         */

        if (!(event.getEntity()
                instanceof Player player)) {

            return;
        }


        /*
         * Способности работают только
         * с полным комплектом.
         */

        if (!hasFullSet(player)) {
            return;
        }


        /*
         * Только сервер.
         */

        if (!(player.level()
                instanceof ServerLevel level)) {

            return;
        }


        // ========================================================
        // ПОЛНЫЙ ИММУНИТЕТ К МОЛНИИ
        // ========================================================

        if (event.getSource()
                .is(DamageTypeTags.IS_LIGHTNING)) {

            event.setCanceled(true);

            /*
             * Небольшой эффект поглощения.
             */

            level.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,

                    player.getX(),
                    player.getY() + 1.0,
                    player.getZ(),

                    30,

                    0.5,
                    0.8,
                    0.5,

                    0.12
            );

            return;
        }


        // ========================================================
        // НЕ ДЕЛАЕМ ДВОЙНОЙ РАЗРЯД СО ЩИТОМ
        // ========================================================

        /*
         * Если игрок сейчас блокирует щитом молнии,
         * контр-атаку обрабатывает LightningShieldEvents.
         */

        if (player.isUsingItem()
                && player.getUseItem()
                .is(Abyss.LIGHTNING_SHIELD.get())) {

            return;
        }


        // ========================================================
        // ОТВЕТНЫЙ УДАР
        // ========================================================

        if (event.getSource().getEntity()
                instanceof LivingEntity attacker) {


            /*
             * Защита от удара самого себя.
             */

            if (attacker == player) {
                return;
            }


            /*
             * Электрический урон атакующему.
             */

            attacker.hurtServer(
                    level,
                    level.damageSources()
                            .lightningBolt(),
                    RETALIATION_DAMAGE
            );


            /*
             * Визуальная молния.
             */

            createVisualLightning(
                    level,

                    attacker.getX(),
                    attacker.getY(),
                    attacker.getZ()
            );


            /*
             * Электрические частицы.
             */

            level.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,

                    attacker.getX(),
                    attacker.getY()
                            + attacker.getBbHeight() / 2.0,
                    attacker.getZ(),

                    25,

                    0.5,
                    1.0,
                    0.5,

                    0.15
            );
        }
    }


    // ============================================================
    // СОЗДАНИЕ ВИЗУАЛЬНОЙ МОЛНИИ
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
         * Только визуальная молния.
         *
         * Она сама не наносит урон и не должна
         * поджигать окружающие блоки.
         */

        lightning.setVisualOnly(true);


        level.addFreshEntity(
                lightning
        );
    }
}