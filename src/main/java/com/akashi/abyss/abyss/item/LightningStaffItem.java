package com.akashi.abyss.abyss.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class LightningStaffItem extends Item {

    private static final double RADIUS = 12.0D;
    private static final float DAMAGE = 12.0F;

    public LightningStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {

        ItemStack stack = player.getItemInHand(hand);

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        /*
         * Область вокруг игрока.
         */
        AABB area = player.getBoundingBox().inflate(RADIUS);

        List<LivingEntity> targets =
                serverLevel.getEntitiesOfClass(
                        LivingEntity.class,
                        area,
                        entity ->
                                entity != player
                                        && entity.isAlive()
                );

        /*
         * Бьём каждую цель.
         */
        for (LivingEntity target : targets) {

            target.hurtServer(
                    serverLevel,
                    serverLevel.damageSources().lightningBolt(),
                    DAMAGE
            );

            createLightning(
                    serverLevel,
                    target.blockPosition()
            );
        }

        /*
         * Большая электрическая вспышка.
         */
        serverLevel.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                player.getX(),
                player.getY() + 1.0,
                player.getZ(),
                120,
                6.0,
                2.0,
                6.0,
                0.25
        );

        /*
         * Гром.
         */
        serverLevel.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.PLAYERS,
                2.0F,
                1.0F
        );

        /*
         * Расход прочности.
         */
        stack.hurtAndBreak(
                1,
                player,
                hand == InteractionHand.MAIN_HAND
                        ? net.minecraft.world.entity.EquipmentSlot.MAINHAND
                        : net.minecraft.world.entity.EquipmentSlot.OFFHAND
        );

        /*
         * Перезарядка — 3 секунды.
         */
        player.getCooldowns().addCooldown(
                stack,
                60
        );

        return InteractionResult.SUCCESS;
    }

    private static void createLightning(
            ServerLevel level,
            BlockPos pos
    ) {

        LightningBolt lightning =
                new LightningBolt(
                        EntityType.LIGHTNING_BOLT,
                        level
                );

        lightning.setPos(
                pos.getX() + 0.5,
                pos.getY(),
                pos.getZ() + 0.5
        );

        /*
         * Ванильная молния только визуальная.
         * Урон мы уже нанесли сами.
         */
        lightning.setVisualOnly(true);

        level.addFreshEntity(lightning);
    }
}