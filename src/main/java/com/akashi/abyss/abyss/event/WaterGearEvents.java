package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import com.akashi.abyss.abyss.item.WaterStaffItem;

@EventBusSubscriber(modid = Abyss.MODID)
public class WaterGearEvents {

    private static final double STAFF_AUTO_RANGE = 6.0;
    private static final float CLEAVE_DAMAGE = 4.0F;
    private static final double CLEAVE_RADIUS = 2.5;
    private static final float WATER_MINING_BONUS = 1.5F;

    // защита от рекурсии, пока лезвие бьёт соседей
    private static boolean inCleave = false;

    private static boolean hasFullSet(Player p) {
        return p.getItemBySlot(EquipmentSlot.HEAD).is(Abyss.WATER_HELMET.get())
                && p.getItemBySlot(EquipmentSlot.CHEST).is(Abyss.WATER_CHESTPLATE.get())
                && p.getItemBySlot(EquipmentSlot.LEGS).is(Abyss.WATER_LEGGINGS.get())
                && p.getItemBySlot(EquipmentSlot.FEET).is(Abyss.WATER_BOOTS.get());
    }

    private static boolean isWaterMiningTool(ItemStack s) {
        return s.is(Abyss.WATER_PICKAXE.get()) || s.is(Abyss.WATER_AXE.get())
                || s.is(Abyss.WATER_SHOVEL.get()) || s.is(Abyss.WATER_HOE.get());
    }

    // ---------- броня и посох ----------
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player.level() instanceof ServerLevel server)) return;
        long time = server.getGameTime();

        if (hasFullSet(player)) {
            if (time % 10 == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 40, 0, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 40, 0, true, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, true, false, true));
            }
            player.clearFire();
            if (time % 4 == 0) {
                lavaToObsidian(server, player);
            }
        }

        if (time % 10 == 0) {
            autoFireStaff(server, player);
        }
    }

    private static void lavaToObsidian(ServerLevel server, Player player) {
        BlockPos base = player.blockPosition();
        boolean played = false;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 1; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos pos = base.offset(dx, dy, dz);
                    if (server.getBlockState(pos).is(Blocks.LAVA)) {
                        server.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState());
                        server.sendParticles(ParticleTypes.LARGE_SMOKE,
                                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 4, 0.2, 0.1, 0.2, 0.01);
                        if (!played) {
                            server.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 1.0F);
                            played = true;
                        }
                    }
                }
            }
        }
    }

    // Посох в руке сам стреляет по врагам, которые подошли ближе 6 блоков
    private static void autoFireStaff(ServerLevel server, Player player) {
        ItemStack staff = player.getMainHandItem().is(Abyss.WATER_STAFF.get()) ? player.getMainHandItem()
                : player.getOffhandItem().is(Abyss.WATER_STAFF.get()) ? player.getOffhandItem()
                : ItemStack.EMPTY;
        if (staff.isEmpty() || player.getCooldowns().isOnCooldown(staff)) return;

        AABB box = player.getBoundingBox().inflate(STAFF_AUTO_RANGE);
        LivingEntity nearest = null;
        double best = Double.MAX_VALUE;
        for (LivingEntity e : server.getEntitiesOfClass(LivingEntity.class, box,
                t -> t instanceof Enemy && t.isAlive() && player.hasLineOfSight(t))) {
            double d = e.distanceToSqr(player);
            if (d < best) {
                best = d;
                nearest = e;
            }
        }
        if (nearest == null) return;

        Vec3 start = player.getEyePosition();
        Vec3 aim = nearest.getBoundingBox().getCenter();
        Vec3 end = start.add(aim.subtract(start).normalize().scale(WaterStaffItem.RANGE));
        WaterStaffItem.fireBeam(server, player, start, end);
        player.getCooldowns().addCooldown(staff, WaterStaffItem.COOLDOWN);
    }

    // ---------- инструменты быстрее под водой ----------
    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (!isWaterMiningTool(player.getMainHandItem())) return;
        if (!player.isEyeInFluid(FluidTags.WATER)) return;

        float speed = event.getOriginalSpeed();
        float submerged = (float) player.getAttributeValue(Attributes.SUBMERGED_MINING_SPEED);
        if (submerged > 0.0F) {
            speed /= submerged;          // убираем штраф «под водой»
        }
        if (!player.onGround()) {
            speed *= 5.0F;               // убираем штраф «в воздухе» (при плавании)
        }
        event.setNewSpeed(speed * WATER_MINING_BONUS);
    }

    // ---------- водяное лезвие меча ----------
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (inCleave) return;
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof Player player) || source.getDirectEntity() != player) return;
        if (!player.getMainHandItem().is(Abyss.WATER_SWORD.get())) return;

        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel server)) return;

        inCleave = true;
        try {
            for (LivingEntity other : server.getEntitiesOfClass(LivingEntity.class,
                    victim.getBoundingBox().inflate(CLEAVE_RADIUS),
                    e -> e != victim && e != player && e.isAlive())) {
                other.hurtServer(server, server.damageSources().playerAttack(player), CLEAVE_DAMAGE);
            }
        } finally {
            inCleave = false;
        }

        server.sendParticles(ParticleTypes.SPLASH, victim.getX(), victim.getY() + 1.0, victim.getZ(),
                30, 0.8, 0.4, 0.8, 0.2);
        server.sendParticles(ParticleTypes.SWEEP_ATTACK, victim.getX(), victim.getY() + 1.0, victim.getZ(),
                1, 0.0, 0.0, 0.0, 0.0);
    }
}
