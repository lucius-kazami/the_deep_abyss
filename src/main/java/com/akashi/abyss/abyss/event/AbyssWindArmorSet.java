package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.world.entity.EquipmentSlot;

@EventBusSubscriber(modid = Abyss.MODID)
public class AbyssWindArmorSet {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().getGameTime() % 20 != 0) return;

        boolean fullSet =
                player.getItemBySlot(EquipmentSlot.HEAD).is(Abyss.WIND_HELMET.get())
                        && player.getItemBySlot(EquipmentSlot.CHEST).is(Abyss.WIND_CHESTPLATE.get())
                        && player.getItemBySlot(EquipmentSlot.LEGS).is(Abyss.WIND_LEGGINGS.get())
                        && player.getItemBySlot(EquipmentSlot.FEET).is(Abyss.WIND_BOOTS.get());

        if (!fullSet) return;

        player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 40, 4, true, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 1, true, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40, 0, true, false, true));
    }
}