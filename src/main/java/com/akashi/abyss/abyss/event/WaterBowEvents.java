package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = Abyss.MODID)
public class WaterBowEvents {

    private static final float BOW_BONUS = 7.0F;      // урон самого лука
    private static final float JET_DAMAGE = 3.0F;     // урон каждой струи
    private static final double JET_LENGTH = 4.0;     // длина струи в блоках
    private static final double[] JET_ANGLES = {-35.0, 0.0, 35.0};   // три струи веером

    private static boolean inJets = false;

    @SubscribeEvent
    public static void onArrowHit(LivingIncomingDamageEvent event) {
        if (inJets) return;
        DamageSource source = event.getSource();
        if (!(source.getDirectEntity() instanceof AbstractArrow arrow)) return;
        ItemStack weapon = arrow.getWeaponItem();
        if (weapon == null || !weapon.is(Abyss.WATER_BOW.get())) return;

        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel server)) return;

        // цель получает +7 от лука и +3 от каждой из трёх струй
        event.setAmount(event.getAmount() + BOW_BONUS + JET_DAMAGE * JET_ANGLES.length);

        Vec3 dir = arrow.getDeltaMovement().multiply(1, 0, 1);
        dir = dir.lengthSqr() > 1.0E-4 ? dir.normalize() : new Vec3(1, 0, 0);
        Vec3 origin = victim.getBoundingBox().getCenter();
        Entity owner = arrow.getOwner();
        DamageSource jetSource = owner != null
                ? server.damageSources().indirectMagic(owner, owner)
                : server.damageSources().magic();

        inJets = true;
        try {
            for (double angle : JET_ANGLES) {
                Vec3 jetDir = dir.yRot((float) Math.toRadians(angle));
                Set<LivingEntity> alreadyHit = new HashSet<>();
                for (double d = 0.5; d <= JET_LENGTH; d += 0.5) {
                    Vec3 p = origin.add(jetDir.scale(d));
                    server.sendParticles(ParticleTypes.SPLASH, p.x, p.y, p.z, 4, 0.08, 0.08, 0.08, 0.0);
                    server.sendParticles(ParticleTypes.BUBBLE, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0.0);
                    // другие существа на пути струи тоже получают урон
                    AABB probe = new AABB(p, p).inflate(0.4);
                    for (LivingEntity e : server.getEntitiesOfClass(LivingEntity.class, probe,
                            t -> t != victim && t != owner && t.isAlive())) {
                        if (alreadyHit.add(e)) {
                            e.hurtServer(server, jetSource, JET_DAMAGE);
                        }
                    }
                }
            }
        } finally {
            inJets = false;
        }

        server.playSound(null, victim.blockPosition(), SoundEvents.PLAYER_SPLASH,
                SoundSource.PLAYERS, 0.8F, 1.3F);
    }
}
