package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

@EventBusSubscriber(modid = Abyss.MODID)
public class AbyssWindShieldHandler {

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {

        // Нас интересует только игрок
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Проверяем, что удар действительно заблокирован
        if (!event.getBlocked()) {
            return;
        }

        // Проверяем именно щит ветра
        if (!player.getUseItem().is(Abyss.WIND_SHIELD.get())) {
            return;
        }

        // Кто атаковал игрока
        Entity attacker = event.getDamageSource().getEntity();

        if (attacker == null || attacker == player) {
            return;
        }

        // Направление ОТ игрока К атакующему
        double dx = attacker.getX() - player.getX();
        double dz = attacker.getZ() - player.getZ();

        double dist = Math.max(
                0.1,
                Math.sqrt(dx * dx + dz * dz)
        );

        // Сильный порыв ветра отбрасывает атакующего
        attacker.push(
                dx / dist * 0.8,
                0.3,
                dz / dist * 0.8
        );

        attacker.hurtMarked = true;
    }
}