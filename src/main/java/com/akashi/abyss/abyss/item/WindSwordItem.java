package com.akashi.abyss.abyss.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class WindSwordItem extends Item {

    public WindSwordItem(Properties props) {
        super(props);
    }

    @Override
    public void hurtEnemy(
            ItemStack stack,
            LivingEntity target,
            LivingEntity attacker
    ) {
        // Стандартная обработка удара оружием
        super.hurtEnemy(stack, target, attacker);

        // Направление от атакующего к цели
        double dx = target.getX() - attacker.getX();
        double dz = target.getZ() - attacker.getZ();

        double dist = Math.max(
                0.1,
                Math.sqrt(dx * dx + dz * dz)
        );

        // Эффект ветра — сильное отбрасывание цели
        target.push(
                dx / dist * 0.9,
                0.35,
                dz / dist * 0.9
        );

        // Синхронизация движения существа
        target.hurtMarked = true;
    }
}