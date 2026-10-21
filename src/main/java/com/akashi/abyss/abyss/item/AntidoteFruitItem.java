package com.akashi.abyss.abyss.item;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AntidoteFruitItem extends Item {
    public AntidoteFruitItem(Properties props) {
        super(props);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (entity instanceof Player player) {
            player.removeEffect(MobEffects.POISON); // сразу снимает уже начавшийся яд
        }
        return result;
    }
}