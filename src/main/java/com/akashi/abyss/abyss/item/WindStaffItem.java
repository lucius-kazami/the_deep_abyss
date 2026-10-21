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

public class WindStaffItem extends Item {

    private static final int COOLDOWN_TICKS = 20;
    private static final double RANGE = 12.0;
    private static final float DAMAGE = 12.0F;

    public WindStaffItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);

        // Если посох на перезарядке
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        if (level instanceof ServerLevel serverLevel) {

            // Пускаем луч
            AbyssRay.Result ray =
                    AbyssRay.trace(serverLevel, player, RANGE);

            // Рисуем эффект ветра
            AbyssRay.drawLine(
                    serverLevel,
                    ray.start(),
                    ray.end(),
                    ParticleTypes.SWEEP_ATTACK
            );

            // Наносим урон всем существам на пути
            for (Entity target : ray.entities()) {
                target.hurtServer(
                        serverLevel,
                        serverLevel.damageSources().playerAttack(player),
                        DAMAGE
                );
            }

            // Тратим прочность
            damageItem(stack, player);
        }

        // Перезарядка — 20 тиков = 1 секунда
        player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);

        return InteractionResult.SUCCESS;
    }

    private void damageItem(ItemStack stack, Player player) {
        if (stack.getMaxDamage() <= 0) {
            return;
        }

        int newDamage = stack.getDamageValue() + 1;

        if (newDamage >= stack.getMaxDamage()) {
            stack.shrink(1);
        } else {
            stack.setDamageValue(newDamage);
        }
    }
}