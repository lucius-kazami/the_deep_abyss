package com.akashi.abyss.abyss.util;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class AbyssRay {
    private AbyssRay() {}

    public record Result(Vec3 start, Vec3 end, List<Entity> entities) {}

    public static Result trace(ServerLevel level, Player player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 maxEnd = start.add(player.getLookAngle().scale(range));

        BlockHitResult blockHit = level.clip(new ClipContext(start, maxEnd,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 end = blockHit.getType() == HitResult.Type.MISS ? maxEnd : blockHit.getLocation();

        AABB search = new AABB(start, end).inflate(1.0);
        List<Entity> hits = new ArrayList<>();
        for (Entity e : level.getEntities(player, search, en -> en instanceof LivingEntity && en.isAlive())) {
            AABB box = e.getBoundingBox().inflate(0.3);
            if (box.contains(start) || box.clip(start, end).isPresent()) hits.add(e);
        }
        hits.sort(Comparator.comparingDouble(e -> e.distanceToSqr(start)));
        return new Result(start, end, hits);
    }

    public static void drawLine(ServerLevel level, Vec3 from, Vec3 to, ParticleOptions particle) {
        Vec3 delta = to.subtract(from);
        int steps = Math.max(1, (int) (delta.length() / 0.5));
        Vec3 step = delta.scale(1.0 / steps);
        for (int i = 1; i <= steps; i++) {
            Vec3 p = from.add(step.scale(i));
            level.sendParticles(particle, p.x, p.y - 0.15, p.z, 1, 0.05, 0.05, 0.05, 0.0);
        }
    }
}