
package com.akashi.abyss.abyss.block;

import com.akashi.abyss.abyss.Abyss;
import com.akashi.abyss.abyss.block.entity.DinosaurEggBlockEntity;
import com.akashi.abyss.abyss.entity.ModEntities;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class DinosaurEggBlock extends BaseEntityBlock {

    // Стадии вылупления:
    // 0 — целое яйцо
    // 1 — появились трещины
    // 2 — почти вылупилось
    public static final IntegerProperty HATCH =
            IntegerProperty.create("hatch", 0, 2);

    private final VoxelShape shape;

    // Mob позволяет использовать как наземных динозавров,
    // так и водного Мозазавра
    private final Supplier<? extends EntityType<? extends Mob>> dinosaurType;

    public DinosaurEggBlock(
            float width,
            float height,
            Supplier<? extends EntityType<? extends Mob>> dinosaurType,
            BlockBehaviour.Properties properties
    ) {
        super(properties);

        this.dinosaurType = dinosaurType;

        this.registerDefaultState(
                this.stateDefinition.any().setValue(HATCH, 0)
        );

        double halfWidth = width / 2.0D;

        this.shape = Block.box(
                8.0D - halfWidth,
                0.0D,
                8.0D - halfWidth,
                8.0D + halfWidth,
                height,
                8.0D + halfWidth
        );
    }

    // Получаем тип динозавра, который должен вылупиться
    public EntityType<? extends Mob> getDinosaurType() {
        return dinosaurType.get();
    }

    public static final MapCodec<DinosaurEggBlock> CODEC =
            simpleCodec(properties -> new DinosaurEggBlock(
                    15.6F,
                    18.2F,
                    ModEntities.TRICERATOPS::get,
                    properties
            ));

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return shape;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(HATCH);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new DinosaurEggBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<T> type
    ) {
        if (level.isClientSide()) {
            return null;
        }

        return createTickerHelper(
                type,
                Abyss.DINOSAUR_EGG_BLOCK_ENTITY.get(),
                (world, pos, blockState, blockEntity) -> {

                    if (world instanceof ServerLevel serverLevel) {
                        blockEntity.serverTick(
                                serverLevel,
                                pos,
                                blockState
                        );
                    }
                }
        );
    }
}
