package com.akashi.abyss.abyss.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/**
 * Прыжок при упоре в препятствие. STEP_HEIGHT=2.0 позволяет ИИ
 * планировать движение через двухблочные уступы.
 */
public final class DinosaurJumpHelper {
    private DinosaurJumpHelper() {}

    public static void tick(Mob mob) {
        if (mob.level().isClientSide() || !mob.onGround()
                || mob.isInWater() || mob.isPassenger()
                || !mob.horizontalCollision || mob.tickCount % 12 != 0) {
            return;
        }

        Vec3 movement = mob.getDeltaMovement();
        Vec3 direction = movement.multiply(1, 0, 1);
        if (direction.lengthSqr() < 0.0004D) {
            Vec3 look = mob.getLookAngle();
            direction = new Vec3(look.x, 0, look.z);
        }
        if (direction.lengthSqr() < 0.0001D) return;
        direction = direction.normalize();

        BlockPos front = BlockPos.containing(
                mob.getX() + direction.x * (mob.getBbWidth() * 0.5D + 0.6D),
                mob.getY() + 0.2D,
                mob.getZ() + direction.z * (mob.getBbWidth() * 0.5D + 0.6D)
        );

        // Не прыгаем в потолок: нужно пространство над головой.
        BlockPos head = mob.blockPosition().above((int) Math.ceil(mob.getBbHeight()) + 1);
        if (!mob.level().getBlockState(head).isAir()) return;

        // Прыжок только когда впереди действительно есть препятствие.
        if (mob.level().getBlockState(front).isAir()
                && mob.level().getBlockState(front.above()).isAir()) return;

        mob.setDeltaMovement(movement.x, Math.max(movement.y, 0.85D), movement.z);
        mob.hasImpulse = true;
    }
}
