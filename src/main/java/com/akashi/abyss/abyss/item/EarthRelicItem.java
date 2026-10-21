package com.akashi.abyss.abyss.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EarthRelicItem extends Item {
    public EarthRelicItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 400, 1));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 1));
            player.getCooldowns().addCooldown(stack, 1200);
        }
        return InteractionResult.SUCCESS;
    }
}