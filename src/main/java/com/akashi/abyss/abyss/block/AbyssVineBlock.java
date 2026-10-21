package com.akashi.abyss.abyss.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AbyssVineBlock extends VegetationBlock {
    public static final MapCodec<AbyssVineBlock> CODEC = simpleCodec(AbyssVineBlock::new);
    private static final VoxelShape SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0);

    public AbyssVineBlock(Properties props) { super(props); }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() { return CODEC; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    /** Лоза висит на сплошном блоке сверху или на другой такой же лозе. */
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        return aboveState.is(this) || aboveState.isFaceSturdy(level, above, Direction.DOWN);
    }
}