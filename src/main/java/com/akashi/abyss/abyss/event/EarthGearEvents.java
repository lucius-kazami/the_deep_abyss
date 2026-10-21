package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = Abyss.MODID)
public class EarthGearEvents {

    private static final float BOW_BONUS = 15.0F;
    private static final float SPIKE_BONUS = 2.0F;
    private static final float REDUCTION_PER_PIECE = 0.125F;

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();
        float amount = event.getAmount();

        if (source.getDirectEntity() instanceof AbstractArrow arrow) {
            ItemStack weapon = arrow.getWeaponItem();
            if (weapon != null && weapon.is(Abyss.EARTH_BOW.get())) {
                amount += BOW_BONUS + SPIKE_BONUS;
                if (victim.level() instanceof ServerLevel server) {
                    server.sendParticles(
                            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.POINTED_DRIPSTONE.defaultBlockState()),
                            victim.getX(), victim.getY() + 0.3, victim.getZ(), 15, 0.3, 0.2, 0.3, 0.05);
                }
            }
        }

        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            int pieces = 0;
            if (victim.getItemBySlot(EquipmentSlot.HEAD).is(Abyss.EARTH_HELMET.get())) pieces++;
            if (victim.getItemBySlot(EquipmentSlot.CHEST).is(Abyss.EARTH_CHESTPLATE.get())) pieces++;
            if (victim.getItemBySlot(EquipmentSlot.LEGS).is(Abyss.EARTH_LEGGINGS.get())) pieces++;
            if (victim.getItemBySlot(EquipmentSlot.FEET).is(Abyss.EARTH_BOOTS.get())) pieces++;
            if (pieces > 0) {
                amount *= 1.0F - REDUCTION_PER_PIECE * pieces;
            }
        }

        event.setAmount(amount);
    }
}