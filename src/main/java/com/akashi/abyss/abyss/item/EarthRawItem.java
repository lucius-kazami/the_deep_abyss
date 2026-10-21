package com.akashi.abyss.abyss.item;

import com.akashi.abyss.abyss.Abyss;
import com.akashi.abyss.abyss.entity.EarthStoneProjectile;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Сырая руда земли: ПКМ выпускает камень. Одна руда даёт SHOTS_PER_ORE выстрелов,
 * счётчик хранится в компоненте Abyss.USES, потом руда исчезает.
 */
public class EarthRawItem extends Item {
    private static final int SHOTS_PER_ORE = 5;
    private static final float STONE_DAMAGE = 5.0F;
    private static final int COOLDOWN = 12;

    public EarthRawItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        EarthStoneProjectile stone = new EarthStoneProjectile(level, player);
        stone.setDamage(STONE_DAMAGE);
        stone.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.6F, 1.0F);
        level.addFreshEntity(stone);
        level.playSound(null, player.blockPosition(), SoundEvents.GRAVEL_BREAK,
                SoundSource.PLAYERS, 0.8F, 0.9F);

        if (!player.getAbilities().instabuild) {
            int used = stack.getOrDefault(Abyss.USES.get(), 0) + 1;
            if (used >= SHOTS_PER_ORE) {
                stack.shrink(1);                              // руда израсходована
            } else if (stack.getCount() > 1) {
                ItemStack charged = stack.split(1);           // отделяем одну руду со счётчиком
                charged.set(Abyss.USES.get(), used);
                if (!player.getInventory().add(charged)) {
                    player.drop(charged, false);
                }
            } else {
                stack.set(Abyss.USES.get(), used);
            }
        }

        player.getCooldowns().addCooldown(stack, COOLDOWN);
        return InteractionResult.CONSUME;
    }
}
