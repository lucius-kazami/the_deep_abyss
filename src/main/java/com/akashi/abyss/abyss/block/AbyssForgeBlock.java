package com.akashi.abyss.abyss.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import com.akashi.abyss.abyss.Abyss;

public class AbyssForgeBlock extends BaseEntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final MapCodec<AbyssForgeBlock> CODEC = simpleCodec(AbyssForgeBlock::new);

    public AbyssForgeBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AbyssForgeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }

        return createTickerHelper(
                type,
                Abyss.ABYSS_FORGE_BE.get(),
                AbyssForgeBlockEntity::serverTick
        );
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MenuProvider provider) {
            player.openMenu(provider, pos);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;

        double x = pos.getX() + 0.5, y = pos.getY(), z = pos.getZ() + 0.5;

        if (random.nextDouble() < 0.4) {
            level.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }

        Direction dir = state.getValue(FACING);
        double fx = x + dir.getStepX() * 0.52;
        double fz = z + dir.getStepZ() * 0.52;
        double jitter = (random.nextDouble() - 0.5) * 0.6;
        double jx = dir.getAxis() == Direction.Axis.Z ? jitter : 0.0;
        double jz = dir.getAxis() == Direction.Axis.X ? jitter : 0.0;

        for (int i = 0; i < 3; i++) {
            level.addParticle(ParticleTypes.FLAME,
                    fx + jx, y + 0.3 + random.nextDouble() * 0.4, fz + jz, 0.0, 0.01, 0.0);
        }
        level.addParticle(ParticleTypes.LAVA, x + (random.nextDouble() - 0.5) * 0.5,
                y + 1.0, z + (random.nextDouble() - 0.5) * 0.5, 0.0, 0.0, 0.0);
        level.addParticle(ParticleTypes.LARGE_SMOKE, x, y + 1.1, z, 0.0, 0.03, 0.0);
    }
}