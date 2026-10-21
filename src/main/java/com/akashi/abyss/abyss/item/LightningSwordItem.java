package com.akashi.abyss.abyss.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class LightningSwordItem extends Item {

    public LightningSwordItem(Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(
            ItemStack stack,
            LivingEntity target,
            LivingEntity attacker
    ) {

        super.hurtEnemy(stack, target, attacker);

        if (!(target.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        /*
         * Создаём визуальную молнию.
         */
        LightningBolt lightning =
                new LightningBolt(
                        EntityType.LIGHTNING_BOLT,
                        serverLevel
                );

        lightning.setPos(
                target.getX(),
                target.getY(),
                target.getZ()
        );

        /*
         * Не позволяем ванильной молнии наносить
         * дополнительный неконтролируемый урон.
         */
        lightning.setVisualOnly(true);

        serverLevel.addFreshEntity(lightning);

        /*
         * Наш собственный электрический урон.
         */
        target.hurtServer(
                serverLevel,
                serverLevel.damageSources().lightningBolt(),
                6.0F
        );
    }
}