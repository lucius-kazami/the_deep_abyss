
package com.akashi.abyss.abyss.block;

import com.akashi.abyss.abyss.entity.ModEntities;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.LevelReader;

import org.jetbrains.annotations.Nullable;

public class MosasaurusEggBlock extends DinosaurEggBlock
        implements SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED =
            BlockStateProperties.WATERLOGGED;

    public static final MapCodec<MosasaurusEggBlock> CODEC =
            simpleCodec(MosasaurusEggBlock::new);

    public MosasaurusEggBlock(BlockBehaviour.Properties properties) {
        super(
                15.6F,
                18.2F,
                ModEntities.MOSASAURUS::get,
                properties
        );

        registerDefaultState(
                defaultBlockState()
                        .setValue(WATERLOGGED, false)
        );
    }

    @Override
    protected MapCodec<? extends DinosaurEggBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        super.createBlockStateDefinition(builder);
        builder.add(WATERLOGGED);
    }

    // Яйцо можно поставить только в воду
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {

        FluidState fluid =
                context.getLevel().getFluidState(
                        context.getClickedPos()
                );

        // Запрещаем установку на суше
        if (!fluid.is(FluidTags.WATER)) {
            return null;
        }

        // Яйцо появляется с водой внутри блока
        return defaultBlockState()
                .setValue(WATERLOGGED, true);
    }

    // Яйцо должно находиться в воде
    @Override
    protected boolean canSurvive(
            BlockState state,
            net.minecraft.world.level.LevelReader level,
            BlockPos pos
    ) {
        return state.getValue(WATERLOGGED);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED)
                ? Fluids.WATER.getSource(false)
                : super.getFluidState(state);
    }


    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess tickAccess,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random
    ) {
        if (state.getValue(WATERLOGGED)) {
            tickAccess.scheduleTick(
                    pos,
                    Fluids.WATER,
                    Fluids.WATER.getTickDelay(level)
            );
        }

        return super.updateShape(
                state,
                level,
                tickAccess,
                pos,
                direction,
                neighborPos,
                neighborState,
                random
        );
    }
}
