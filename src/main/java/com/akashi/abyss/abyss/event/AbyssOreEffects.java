package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Abyss.MODID)
public class AbyssOreEffects {

    public static final TagKey<Item> WATER_ARMOR =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Abyss.MODID, "water_armor"));

    private static final double FIRE_RANGE = 1.7;        // "подошёл на один блок"
    private static final double LIGHTNING_RANGE = 1.7;
    private static final double WIND_RANGE = 3.0;        // зона отталкивания

    private static final float FIRE_DAMAGE = 2.0f;
    private static final int FIRE_INTERVAL_TICKS = 20;
    private static final int LIGHTNING_COOLDOWN_TICKS = 100;
    private static final double WIND_PUSH_MAX = 0.45;
    private static final float WATER_ORE_MINING_MULT = 0.2f; // как под водой

    private static final Map<UUID, Long> LIGHTNING_LAST = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel level)) return;
        if (player.isSpectator()) return;

        Vec3 center = player.getBoundingBox().getCenter();
        BlockPos base = player.blockPosition();

        boolean nearFire = false;
        boolean nearLightning = false;
        double pushX = 0, pushZ = 0;

        for (BlockPos p : BlockPos.betweenClosed(base.offset(-3, -3, -3), base.offset(3, 3, 3))) {
            BlockState state = level.getBlockState(p);
            boolean fire = state.is(Abyss.FIRE_ORE.get());
            boolean lightning = state.is(Abyss.LIGHTNING_ORE.get());
            boolean wind = state.is(Abyss.WIND_ORE.get());
            if (!fire && !lightning && !wind) continue;

            double dx = p.getX() + 0.5 - center.x;
            double dy = p.getY() + 0.5 - center.y;
            double dz = p.getZ() + 0.5 - center.z;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (fire && dist <= FIRE_RANGE) nearFire = true;
            if (lightning && dist <= LIGHTNING_RANGE) nearLightning = true;
            if (wind && dist <= WIND_RANGE && dist > 0.001) {
                double strength = (WIND_RANGE - dist) / WIND_RANGE;
                double horiz = Math.sqrt(dx * dx + dz * dz);
                if (horiz > 0.001) {
                    pushX -= dx / horiz * strength;
                    pushZ -= dz / horiz * strength;
                }
            }
        }

        long time = level.getGameTime();

        // Огненная руда
        if (nearFire && time % FIRE_INTERVAL_TICKS == 0 && !wearsFullWaterArmor(player)) {
            player.hurt(level.damageSources().onFire(), FIRE_DAMAGE);
            player.setRemainingFireTicks(60);
        }

        // Руда молнии
        if (nearLightning) {
            long last = LIGHTNING_LAST.getOrDefault(player.getUUID(), -100000L);
            if (time - last >= LIGHTNING_COOLDOWN_TICKS) {
                LIGHTNING_LAST.put(player.getUUID(), time);
                LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
                bolt.setPos(player.getX(), player.getY(), player.getZ());
                level.addFreshEntity(bolt);
            }
        }

        // Руда ветра: отталкивает
        double len = Math.sqrt(pushX * pushX + pushZ * pushZ);
        if (len > 0.001) {
            double scale = Math.min(len, 1.0) * WIND_PUSH_MAX / len;
            Vec3 v = player.getDeltaMovement();
            player.setDeltaMovement(v.x * 0.5 + pushX * scale, v.y, v.z * 0.5 + pushZ * scale);
            player.hurtMarked = true; // синхронизировать скорость с клиентом
        }
    }

    // Руда воды: медленная добыча
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getState().is(Abyss.WATER_ORE.get())) {
            event.setNewSpeed(event.getNewSpeed() * WATER_ORE_MINING_MULT);
        }
    }

    private static boolean wearsFullWaterArmor(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(WATER_ARMOR)
                && player.getItemBySlot(EquipmentSlot.CHEST).is(WATER_ARMOR)
                && player.getItemBySlot(EquipmentSlot.LEGS).is(WATER_ARMOR)
                && player.getItemBySlot(EquipmentSlot.FEET).is(WATER_ARMOR);
    }
}