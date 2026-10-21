package com.akashi.abyss.abyss.world.castle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class AncientFrozenCastleSavedData extends SavedData {

    // ============================================================
    // CODEC
    // ============================================================

    public static final Codec<AncientFrozenCastleSavedData> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(

                            Codec.BOOL
                                    .fieldOf("generated")
                                    .forGetter(data -> data.generated),

                            Codec.INT
                                    .fieldOf("x")
                                    .forGetter(data -> data.x),

                            Codec.INT
                                    .fieldOf("y")
                                    .forGetter(data -> data.y),

                            Codec.INT
                                    .fieldOf("z")
                                    .forGetter(data -> data.z)

                    ).apply(
                            instance,
                            AncientFrozenCastleSavedData::new
                    )
            );


    // ============================================================
    // SAVED DATA TYPE
    // ============================================================

    public static final SavedDataType<AncientFrozenCastleSavedData> TYPE =
            new SavedDataType<>(
                    "abyss_ancient_frozen_castle",
                    AncientFrozenCastleSavedData::new,
                    CODEC
            );


    // ============================================================
    // DATA
    // ============================================================

    private boolean generated;

    private int x;
    private int y;
    private int z;


    // ============================================================
    // CONSTRUCTOR - НОВЫЙ МИР
    // ============================================================

    public AncientFrozenCastleSavedData() {

        this(
                false,
                0,
                0,
                0
        );
    }


    // ============================================================
    // CONSTRUCTOR - ЗАГРУЗКА
    // ============================================================

    public AncientFrozenCastleSavedData(
            boolean generated,
            int x,
            int y,
            int z
    ) {

        this.generated = generated;

        this.x = x;
        this.y = y;
        this.z = z;
    }


    // ============================================================
    // СОЗДАН ЛИ ЗАМОК
    // ============================================================

    public boolean isGenerated() {

        return generated;
    }


    // ============================================================
    // КООРДИНАТЫ ЗАМКА
    // ============================================================

    public BlockPos getPosition() {

        return new BlockPos(
                x,
                y,
                z
        );
    }


    // ============================================================
    // СОХРАНЯЕМ ЗАМОК
    // ============================================================

    public void markGenerated(BlockPos pos) {

        generated = true;

        x = pos.getX();
        y = pos.getY();
        z = pos.getZ();

        setDirty();
    }
}