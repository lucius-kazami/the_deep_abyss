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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

public class FireEyeItem extends Item {
    private static final double RANGE = 10.0;
    private static final float DAMAGE = 8.0F;
    private static final int IGNITE_SECONDS = 5;
    private static final int COOLDOWN = 10;

    public FireEyeItem(Properties props) {
        super(props);
    }

    // ПКМ (можно зажать): струя огня по линии взгляда, бьёт всех на пути
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }

        Vec3 start = player.getEyePosition().add(0.0, -0.2, 0.0);
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(RANGE));
        BlockHitResult blockHit = server.clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double length = RANGE;
        if (blockHit.getType() != HitResult.Type.MISS) {
            length = start.distanceTo(blockHit.getLocation());
        }

        Set<LivingEntity> alreadyHit = new HashSet<>();
        for (double d = 0.8; d <= length; d += 0.5) {
            Vec3 p = start.add(look.scale(d));
            double spread = 0.05 + d * 0.012;
            server.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 4, spread, spread, spread, 0.01);
            if (((int) (d * 2)) % 4 == 0) {
                server.sendParticles(ParticleTypes.SMOKE, p.x, p.y, p.z, 1, spread, spread, spread, 0.0);
            }
            AABB probe = new AABB(p, p).inflate(0.5 + d * 0.03);
            for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, probe,
                    e -> e != player && e.isAlive())) {
                if (alreadyHit.add(target)) {
                    target.hurtServer(server, server.damageSources().playerAttack(player), DAMAGE);
                    target.igniteForSeconds(IGNITE_SECONDS);
                }
            }
        }

        server.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE,
                SoundSource.PLAYERS, 0.8F, 1.1F);
        player.getCooldowns().addCooldown(stack, COOLDOWN);
        return InteractionResult.CONSUME;
    }
}
