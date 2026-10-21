package com.akashi.abyss.abyss.block;

import com.akashi.abyss.abyss.entity.ModEntities;
import com.akashi.abyss.abyss.entity.TyrannosaurusEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TyrannosaurusEggBlock extends Block {

    private static final int HATCH_TIME = 6000; // 5 минут

    public TyrannosaurusEggBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void onPlace(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState oldState,
            boolean movedByPiston
    ) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if (!level.isClientSide() && !state.is(oldState.getBlock())) {
            level.scheduleTick(pos, this, HATCH_TIME);
        }
    }

    @Override
    protected void tick(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random
    ) {
        TyrannosaurusEntity baby =
                ModEntities.TYRANNOSAURUS.get().create(
                        level,
                        net.minecraft.world.entity.EntitySpawnReason.TRIGGERED
                );

        if (baby == null) return;

        baby.setAge(-24000);
        baby.setPos(
                pos.getX() + 0.5,
                pos.getY() + 0.2,
                pos.getZ() + 0.5
        );

        level.removeBlock(pos, false);
        level.addFreshEntity(baby);
    }
}