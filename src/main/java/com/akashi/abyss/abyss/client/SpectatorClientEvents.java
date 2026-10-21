package com.akashi.abyss.abyss.client;

import com.akashi.abyss.abyss.Abyss;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;


public class SpectatorClientEvents {

    // ============================================================
    // НАСТРОЙКИ
    // ============================================================

    /**
     * Скорость движения вперёд / назад / в стороны.
     */
    private static final double SPECTRAL_SPEED = 0.30D;

    /**
     * Скорость движения вверх / вниз.
     */
    private static final double SPECTRAL_VERTICAL_SPEED = 0.30D;


    // ============================================================
    // ПРОВЕРКА SPECTATOR CHESTPLATE
    // ============================================================

    private static boolean hasSpectatorChestplate(Player player) {

        ItemStack chest =
                player.getItemBySlot(EquipmentSlot.CHEST);

        return chest.is(
                Abyss.SPECTATOR_CHESTPLATE.get()
        );
    }


    // ============================================================
    // CLIENT PLAYER TICK
    // ============================================================

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {

        // Работаем только на клиенте
        if (!event.getEntity().level().isClientSide()) {
            return;
        }


        Minecraft minecraft =
                Minecraft.getInstance();


        LocalPlayer player =
                minecraft.player;


        if (player == null) {
            return;
        }


        // Обрабатываем только локального игрока
        if (event.getEntity() != player) {
            return;
        }


        boolean spectral =
                hasSpectatorChestplate(player);


        // ========================================================
        // SPECTATOR CHESTPLATE НЕ НАДЕТ
        // ========================================================

        if (!spectral) {

            player.noPhysics = false;


            // Если игрок не Creative и не настоящий Spectator,
            // отключаем полёт.
            if (!player.isCreative()
                    && !player.isSpectator()) {

                player.getAbilities().flying = false;
                player.getAbilities().mayfly = false;
            }


            return;
        }


        // ========================================================
        // SPECTRAL MODE
        // ========================================================

        /*
         * Отключаем обычную физику Entity.
         */
        player.noPhysics = true;


        /*
         * Не получаем урон от падения.
         */
        player.fallDistance = 0.0F;


        /*
         * Игрок не считается стоящим на земле.
         */
        player.setOnGround(false);


        // ========================================================
        // РАЗРЕШАЕМ ПОЛЁТ
        // ========================================================

        player.getAbilities().mayfly = true;

        player.getAbilities().flying = true;

        player.getAbilities().setFlyingSpeed(0.10F);


        // ========================================================
        // НАПРАВЛЕНИЕ КАМЕРЫ
        // ========================================================

        Vec3 look =
                player.getLookAngle();


        /*
         * Нам нужна горизонтальная часть направления камеры.
         *
         * Y = 0, потому что W/S должны двигать
         * вперёд/назад горизонтально.
         *
         * Для вертикального движения используются
         * прыжок и приседание.
         */
        Vec3 forward =
                new Vec3(
                        look.x,
                        0.0D,
                        look.z
                );


        // Нормализуем направление
        if (forward.lengthSqr() > 0.0001D) {

            forward =
                    forward.normalize();
        }


        // ========================================================
        // НАПРАВЛЕНИЕ ВПРАВО
        // ========================================================

        Vec3 right =
                new Vec3(
                        -forward.z,
                        0.0D,
                        forward.x
                );


        // ========================================================
        // INPUT
        // ========================================================

        double forwardInput = 0.0D;

        double sideInput = 0.0D;

        double verticalInput = 0.0D;


        // --------------------------------------------------------
        // ВПЕРЁД
        // --------------------------------------------------------

        if (minecraft.options.keyUp.isDown()) {

            forwardInput += 1.0D;
        }


        // --------------------------------------------------------
        // НАЗАД
        // --------------------------------------------------------

        if (minecraft.options.keyDown.isDown()) {

            forwardInput -= 1.0D;
        }


        // --------------------------------------------------------
        // ВЛЕВО
        // --------------------------------------------------------

        if (minecraft.options.keyLeft.isDown()) {

            sideInput -= 1.0D;
        }


        // --------------------------------------------------------
        // ВПРАВО
        // --------------------------------------------------------

        if (minecraft.options.keyRight.isDown()) {

            sideInput += 1.0D;
        }


        // --------------------------------------------------------
        // ВВЕРХ
        // --------------------------------------------------------

        if (minecraft.options.keyJump.isDown()) {

            verticalInput += 1.0D;
        }


        // --------------------------------------------------------
        // ВНИЗ
        // --------------------------------------------------------

        if (minecraft.options.keyShift.isDown()) {

            verticalInput -= 1.0D;
        }


        // ========================================================
        // ГОРИЗОНТАЛЬНОЕ ДВИЖЕНИЕ
        // ========================================================

        Vec3 horizontal =
                forward
                        .scale(forwardInput)
                        .add(
                                right.scale(sideInput)
                        );


        /*
         * Не позволяем диагональному движению
         * быть быстрее обычного.
         */
        if (horizontal.lengthSqr() > 1.0D) {

            horizontal =
                    horizontal.normalize();
        }


        // ========================================================
        // ФИНАЛЬНАЯ СКОРОСТЬ
        // ========================================================

        double moveX =
                horizontal.x * SPECTRAL_SPEED;


        double moveY =
                verticalInput
                        * SPECTRAL_VERTICAL_SPEED;


        double moveZ =
                horizontal.z * SPECTRAL_SPEED;


        // ========================================================
        // NOCLIP MOVEMENT
        // ========================================================

        /*
         * ВАЖНО:
         *
         * Здесь специально НЕ используется:
         *
         * player.move(...)
         *
         * потому что move() использует обычную
         * collision систему Minecraft.
         *
         * Вместо этого напрямую меняем позицию.
         */

        player.setPos(
                player.getX() + moveX,
                player.getY() + moveY,
                player.getZ() + moveZ
        );


        // ========================================================
        // УБИРАЕМ VANILLA MOVEMENT
        // ========================================================

        /*
         * Чтобы Minecraft дополнительно не пытался
         * перемещать игрока своей физикой.
         */
        player.setDeltaMovement(
                Vec3.ZERO
        );


        // Ещё раз гарантируем отсутствие падения
        player.fallDistance = 0.0F;
    }


    // ============================================================
    // СКРЫТИЕ ИГРОКА
    // ============================================================

    @SubscribeEvent
    public static void onRenderPlayer(
            RenderPlayerEvent.Pre event
    ) {

        Minecraft minecraft =
                Minecraft.getInstance();


        LocalPlayer player =
                minecraft.player;


        if (player == null) {
            return;
        }


        /*
         * Если Spectator Chestplate не надет,
         * ничего не скрываем.
         */
        if (!hasSpectatorChestplate(player)) {
            return;
        }


        /*
         * Полностью отменяем рендер Player.
         *
         * Поэтому не отображаются:
         *
         * - тело
         * - кожа
         * - Spectator Chestplate
         * - модель игрока
         */
        event.setCanceled(true);
    }
}