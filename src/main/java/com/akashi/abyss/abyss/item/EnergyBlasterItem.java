package com.akashi.abyss.abyss.item;

import com.akashi.abyss.abyss.Abyss;
import com.akashi.abyss.abyss.entity.EnergyBlastProjectile;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EnergyBlasterItem extends Item {

    // ============================================================
    // НАСТРОЙКИ
    // ============================================================

    private static final float PROJECTILE_SPEED = 4.0F;

    // 4 тика = примерно 5 выстрелов в секунду
    private static final int FIRE_INTERVAL = 4;

    // Максимальное время удержания ПКМ
    private static final int USE_DURATION = 72000;

    public EnergyBlasterItem(Properties properties) {
        super(
                properties
                        .stacksTo(1)
                        .rarity(Rarity.EPIC)
                        .component(
                                DataComponents.UNBREAKABLE,
                                Unit.INSTANCE
                        )
        );
    }


    // ============================================================
    // НАЖАТИЕ ПКМ
    // ============================================================

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {

        // Первый выстрел сразу
        if (level instanceof ServerLevel serverLevel) {
            shoot(
                    serverLevel,
                    player
            );
        }

        // После первого выстрела начинаем удержание
        player.startUsingItem(hand);

        return InteractionResult.SUCCESS;
    }


    // ============================================================
    // ВРЕМЯ УДЕРЖАНИЯ
    // ============================================================

    @Override
    public int getUseDuration(
            ItemStack stack,
            LivingEntity entity
    ) {
        return USE_DURATION;
    }


    // ============================================================
    // АНИМАЦИЯ
    // ============================================================

    @Override
    public ItemUseAnimation getUseAnimation(
            ItemStack stack
    ) {
        return ItemUseAnimation.NONE;
    }


    // ============================================================
    // АВТОМАТИЧЕСКАЯ СТРЕЛЬБА
    // ============================================================

    @Override
    public void onUseTick(
            Level level,
            LivingEntity livingEntity,
            ItemStack stack,
            int remainingUseDuration
    ) {

        if (!(livingEntity instanceof Player player)) {
            return;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int usedTicks =
                USE_DURATION - remainingUseDuration;

        // Первый выстрел уже был сделан в use()
        if (usedTicks <= 0) {
            return;
        }

        // Новый выстрел каждые 4 тика
        if (usedTicks % FIRE_INTERVAL != 0) {
            return;
        }

        shoot(
                serverLevel,
                player
        );
    }


    // ============================================================
    // ВЫСТРЕЛ
    // ============================================================

    private void shoot(
            ServerLevel level,
            Player player
    ) {

        // ========================================================
        // СОЗДАЁМ ENERGY BLAST
        // ========================================================

        EnergyBlastProjectile projectile =
                new EnergyBlastProjectile(
                        Abyss.ENERGY_BLAST.get(),
                        player,
                        level
                );


        // ========================================================
        // НАПРАВЛЕНИЕ ВЫСТРЕЛА
        // ========================================================

        projectile.shootFromRotation(
                player,
                player.getXRot(),
                player.getYRot(),
                0.0F,
                PROJECTILE_SPEED,
                0.0F
        );


        // ========================================================
        // ДОБАВЛЯЕМ В МИР
        // ========================================================

        level.addFreshEntity(projectile);


        // ========================================================
        // ПОЗИЦИЯ ДУЛА
        // ========================================================

        Vec3 look =
                player.getLookAngle();

        double muzzleX =
                player.getX()
                        + look.x * 0.8D;

        double muzzleY =
                player.getEyeY()
                        - 0.15D
                        + look.y * 0.8D;

        double muzzleZ =
                player.getZ()
                        + look.z * 0.8D;


        // ========================================================
        // ЭЛЕКТРИЧЕСКАЯ ВСПЫШКА
        // ========================================================

        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                muzzleX,
                muzzleY,
                muzzleZ,
                8,
                0.08D,
                0.08D,
                0.08D,
                0.08D
        );


        // ========================================================
        // СВЕТЯЩАЯСЯ ЭНЕРГИЯ
        // ========================================================

        level.sendParticles(
                ParticleTypes.END_ROD,
                muzzleX,
                muzzleY,
                muzzleZ,
                4,
                0.05D,
                0.05D,
                0.05D,
                0.03D
        );


        // ========================================================
        // ЗВУК
        // ========================================================

        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.BEACON_POWER_SELECT,
                SoundSource.PLAYERS,
                0.8F,
                1.8F
        );
    }
}