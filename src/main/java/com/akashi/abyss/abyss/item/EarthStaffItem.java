package com.akashi.abyss.abyss.item;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EarthStaffItem extends Item {
    private static final double RADIUS = 7.0;
    private static final float DAMAGE = 8.0F;

    public EarthStaffItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack staff = player.getItemInHand(hand);

        int slot = -1;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(Abyss.EARTH_RAW.get())) {
                slot = i;
                break;
            }
        }
        boolean creative = player.getAbilities().instabuild;
        if (slot < 0 && !creative) {
            return InteractionResult.FAIL;
        }
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        if (slot >= 0 && !creative) {
            player.getInventory().getItem(slot).shrink(1);
        }

        AABB area = player.getBoundingBox().inflate(RADIUS, 2.0, RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive())) {
            target.hurtServer(server, server.damageSources().playerAttack(player), DAMAGE);
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 2));
            Vec3 away = target.position().subtract(player.position()).multiply(1, 0, 1);
            away = away.lengthSqr() > 0.001 ? away.normalize() : Vec3.ZERO;
            target.push(away.x * 0.5, 0.8, away.z * 0.5);
            target.hurtMarked = true;
        }

        for (int ring = 1; ring <= 7; ring++) {
            server.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()),
                    player.getX(), player.getY() + 0.2, player.getZ(),
                    25, ring * 0.5, 0.15, ring * 0.5, 0.05);
        }
        server.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.PLAYERS, 0.8F, 0.5F);
        server.playSound(null, player.blockPosition(), SoundEvents.GRAVEL_BREAK,
                SoundSource.PLAYERS, 1.5F, 0.5F);

        player.getCooldowns().addCooldown(staff, 100);
        return InteractionResult.CONSUME;
    }
}