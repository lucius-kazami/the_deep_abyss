package com.akashi.abyss.abyss.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class FireStaffItem extends Item {
    private static final double RADIUS = 6.0;
    private static final float DAMAGE = 12.0F;
    private static final int IGNITE_SECONDS = 6;
    private static final int COOLDOWN = 80;

    public FireStaffItem(Properties props) {
        super(props);
    }

    // ПКМ: огненная вспышка по площади вокруг игрока
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }

        AABB area = player.getBoundingBox().inflate(RADIUS, 2.0, RADIUS);
        for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive() && e.distanceToSqr(player) <= RADIUS * RADIUS)) {
            target.hurtServer(server, server.damageSources().playerAttack(player), DAMAGE);
            target.igniteForSeconds(IGNITE_SECONDS);
        }

        for (int r = 1; r <= (int) RADIUS; r++) {
            server.sendParticles(ParticleTypes.FLAME,
                    player.getX(), player.getY() + 0.3, player.getZ(),
                    30 + r * 6, r * 0.5, 0.25, r * 0.5, 0.03);
        }
        server.sendParticles(ParticleTypes.LAVA,
                player.getX(), player.getY() + 0.5, player.getZ(), 20, RADIUS * 0.4, 0.3, RADIUS * 0.4, 0.0);
        server.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.PLAYERS, 0.7F, 1.4F);
        server.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE,
                SoundSource.PLAYERS, 1.2F, 0.7F);

        player.getCooldowns().addCooldown(stack, COOLDOWN);
        return InteractionResult.CONSUME;
    }
}
