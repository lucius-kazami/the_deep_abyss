package com.akashi.abyss.abyss.item;

import com.akashi.abyss.abyss.Abyss;
import com.akashi.abyss.abyss.entity.BlackHoleEntity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BlackHoleStaffItem extends Item {

    // ============================================================
    // COOLDOWN
    // ============================================================

    // Обычная атака — 5 секунд
    private static final int NORMAL_COOLDOWN =
            20 * 5;

    // Великая сингулярность — 15 секунд
    private static final int SUPER_COOLDOWN =
            20 * 15;


    // ============================================================
    // ДИСТАНЦИЯ ПОЯВЛЕНИЯ
    // ============================================================

    private static final double NORMAL_DISTANCE =
            8.0D;

    private static final double SUPER_DISTANCE =
            10.0D;


    public BlackHoleStaffItem(
            Properties properties
    ) {

        super(properties);
    }


    // ============================================================
    // USE
    // ============================================================

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {

        ItemStack stack =
                player.getItemInHand(hand);


        if (!(level instanceof ServerLevel serverLevel)) {

            return InteractionResult.SUCCESS;
        }


        // ========================================================
        // SHIFT + ПКМ
        //
        // ВЕЛИКАЯ СИНГУЛЯРНОСТЬ
        // ========================================================

        if (player.isShiftKeyDown()) {

            spawnBlackHole(
                    serverLevel,
                    player,
                    true,
                    SUPER_DISTANCE
            );


            player.getCooldowns().addCooldown(
                    stack,
                    SUPER_COOLDOWN
            );


            return InteractionResult.SUCCESS;
        }


        // ========================================================
        // ОБЫЧНЫЙ ПКМ
        //
        // ОБЫЧНАЯ ЧЁРНАЯ ДЫРА
        // ========================================================

        spawnBlackHole(
                serverLevel,
                player,
                false,
                NORMAL_DISTANCE
        );


        player.getCooldowns().addCooldown(
                stack,
                NORMAL_COOLDOWN
        );


        return InteractionResult.SUCCESS;
    }


    // ============================================================
    // СОЗДАНИЕ ЧЁРНОЙ ДЫРЫ
    // ============================================================

    private void spawnBlackHole(
            ServerLevel serverLevel,
            Player player,
            boolean superBlackHole,
            double distance
    ) {

        Vec3 look =
                player.getLookAngle();


        Vec3 position =
                player.getEyePosition()
                        .add(
                                look.scale(distance)
                        );


        BlackHoleEntity blackHole =
                new BlackHoleEntity(
                        Abyss.BLACK_HOLE_ENTITY.get(),
                        serverLevel
                );


        blackHole.setOwner(
                player
        );


        blackHole.setSuperBlackHole(
                superBlackHole
        );


        blackHole.setPos(
                position.x,
                position.y,
                position.z
        );


        serverLevel.addFreshEntity(
                blackHole
        );


        // ========================================================
        // ЗВУК
        // ========================================================

        serverLevel.playSound(
                null,

                position.x,
                position.y,
                position.z,

                SoundEvents.END_PORTAL_SPAWN,

                SoundSource.PLAYERS,

                superBlackHole
                        ? 2.0F
                        : 1.0F,

                superBlackHole
                        ? 0.25F
                        : 0.5F
        );
    }
}