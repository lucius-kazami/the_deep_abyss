package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import com.akashi.abyss.abyss.world.AbyssDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Abyss.MODID)
public class AbyssFruitDrop {

    private static final int INTERVAL = 100; // раз в 5 секунд
    private static final int RADIUS = 12;
    private static final int ATTEMPTS = 10;
    private static final double DROP_CHANCE = 0.05;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (level.dimension() != AbyssDimensions.BEZDNA) return;
        if (level.getGameTime() % INTERVAL != 0) return;

        RandomSource rnd = level.getRandom();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < ATTEMPTS; i++) {
            int x = player.getBlockX() + rnd.nextInt(RADIUS * 2 + 1) - RADIUS;
            int y = player.getBlockY() + rnd.nextInt(9) - 3;
            int z = player.getBlockZ() + rnd.nextInt(RADIUS * 2 + 1) - RADIUS;
            pos.set(x, y, z);
            if (!level.hasChunkAt(pos)) continue;
            if (!level.getBlockState(pos).is(Abyss.PURPLE_LEAVES.get())) continue;
            if (rnd.nextDouble() > DROP_CHANCE) continue;

            BlockPos below = pos.below();
            if (!level.getBlockState(below).isAir()) continue;
            ItemEntity fruit = new ItemEntity(level, below.getX() + 0.5, below.getY() + 0.1, below.getZ() + 0.5,
                    new ItemStack(Abyss.PALE_ANTIDOTE_FRUIT.get()));
            level.addFreshEntity(fruit);
        }
    }
}