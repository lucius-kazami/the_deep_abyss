
package com.akashi.abyss.abyss.block.entity;

import com.akashi.abyss.abyss.Abyss;
import com.akashi.abyss.abyss.block.DinosaurEggBlock;
import com.akashi.abyss.abyss.entity.MosasaurusEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import com.akashi.abyss.abyss.block.MosasaurusEggBlock;
import net.minecraft.world.level.block.Blocks;
import com.akashi.abyss.abyss.entity.water.ElasmosaurusEntity;
import com.akashi.abyss.abyss.block.ElasmosaurusEggBlock;
import com.akashi.abyss.abyss.entity.water.ArchelonEntity;
import com.akashi.abyss.abyss.entity.heaven.QuetzalcoatlusEntity;

import java.util.List;

public class DinosaurEggBlockEntity extends BlockEntity {

    // Время инкубации обычных динозавров: 6 минут
    private static final int INCUBATION_TIME = 6000;

    // Время инкубации Мозазавра: 5 минут
    private static final int MOSASAURUS_INCUBATION_TIME = 6000;
    private static final int QUETZALCOATLUS_INCUBATION_TIME = 6000;

    private static final double START_RADIUS = 10.0;
    private static final double STOP_RADIUS = 15.0;

    private int incubationTicks = 0;
    private boolean incubationActive = false;

    public DinosaurEggBlockEntity(BlockPos pos, BlockState state) {
        super(Abyss.DINOSAUR_EGG_BLOCK_ENTITY.get(), pos, state);
    }

    public void serverTick(
            ServerLevel level,
            BlockPos pos,
            BlockState state
    ) {

        if (!(state.getBlock() instanceof DinosaurEggBlock eggBlock)) {
            return;
        }

        boolean isMosasaurusEgg =
                state.is(Abyss.MOSASAURUS_EGG.get());

        boolean isElasmosaurusEgg =
                state.is(Abyss.ELASMOSAURUS_EGG.get());

        boolean isAquaticEgg = isMosasaurusEgg || isElasmosaurusEgg;

        int requiredTime = state.is(Abyss.QUETZALCOATLUS_EGG.get())
                ? QUETZALCOATLUS_INCUBATION_TIME
                : (isAquaticEgg ? MOSASAURUS_INCUBATION_TIME : INCUBATION_TIME);

// Яйца водных рептилий развиваются только под водой
        if (isAquaticEgg
                && !level.getFluidState(pos).is(FluidTags.WATER)) {
            return;
        }

        // Мозазавр развивается исключительно под водой
        if (isMosasaurusEgg
                && !level.getFluidState(pos).is(FluidTags.WATER)) {

            return;
        }

        // Проверяем наличие игроков
        boolean playerWithinStart =
                hasPlayerNearby(level, pos, START_RADIUS);

        boolean playerWithinStop =
                hasPlayerNearby(level, pos, STOP_RADIUS);

        // Запускаем инкубацию
        if (!incubationActive && playerWithinStart) {
            incubationActive = true;
            setChanged();
        }

        // Приостанавливаем инкубацию
        if (incubationActive && !playerWithinStop) {
            incubationActive = false;
            setChanged();
        }

        if (!incubationActive) {
            return;
        }

        // Увеличиваем прогресс инкубации
        incubationTicks++;
        setChanged();

        // Три стадии развития яйца
        int stage = Math.min(
                2,
                (incubationTicks * 3) / requiredTime
        );

        if (state.getValue(DinosaurEggBlock.HATCH) != stage) {

            level.setBlock(
                    pos,
                    state.setValue(DinosaurEggBlock.HATCH, stage),
                    3
            );
        }

        // Проверяем завершение инкубации
        if (incubationTicks >= requiredTime) {
            hatchDinosaur(level, pos, eggBlock);
        }
    }

    private boolean hasPlayerNearby(
            ServerLevel level,
            BlockPos pos,
            double radius
    ) {

        AABB area = new AABB(pos).inflate(radius);

        List<Player> players = level.getEntitiesOfClass(
                Player.class,
                area,
                player -> !player.isSpectator()
        );

        return !players.isEmpty();
    }

    private void hatchDinosaur(
            ServerLevel level,
            BlockPos pos,
            DinosaurEggBlock eggBlock
    ) {

        // Создаём существо из зарегистрированного типа
        Mob mob = eggBlock.getDinosaurType().create(
                level,
                EntitySpawnReason.BREEDING
        );

        if (mob == null) {
            return;
        }

        // Отдельная логика для Мозазавра
        if (mob instanceof QuetzalcoatlusEntity quetzalcoatlus) {
            quetzalcoatlus.setQuetzalcoatlusBaby(true);
        } else if (mob instanceof ArchelonEntity archelon) {

            archelon.setArchelonBaby(true);

        } else if (mob instanceof MosasaurusEntity mosasaurus) {

            mosasaurus.setMosasaurusBaby(true);

        } else if (mob instanceof AgeableMob baby) {

            baby.setBaby(true);

        } else {
            return;
        }
        mob.setPos(
                pos.getX() + 0.5D,
                pos.getY(),
                pos.getZ() + 0.5D
        );

        if (level.addFreshEntity(mob)) {

            if (eggBlock instanceof MosasaurusEggBlock
                    || eggBlock instanceof ElasmosaurusEggBlock) {

                level.setBlock(
                        pos,
                        Blocks.WATER.defaultBlockState(),
                        3
                );

            } else {
                level.removeBlock(pos, false);
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putInt(
                "IncubationTicks",
                incubationTicks
        );

        output.putBoolean(
                "IncubationActive",
                incubationActive
        );
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        incubationTicks = input.getIntOr(
                "IncubationTicks",
                0
        );

        incubationActive = input.getBooleanOr(
                "IncubationActive",
                false
        );
    }
}
