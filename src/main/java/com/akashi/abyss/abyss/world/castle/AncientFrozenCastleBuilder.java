package com.akashi.abyss.abyss.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public final class AncientFrozenCastleBuilder {

    private AncientFrozenCastleBuilder() {}

    public static void buildChunk(
            ServerLevel level,
            BlockPos center,
            ChunkPos chunk
    ) {

        int centerX = center.getX();
        int baseY = center.getY();
        int centerZ = center.getZ();

        int chunkMinX =
                chunk.getMinBlockX();

        int chunkMinZ =
                chunk.getMinBlockZ();

        // ==============================================
        // 1. Древний остров / фундамент
        // ==============================================

        CastleFoundation.buildChunk(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );

        // ==============================================
        // 2. Главные ворота
        // ==============================================

        CastleEntrance.buildChunk(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );

        // ==============================================
// 3. Внешние крепостные стены
// ==============================================

        CastleWalls.buildChunk(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );

// ==============================================
// 4. Четыре угловые башни
// ==============================================

        CastleTowers.buildChunk(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );

        // ==============================================
// 5. Главный дворец
// ==============================================

        CastleMainPalace.buildChunk(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );
        // ==============================================
// 6. Тронный зал и арена Меча Вечного Льда
// ==============================================

        CastleThroneRoom.buildChunk(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );

        // ==============================================
// 7. Архитектурные детали и разрушения
// ==============================================

        CastleDetails.buildChunk(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );

        // ==============================================
// 8. Внутренние помещения замка
// ==============================================

        CastleInterior.buildChunk(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );

        // ==============================================
// 9. Атмосфера и декорации
// ==============================================

        CastleDecorations.buildChunk(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );

// ==============================================
// 10. Сундуки и награды
// ==============================================

        CastleLoot.buildChunk(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );
        // ОБЯЗАТЕЛЬНО ПОСЛЕДНИМ
        CastleThroneAndPedestal.buildChunk(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );
    }
}