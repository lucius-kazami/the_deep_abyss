package com.akashi.abyss.abyss.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import com.akashi.abyss.abyss.Abyss;

public class AbyssWaterPlantBlock extends VegetationBlock {
    public static final MapCodec<AbyssWaterPlantBlock> CODEC = simpleCodec(AbyssWaterPlantBlock::new);
    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);

    public AbyssWaterPlantBlock(Properties props) { super(props); }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() { return CODEC; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.SAND) || state.is(Blocks.GRAVEL) || state.is(Blocks.CLAY)
                || state.is(Abyss.ABYSS_SOIL.get()) || state.is(Abyss.ABYSS_GRASS_WET.get());
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return Fluids.WATER.getSource(false);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        FluidState fluid = ctx.getLevel().getFluidState(ctx.getClickedPos());
        return fluid.is(FluidTags.WATER) && fluid.isSource() ? defaultBlockState() : null;
    }
}