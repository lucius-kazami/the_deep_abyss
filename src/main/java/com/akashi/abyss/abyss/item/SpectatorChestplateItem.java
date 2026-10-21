package com.akashi.abyss.abyss.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SpectatorChestplateItem extends Item {

    // ============================================================
    // СПЕКТРАЛЬНОЕ СОСТОЯНИЕ
    // ============================================================

    // 60 секунд
    public static final int SPECTRAL_DURATION = 20 * 60;

    // 120 секунд
    public static final int SPECTRAL_COOLDOWN = 20 * 60 * 2;


    public SpectatorChestplateItem(Properties properties) {
        super(properties);
    }


    // ============================================================
    // ПРОВЕРКА ПРЕДМЕТА
    // ============================================================

    public static boolean isSpectatorChestplate(ItemStack stack) {

        return stack.getItem()
                instanceof SpectatorChestplateItem;
    }
}