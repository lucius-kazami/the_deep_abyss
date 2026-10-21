
package com.akashi.abyss.abyss.block;

import com.akashi.abyss.abyss.entity.ModEntities;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;

public class ElasmosaurusEggBlock
        extends DinosaurEggBlock
        implements SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED =
            BlockStateProperties.WATERLOGGED;

    public static final MapCodec<ElasmosaurusEggBlock> CODEC =
            simpleCodec(ElasmosaurusEggBlock::new);

    public ElasmosaurusEggBlock(BlockBehaviour.Properties properties) {
        super(
                12.0F,
                12.0F,
                ModEntities.ELASMOSAURUS::get,
                properties
        );

        registerDefaultState(
                defaultBlockState().setValue(WATERLOGGED, false)
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

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean water = context.getLevel()
                .getFluidState(context.getClickedPos())
                .getType() == Fluids.WATER;

        return defaultBlockState()
                .setValue(WATERLOGGED, water);
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
            net.minecraft.world.level.LevelReader level,
            net.minecraft.world.level.ScheduledTickAccess ticks,
            BlockPos pos,
            net.minecraft.core.Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            net.minecraft.util.RandomSource random
    ) {
        if (state.getValue(WATERLOGGED)) {
            ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(
                state, level, ticks, pos,
                direction, neighborPos, neighborState, random
        );
    }
}
