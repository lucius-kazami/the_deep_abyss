package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import com.akashi.abyss.abyss.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Abyss.MODID)
public class HealingWaterEvents {

    /** ПКМ стеклянной бутылкой по целебной воде даёт зелье исцеления. */
    @SubscribeEvent
    public static void onBottleUse(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(Items.GLASS_BOTTLE)) return;

        Player player = event.getEntity();
        Level level = event.getLevel();

        HitResult hit = player.pick(player.blockInteractionRange(), 1.0F, true); // true = учитывать жидкости
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = blockHit.getBlockPos();
        FluidState fluid = level.getFluidState(pos);
        if (!fluid.isSource() || !fluid.getType().isSame(ModFluids.HEALING_WATER.get())) return;

        if (!level.isClientSide()) {
            level.playSound(null, player.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
            ItemStack potion = PotionContents.createItemStack(Items.POTION, Potions.HEALING);
            player.setItemInHand(event.getHand(), ItemUtils.createFilledResult(stack, player, potion));
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    /** Купание: раз в секунду лечит существо в целебной воде и снимает яд. */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        if (!(living.level() instanceof ServerLevel level)) return;
        if (living.tickCount % 20 != 0) return;

        FluidState fluid = level.getFluidState(living.blockPosition());
        if (!fluid.getType().isSame(ModFluids.HEALING_WATER.get())) return;

        living.heal(2.0F); // одно сердце в секунду
        living.removeEffect(MobEffects.POISON);
    }
}