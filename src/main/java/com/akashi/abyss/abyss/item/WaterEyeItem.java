package com.akashi.abyss.abyss.item;

import com.akashi.abyss.abyss.entity.WaterBladeProjectile;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WaterEyeItem extends Item {
    public WaterEyeItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            WaterBladeProjectile blade = new WaterBladeProjectile(level, player);
            blade.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.0F, 0.5F);
            level.addFreshEntity(blade);
            level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_SPLASH,
                    SoundSource.PLAYERS, 1.0F, 1.4F);
            player.getCooldowns().addCooldown(stack, 20);
        }
        return InteractionResult.SUCCESS;
    }
}
