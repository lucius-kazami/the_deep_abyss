package com.akashi.abyss.abyss.item;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ElementCharge {
    private ElementCharge() {}

    public static int getUses(ItemStack stack) {
        return stack.getOrDefault(Abyss.USES.get(), 0);
    }

    /** Использованная (не целая) руда. */
    public static boolean isUsed(ItemStack stack) {
        return getUses(stack) > 0;
    }

    /** Тратит один заряд предмета в руке. Из стака отделяет одну руду, на последнем заряде руда ломается. */
    public static void consume(Player player, InteractionHand hand) {
        if (player.level().isClientSide() || player.hasInfiniteMaterials()) return;

        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) return;

        if (stack.getCount() > 1) {
            ItemStack single = stack.split(1);            // отделили одну из стака
            int uses = getUses(single) + 1;
            if (uses < RawElementItem.MAX_USES) {
                single.set(Abyss.USES.get(), uses);
                if (!player.getInventory().add(single)) {
                    player.drop(single, false);
                }
            }                                             // иначе руда сломалась и просто исчезла
        } else {
            int uses = getUses(stack) + 1;
            if (uses >= RawElementItem.MAX_USES) {
                stack.shrink(1);
            } else {
                stack.set(Abyss.USES.get(), uses);
            }
        }
    }
}