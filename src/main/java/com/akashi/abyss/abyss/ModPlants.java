package com.akashi.abyss.abyss;

import com.akashi.abyss.abyss.block.AbyssPlantBlock;
import com.akashi.abyss.abyss.block.AbyssVineBlock;
import com.akashi.abyss.abyss.block.AbyssWaterPlantBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.ArrayList;
import java.util.List;

public final class ModPlants {
    private ModPlants() {}

    /** Все предметы растений — для креативной вкладки. */
    public static final List<DeferredItem<? extends Item>> ITEM_LIST = new ArrayList<>();

    /** Пустой вызов из конструктора мода, чтобы класс загрузился до регистрации. */
    public static void init() {}

    // --- Наземные ---
    public static final DeferredBlock<AbyssPlantBlock> BELL_FLOWER = plant("bell_flower", 0);
    public static final DeferredBlock<AbyssPlantBlock> PINK_FLOWER = plant("pink_flower", 0);
    public static final DeferredBlock<AbyssPlantBlock> FIRE_FLOWER = plant("fire_flower", 8);
    public static final DeferredBlock<AbyssPlantBlock> STAR_FLOWER = plant("star_flower", 9);
    public static final DeferredBlock<AbyssPlantBlock> TALL_GRASS = plant("tall_grass", 0);
    public static final DeferredBlock<AbyssPlantBlock> GOLDEN_GRASS = plant("golden_grass", 0);
    public static final DeferredBlock<AbyssPlantBlock> PURPLE_GRASS = plant("purple_grass", 0);
    public static final DeferredBlock<AbyssPlantBlock> CATTAIL = plant("cattail", 0);
    public static final DeferredBlock<AbyssPlantBlock> CYAN_MUSHROOM = plant("cyan_mushroom", 7);
    public static final DeferredBlock<AbyssPlantBlock> DARK_MUSHROOM = plant("dark_mushroom", 0);
    public static final DeferredBlock<AbyssPlantBlock> FIRE_MUSHROOM = plant("fire_mushroom", 10);

    // --- Лозы ---
    public static final DeferredBlock<AbyssVineBlock> WHITE_VINE = vine("white_vine", 0);
    public static final DeferredBlock<AbyssVineBlock> PINK_VINE = vine("pink_vine", 0);
    public static final DeferredBlock<AbyssVineBlock> FIRE_VINE = vine("fire_vine", 8);
    public static final DeferredBlock<AbyssVineBlock> PURPLE_VINE = vine("purple_vine", 0);

    // --- Подводные ---
    public static final DeferredBlock<AbyssWaterPlantBlock> GREEN_SEAGRASS = water("green_seagrass", 0);
    public static final DeferredBlock<AbyssWaterPlantBlock> CYAN_SEAGRASS = water("cyan_seagrass", 0);
    public static final DeferredBlock<AbyssWaterPlantBlock> RED_SEAGRASS = water("red_seagrass", 0);
    public static final DeferredBlock<AbyssWaterPlantBlock> BLUE_SEAGRASS = water("blue_seagrass", 0);
    public static final DeferredBlock<AbyssWaterPlantBlock> RED_CORAL = water("red_coral", 0);
    public static final DeferredBlock<AbyssWaterPlantBlock> PURPLE_CORAL = water("purple_coral", 0);
    public static final DeferredBlock<AbyssWaterPlantBlock> BLUE_AQUA_FLOWER = water("blue_aqua_flower", 6);
    public static final DeferredBlock<AbyssWaterPlantBlock> PINK_AQUA_FLOWER = water("pink_aqua_flower", 6);

    // --- Кувшинка ---
    public static final DeferredBlock<WaterlilyBlock> LILY_PAD_FLOWER = Abyss.BLOCKS.registerBlock(
            "lily_pad_flower", WaterlilyBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).instabreak()
                    .sound(SoundType.LILY_PAD).noOcclusion().pushReaction(PushReaction.DESTROY));
    public static final DeferredItem<Item> LILY_PAD_FLOWER_ITEM = Abyss.ITEMS.registerItem("lily_pad_flower",
            props -> new PlaceOnWaterBlockItem(LILY_PAD_FLOWER.get(), props.useBlockDescriptionPrefix()),
            new Item.Properties());
    static { ITEM_LIST.add(LILY_PAD_FLOWER_ITEM); }

    private static BlockBehaviour.Properties base(int light) {
        return BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().instabreak()
                .sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY).lightLevel(state -> light);
    }

    private static DeferredBlock<AbyssPlantBlock> plant(String id, int light) {
        DeferredBlock<AbyssPlantBlock> block = Abyss.BLOCKS.registerBlock(id, AbyssPlantBlock::new,
                base(light).offsetType(BlockBehaviour.OffsetType.XZ));
        ITEM_LIST.add(Abyss.ITEMS.registerSimpleBlockItem(id, block));
        return block;
    }

    private static DeferredBlock<AbyssVineBlock> vine(String id, int light) {
        DeferredBlock<AbyssVineBlock> block = Abyss.BLOCKS.registerBlock(id, AbyssVineBlock::new, base(light));
        ITEM_LIST.add(Abyss.ITEMS.registerSimpleBlockItem(id, block));
        return block;
    }

    private static DeferredBlock<AbyssWaterPlantBlock> water(String id, int light) {
        DeferredBlock<AbyssWaterPlantBlock> block = Abyss.BLOCKS.registerBlock(id, AbyssWaterPlantBlock::new, base(light));
        ITEM_LIST.add(Abyss.ITEMS.registerSimpleBlockItem(id, block));
        return block;
    }
}