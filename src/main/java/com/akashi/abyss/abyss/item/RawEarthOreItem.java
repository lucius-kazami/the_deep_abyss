package com.akashi.abyss.abyss.item;

import com.akashi.abyss.abyss.entity.EarthStoneProjectile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RawEarthOreItem extends Item {

    private static final int COOLDOWN_TICKS = 20; // 1 секунда
    private static final float DAMAGE = 10.0F;
    private static final float VELOCITY = 1.8F;
    private static final float INACCURACY = 0.5F;

    public RawEarthOreItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);

        // Проверяем перезарядку
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        if (level instanceof ServerLevel serverLevel) {

            // Создаём камень
            EarthStoneProjectile stone =
                    new EarthStoneProjectile(serverLevel, player);

            // Устанавливаем урон
            stone.setDamage(DAMAGE);

            // Стреляем туда, куда смотрит игрок
            stone.shootFromRotation(
                    player,
                    player.getXRot(),
                    player.getYRot(),
                    0.0F,
                    VELOCITY,
                    INACCURACY
            );

            // Добавляем projectile в мир
            serverLevel.addFreshEntity(stone);

            // Звук броска
            serverLevel.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.SNOWBALL_THROW,
                    SoundSource.PLAYERS,
                    1.0F,
                    0.7F
            );
        }

        // Перезарядка
        player.getCooldowns().addCooldown(
                stack,
                COOLDOWN_TICKS
        );

        return InteractionResult.SUCCESS;
    }
}