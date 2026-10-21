package com.akashi.abyss.abyss.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class EternalFrostSwordPedestalBlock extends BaseEntityBlock {

    public static final MapCodec<EternalFrostSwordPedestalBlock> CODEC =
            simpleCodec(EternalFrostSwordPedestalBlock::new);

    public EternalFrostSwordPedestalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new EternalFrostSwordPedestalBlockEntity(
                pos,
                state
        );
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(level instanceof ServerLevel)) {
            return InteractionResult.PASS;
        }

        player.displayClientMessage(
                Component.literal(
                        "§b❄ Меч Вечного Мороза запечатан..."
                ),
                false
        );

        player.displayClientMessage(
                Component.literal(
                        "§7Древняя сила не позволяет вытащить клинок."
                ),
                false
        );

        return InteractionResult.SUCCESS;
    }
}