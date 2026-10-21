package com.akashi.abyss.abyss.block;

import com.akashi.abyss.abyss.Abyss;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AbyssPlantBlock extends VegetationBlock {
    public static final MapCodec<AbyssPlantBlock> CODEC = simpleCodec(AbyssPlantBlock::new);
    private static final VoxelShape SHAPE = Block.box(3.0, 0.0, 3.0, 13.0, 12.0, 13.0);

    public AbyssPlantBlock(Properties props) { super(props); }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() { return CODEC; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Abyss.ABYSS_GRASS.get()) || state.is(Abyss.ABYSS_GRASS_WET.get())
                || state.is(Abyss.ABYSS_SOIL.get()) || state.is(Abyss.ABYSS_CHARRED_GROUND.get());
    }
}