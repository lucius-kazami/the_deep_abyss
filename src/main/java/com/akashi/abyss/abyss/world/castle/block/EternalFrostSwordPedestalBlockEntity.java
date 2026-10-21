package com.akashi.abyss.abyss.block;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EternalFrostSwordPedestalBlockEntity extends BlockEntity {

    public EternalFrostSwordPedestalBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(
                Abyss.ETERNAL_FROST_SWORD_PEDESTAL_BE.get(),
                pos,
                state
        );
    }
}