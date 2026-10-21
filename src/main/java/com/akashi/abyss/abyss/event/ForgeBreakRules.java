package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = Abyss.MODID)
public class ForgeBreakRules {

    private static final TagKey<Item> FORGE_BREAKERS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Abyss.MODID, "forge_breakers"));

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!event.getState().is(Abyss.ABYSS_FORGE.get())) return;
        Player player = event.getPlayer();
        if (player.isCreative()) return;
        if (player.getMainHandItem().is(FORGE_BREAKERS)) return;

        event.setCanceled(true);
        player.displayClientMessage(Component.literal("Кузницу Бездны можно сломать только алмазной киркой"), true);
    }
}