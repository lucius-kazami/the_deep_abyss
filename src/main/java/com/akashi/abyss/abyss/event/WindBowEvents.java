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

@EventBusSubscriber(modid = Abyss.MODID)
public class WindBowEvents {

    private static final float WIND_BONUS = 8.0F;        // дополнительный урон ветром цели
    private static final float GUST_DAMAGE = 3.0F;       // урон порыва по остальным вокруг цели
    private static final double GUST_RADIUS = 3.5;       // радиус порыва в блоках
    private static final double VICTIM_LIFT = 0.6;       // подброс цели вверх
    private static final double VICTIM_PUSH = 0.7;       // отбрасывание цели по полёту стрелы
    private static final double GUST_PUSH = 0.9;         // отбрасывание остальных от цели

    private static boolean inGust = false;

    @SubscribeEvent
    public static void onArrowHit(LivingIncomingDamageEvent event) {
        if (inGust) return;
        DamageSource source = event.getSource();
        if (!(source.getDirectEntity() instanceof AbstractArrow arrow)) return;
        ItemStack weapon = arrow.getWeaponItem();
        if (weapon == null || !weapon.is(Abyss.WIND_BOW.get())) return;

        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel server)) return;

        // цель получает дополнительный урон ветром
        event.setAmount(event.getAmount() + WIND_BONUS);

        Vec3 dir = arrow.getDeltaMovement().multiply(1, 0, 1);
        dir = dir.lengthSqr() > 1.0E-4 ? dir.normalize() : new Vec3(1, 0, 0);
        Vec3 origin = victim.getBoundingBox().getCenter();
        Entity owner = arrow.getOwner();
        DamageSource gustSource = owner != null
                ? server.damageSources().indirectMagic(owner, owner)
                : server.damageSources().magic();

        // цель подбрасывает вверх и сдувает по направлению выстрела
        victim.push(dir.x * VICTIM_PUSH, VICTIM_LIFT, dir.z * VICTIM_PUSH);
        victim.hurtMarked = true;

        // порыв: остальные вокруг цели получают урон и отлетают от неё
        inGust = true;
        try {
            AABB area = new AABB(origin, origin).inflate(GUST_RADIUS);
            for (LivingEntity e : server.getEntitiesOfClass(LivingEntity.class, area,
                    t -> t != victim && t != owner && t.isAlive()
                            && t.distanceToSqr(origin) <= GUST_RADIUS * GUST_RADIUS)) {
                e.hurtServer(server, gustSource, GUST_DAMAGE);
                Vec3 away = e.position().subtract(victim.position()).multiply(1, 0, 1);
                away = away.lengthSqr() > 1.0E-4 ? away.normalize() : Vec3.ZERO;
                e.push(away.x * GUST_PUSH, 0.4, away.z * GUST_PUSH);
                e.hurtMarked = true;
            }
        } finally {
            inGust = false;
        }

        // визуал: облако и завихрение вокруг цели
        server.sendParticles(ParticleTypes.CLOUD, origin.x, origin.y, origin.z, 25, 0.5, 0.4, 0.5, 0.15);
        server.sendParticles(ParticleTypes.POOF, origin.x, origin.y, origin.z, 12, 0.4, 0.3, 0.4, 0.1);
        for (int i = 0; i < 12; i++) {
            double a = Math.PI * 2 * i / 12.0;
            server.sendParticles(ParticleTypes.CLOUD,
                    origin.x + Math.cos(a) * GUST_RADIUS * 0.6, origin.y, origin.z + Math.sin(a) * GUST_RADIUS * 0.6,
                    1, 0.05, 0.05, 0.05, 0.02);
        }
        server.playSound(null, victim.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.PLAYERS, 1.0F, 0.6F);
    }
}