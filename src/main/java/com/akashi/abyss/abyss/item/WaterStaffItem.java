package com.akashi.abyss.abyss.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class WaterStaffItem extends Item {
    public static final float DAMAGE = 18.0F;
    public static final double RANGE = 16.0;
    public static final int COOLDOWN = 60;

    public WaterStaffItem(Properties props) {
        super(props);
    }

    // ПКМ: выстрел водяным лучом по взгляду
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(RANGE));
        fireBeam(server, player, start, end);
        player.getCooldowns().addCooldown(stack, COOLDOWN);
        return InteractionResult.CONSUME;
    }

    // Общий выстрел: используется и для ПКМ, и для автоматического выстрела по приближающимся врагам
    public static void fireBeam(ServerLevel server, Player player, Vec3 start, Vec3 end) {
        BlockHitResult blockHit = server.clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
        }

        AABB box = new AABB(start, end).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                player, start, end, box,
                e -> e instanceof LivingEntity && !e.isSpectator() && e.isPickable(),
                RANGE * RANGE);
        if (hit != null) {
            end = hit.getLocation();
            if (hit.getEntity() instanceof LivingEntity target) {
                target.hurtServer(server, server.damageSources().playerAttack(player), DAMAGE);
                target.clearFire();
            }
        }

        Vec3 step = end.subtract(start);
        int points = Math.max((int) (step.length() * 3), 1);
        for (int i = 0; i <= points; i++) {
            Vec3 p = start.add(step.scale(i / (double) points));
            server.sendParticles(ParticleTypes.SPLASH, p.x, p.y, p.z, 2, 0.04, 0.04, 0.04, 0.0);
            server.sendParticles(ParticleTypes.BUBBLE, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0.0);
        }
        server.playSound(null, player.blockPosition(), SoundEvents.PLAYER_SPLASH_HIGH_SPEED,
                SoundSource.PLAYERS, 1.0F, 1.2F);
    }
}