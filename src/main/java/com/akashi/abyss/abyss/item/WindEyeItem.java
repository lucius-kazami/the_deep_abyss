package com.akashi.abyss.abyss.item;

import com.akashi.abyss.abyss.util.AbyssRay;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WindEyeItem extends Item {

    private static final int COOLDOWN_TICKS = 40;
    private static final double RANGE = 18.0;
    private static final float DAMAGE = 15.0F;

    public WindEyeItem(Properties props) {
        super(props);
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

            // Луч ветра
            AbyssRay.Result ray =
                    AbyssRay.trace(serverLevel, player, RANGE);

            // Линия из частиц ветра
            AbyssRay.drawLine(
                    serverLevel,
                    ray.start(),
                    ray.end(),
                    ParticleTypes.GUST
            );

            // Облако в конце луча
            serverLevel.sendParticles(
                    ParticleTypes.CLOUD,
                    ray.end().x,
                    ray.end().y,
                    ray.end().z,
                    25,
                    1.2,
                    1.2,
                    1.2,
                    0.02
            );

            // Урон + отбрасывание
            for (Entity target : ray.entities()) {

                target.hurtServer(
                        serverLevel,
                        serverLevel.damageSources().playerAttack(player),
                        DAMAGE
                );

                double dx = target.getX() - player.getX();
                double dz = target.getZ() - player.getZ();

                double dist = Math.max(
                        0.1,
                        Math.sqrt(dx * dx + dz * dz)
                );

                target.push(
                        dx / dist * 0.7,
                        0.3,
                        dz / dist * 0.7
                );
            }

            // Расход прочности
            if (stack.getMaxDamage() > 0) {
                int newDamage = stack.getDamageValue() + 1;

                if (newDamage >= stack.getMaxDamage()) {
                    stack.shrink(1);
                } else {
                    stack.setDamageValue(newDamage);
                }
            }
        }

        // 40 тиков = 2 секунды
        player.getCooldowns().addCooldown(
                stack,
                COOLDOWN_TICKS
        );

        return InteractionResult.SUCCESS;
    }
}