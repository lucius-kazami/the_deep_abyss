package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import com.akashi.abyss.abyss.world.AbyssDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import com.akashi.abyss.abyss.entity.ParasaurolophusEntity;
import com.akashi.abyss.abyss.entity.SpinosaurusEntity;

@EventBusSubscriber(modid = Abyss.MODID)
public class AbyssPoisonForest {

    private static final int CHECK_INTERVAL = 20;   // раз в секунду
    private static final int POISON_DURATION = 100; // 5 секунд, обновляется, пока в зоне
    private static final int VERTICAL_RANGE = 20;
    private static final int MOB_SCAN_RADIUS = 64;  // мобов травим только рядом с игроками

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (level.dimension() != AbyssDimensions.BEZDNA) return;
        if (level.getGameTime() % CHECK_INTERVAL != 0) return;

        if (shouldPoison(player)) {
            applyPoison(player);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.dimension() != AbyssDimensions.BEZDNA) return;
        if (level.getGameTime() % CHECK_INTERVAL != 0) return;

        for (ServerPlayer player : level.players()) {
            AABB box = player.getBoundingBox().inflate(MOB_SCAN_RADIUS);
            for (Mob mob : level.getEntitiesOfClass(Mob.class, box)) {
                if (shouldPoison(mob)) {
                    applyPoison(mob);
                }
            }
        }
    }

    private static boolean shouldPoison(LivingEntity entity) {

        // Защита от яда с помощью плода
        if (entity.hasEffect(Abyss.PALE_WARD)) {
            return false;
        }

        // Иммунитет паразауролофа и спинозавра
        if (entity instanceof ParasaurolophusEntity
                || entity instanceof SpinosaurusEntity) {
            return false;
        }

        int x = entity.getBlockX();
        int z = entity.getBlockZ();

        AbyssTerrain.Sample sample = AbyssTerrain.sample(x, z);

        if (sample.water() || sample.lava()) {
            return false;
        }

        if (Math.abs(entity.getBlockY() - sample.floorTop()) > VERTICAL_RANGE) {
            return false;
        }

        return AbyssTerrain.isPoisonZone(x, z);
    }

    private static void applyPoison(LivingEntity entity) {
        MobEffectInstance poison = new MobEffectInstance(MobEffects.POISON, POISON_DURATION, 0, true, true, true);

        if (entity.canBeAffected(poison)) {
            entity.addEffect(poison);
        } else if (entity.level() instanceof ServerLevel level) {
            // иммунные к яду (нежить и т.п.) получают урон напрямую
            entity.hurtServer(level, level.damageSources().magic(), 1.0f);
        }
    }

}