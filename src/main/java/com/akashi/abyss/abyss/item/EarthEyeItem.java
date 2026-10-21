package com.akashi.abyss.abyss.item;

import com.akashi.abyss.abyss.entity.EarthStoneProjectile;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EarthEyeItem extends Item {
    public EarthEyeItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            EarthStoneProjectile stone = new EarthStoneProjectile(level, player);
            stone.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.8F, 1.0F);
            level.addFreshEntity(stone);
            level.playSound(null, player.blockPosition(), SoundEvents.GRAVEL_BREAK,
                    SoundSource.PLAYERS, 1.0F, 0.8F);
            player.getCooldowns().addCooldown(stack, 20);
        }
        return InteractionResult.SUCCESS;
    }
}