package com.akashi.abyss.abyss;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import com.akashi.abyss.abyss.item.RawElementItem;
import net.minecraft.world.level.block.UntintedParticleLeavesBlock;
import net.minecraft.core.particles.DustParticleOptions;
import com.akashi.abyss.abyss.item.AntidoteFruitItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import com.akashi.abyss.abyss.item.PaleWardEffect;
import java.util.Optional;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import com.akashi.abyss.abyss.block.AbyssForgeBlockEntity;
import com.akashi.abyss.abyss.block.AbyssForgeBlock;
import com.akashi.abyss.abyss.block.AbyssForgeMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import java.util.Set;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import com.akashi.abyss.abyss.block.EmptyOreBlock;

import com.akashi.abyss.abyss.item.EarthStaffItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.equipment.ArmorType;
import java.util.List;
import com.akashi.abyss.abyss.entity.EarthStoneProjectile;
import com.akashi.abyss.abyss.item.EarthEyeItem;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.minecraft.world.item.DyeColor;
import com.akashi.abyss.abyss.entity.WaterBladeProjectile;
import com.akashi.abyss.abyss.item.WaterEyeItem;
import com.akashi.abyss.abyss.item.WaterStaffItem;
import com.akashi.abyss.abyss.item.FireEyeItem;
import com.akashi.abyss.abyss.item.FireStaffItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.Util;
import java.util.EnumMap;
import net.minecraft.tags.TagKey;
import com.akashi.abyss.abyss.item.WindSwordItem;
import com.akashi.abyss.abyss.item.WindStaffItem;
import com.akashi.abyss.abyss.item.WindEyeItem;
import com.akashi.abyss.abyss.item.LightningSwordItem;
import com.akashi.abyss.abyss.item.LightningStaffItem;
import com.akashi.abyss.abyss.item.LightningEyeItem;
import com.akashi.abyss.abyss.entity.IceBladeProjectile;
import com.akashi.abyss.abyss.item.EternalFrostSwordItem;
import com.akashi.abyss.abyss.client.IceBladeRenderer;
import com.akashi.abyss.abyss.item.BlackHoleStaffItem;
import com.akashi.abyss.abyss.entity.BlackHoleEntity;
import com.akashi.abyss.abyss.client.BlackHoleRenderer;
import com.akashi.abyss.abyss.item.SpectatorChestplateItem;
import com.akashi.abyss.abyss.event.SpectatorChestplateEvents;
import com.akashi.abyss.abyss.client.SpectatorClientEvents;
import com.akashi.abyss.abyss.item.EnergyBlasterItem;
import com.akashi.abyss.abyss.entity.EnergyBlastProjectile;
import com.akashi.abyss.abyss.block.EternalFrostSwordPedestalBlock;
import net.minecraft.world.item.BlockItem;
import com.akashi.abyss.abyss.block.EternalFrostSwordPedestalBlockEntity;
import com.akashi.abyss.abyss.client.EternalFrostSwordPedestalRenderer;
import com.akashi.abyss.abyss.entity.ModEntities;
import com.akashi.abyss.abyss.block.DinosaurEggBlock;
import com.akashi.abyss.abyss.block.entity.DinosaurEggBlockEntity;
import com.akashi.abyss.abyss.block.MosasaurusEggBlock;
import com.akashi.abyss.abyss.entity.water.XiphactinusRenderer;
import com.akashi.abyss.abyss.entity.water.ElasmosaurusRenderer;
import com.akashi.abyss.abyss.block.ElasmosaurusEggBlock;
import com.akashi.abyss.abyss.entity.water.ArchelonRenderer;
import com.akashi.abyss.abyss.entity.water.TrilobiteRenderer;
import com.akashi.abyss.abyss.entity.water.OrthocerasRenderer;
import com.akashi.abyss.abyss.entity.heaven.QuetzalcoatlusRenderer;

@Mod(Abyss.MODID)
public class Abyss {
    public static final String MODID = "abyss";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, MODID);

    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> USES =
            DATA_COMPONENTS.registerComponentType("uses",
                    builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    // --- Деревья для саженцев (файлы лежат в data/abyss/worldgen/configured_feature/) ---
    private static TreeGrower grower(String name, String featureId) {
        return new TreeGrower(
                name,
                Optional.empty(),
                Optional.of(ResourceKey.create(Registries.CONFIGURED_FEATURE,
                        ResourceLocation.fromNamespaceAndPath(MODID, featureId))),
                Optional.empty());
    }

    private static final TreeGrower ABYSS_GROWER = grower("abyss_tree", "abyss_tree");
    private static final TreeGrower CHARRED_GROWER = grower("charred_tree", "charred_tree");
    private static final TreeGrower WHITE_GROWER = grower("white_tree", "white_tree");

    // Стандартные примеры
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock(
            "example_block", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);

    // Почва и трава Бездны
    public static final DeferredBlock<Block> ABYSS_GRASS = BLOCKS.registerSimpleBlock(
            "abyss_grass", BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(0.6f).sound(SoundType.GRAVEL));
    public static final DeferredItem<BlockItem> ABYSS_GRASS_ITEM = ITEMS.registerSimpleBlockItem("abyss_grass", ABYSS_GRASS);

    public static final DeferredBlock<Block> ABYSS_GRASS_WET = BLOCKS.registerSimpleBlock(
            "abyss_grass_wet", BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(0.6f).sound(SoundType.GRAVEL));
    public static final DeferredItem<BlockItem> ABYSS_GRASS_WET_ITEM = ITEMS.registerSimpleBlockItem("abyss_grass_wet", ABYSS_GRASS_WET);

    public static final DeferredBlock<Block> ABYSS_SOIL = BLOCKS.registerSimpleBlock(
            "abyss_soil", BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.5f).sound(SoundType.GRAVEL));
    public static final DeferredItem<BlockItem> ABYSS_SOIL_ITEM = ITEMS.registerSimpleBlockItem("abyss_soil", ABYSS_SOIL);

    public static final DeferredBlock<Block> ABYSS_CHARRED_GROUND = BLOCKS.registerSimpleBlock(
            "abyss_charred_ground", BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(1.2f).sound(SoundType.GRAVEL));
    public static final DeferredItem<BlockItem> ABYSS_CHARRED_GROUND_ITEM = ITEMS.registerSimpleBlockItem("abyss_charred_ground", ABYSS_CHARRED_GROUND);

    public static final DeferredBlock<Block> ABYSS_LIGHT_STONE = BLOCKS.registerSimpleBlock(
            "abyss_light_stone", BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(2.0f).lightLevel(state -> 15));
    public static final DeferredItem<BlockItem> ABYSS_LIGHT_STONE_ITEM = ITEMS.registerSimpleBlockItem("abyss_light_stone", ABYSS_LIGHT_STONE);

    // --- Брёвна (RotatedPillarBlock: у них есть свойство axis) ---
    public static final DeferredBlock<RotatedPillarBlock> ABYSS_LOG = BLOCKS.registerBlock(
            "abyss_log", RotatedPillarBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f).sound(SoundType.WOOD));
    public static final DeferredItem<BlockItem> ABYSS_LOG_ITEM = ITEMS.registerSimpleBlockItem("abyss_log", ABYSS_LOG);

    public static final DeferredBlock<RotatedPillarBlock> ABYSS_CHARRED_LOG = BLOCKS.registerBlock(
            "abyss_charred_log", RotatedPillarBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(2.5f).sound(SoundType.WOOD));
    public static final DeferredItem<BlockItem> ABYSS_CHARRED_LOG_ITEM = ITEMS.registerSimpleBlockItem("abyss_charred_log", ABYSS_CHARRED_LOG);

    public static final DeferredBlock<RotatedPillarBlock> ABYSS_WHITE_LOG = BLOCKS.registerBlock(
            "abyss_white_log", RotatedPillarBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.QUARTZ).strength(2.0f).sound(SoundType.WOOD));
    public static final DeferredItem<BlockItem> ABYSS_WHITE_LOG_ITEM = ITEMS.registerSimpleBlockItem("abyss_white_log", ABYSS_WHITE_LOG);

    // --- Листья (LeavesBlock абстрактный, поэтому TintedParticleLeavesBlock) ---
    public static final DeferredBlock<LeavesBlock> RED_LEAVES = BLOCKS.registerBlock(
            "red_leaves",
            props -> new UntintedParticleLeavesBlock(0.01f,
                    new DustParticleOptions(0xC7291A, 1.1f), props),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(0.2f)
                    .randomTicks().sound(SoundType.GRASS).noOcclusion());
    public static final DeferredItem<BlockItem> RED_LEAVES_ITEM = ITEMS.registerSimpleBlockItem("red_leaves", RED_LEAVES);

    public static final DeferredBlock<LeavesBlock> YELLOW_LEAVES = BLOCKS.registerBlock(
            "yellow_leaves",
            props -> new UntintedParticleLeavesBlock(0.01f,
                    new DustParticleOptions(0xE0C226, 1.1f), props),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(0.2f)
                    .randomTicks().sound(SoundType.GRASS).noOcclusion());
    public static final DeferredItem<BlockItem> YELLOW_LEAVES_ITEM = ITEMS.registerSimpleBlockItem("yellow_leaves", YELLOW_LEAVES);

    public static final DeferredBlock<LeavesBlock> GREEN_LEAVES = BLOCKS.registerBlock(
            "green_leaves",
            props -> new UntintedParticleLeavesBlock(0.01f,
                    new DustParticleOptions(0x529438, 1.1f), props),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(0.2f)
                    .randomTicks().sound(SoundType.GRASS).noOcclusion());
    public static final DeferredItem<BlockItem> GREEN_LEAVES_ITEM = ITEMS.registerSimpleBlockItem("green_leaves", GREEN_LEAVES);

    public static final DeferredBlock<LeavesBlock> PURPLE_LEAVES = BLOCKS.registerBlock(
            "purple_leaves",
            props -> new UntintedParticleLeavesBlock(0.01f,
                    new DustParticleOptions(0x8C33A6, 1.1f), props),
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(0.2f)
                    .randomTicks().sound(SoundType.GRASS).noOcclusion());
    public static final DeferredItem<BlockItem> PURPLE_LEAVES_ITEM = ITEMS.registerSimpleBlockItem("purple_leaves", PURPLE_LEAVES);

    // ===============================
// БОЛЬШИЕ ЯЙЦА ДИНОЗАВРОВ
// ===============================

    public static final DeferredBlock<DinosaurEggBlock> TRICERATOPS_EGG =
            BLOCKS.registerBlock(
                    "triceratops_egg",
                    props -> new DinosaurEggBlock(
                            15.6F,
                            18.2F,
                            ModEntities.TRICERATOPS::get,
                            props
                    ),
                    BlockBehaviour.Properties.of()
                            .strength(0.5F)
                            .sound(SoundType.STONE)
                            .noOcclusion()
            );

    public static final DeferredBlock<DinosaurEggBlock> STEGOSAURUS_EGG =
            BLOCKS.registerBlock(
                    "stegosaurus_egg",
                    props -> new DinosaurEggBlock(
                            15.6F,
                            18.2F,
                            ModEntities.STEGOSAURUS::get,
                            props
                    ),
                    BlockBehaviour.Properties.of()
                            .strength(0.5F)
                            .sound(SoundType.STONE)
                            .noOcclusion()
            );

    public static final DeferredBlock<DinosaurEggBlock> PARASAUROLOPHUS_EGG =
            BLOCKS.registerBlock(
                    "parasaurolophus_egg",
                    props -> new DinosaurEggBlock(
                            15.6F,
                            18.2F,
                            ModEntities.PARASAUROLOPHUS::get,
                            props
                    ),
                    BlockBehaviour.Properties.of()
                            .strength(0.5F)
                            .sound(SoundType.STONE)
                            .noOcclusion()
            );

    public static final DeferredBlock<DinosaurEggBlock> ANKYLOSAURUS_EGG =
            BLOCKS.registerBlock(
                    "ankylosaurus_egg",
                    props -> new DinosaurEggBlock(
                            15.6F,
                            18.2F,
                            ModEntities.ANKYLOSAURUS::get,
                            props
                    ),
                    BlockBehaviour.Properties.of()
                            .strength(0.5F)
                            .sound(SoundType.STONE)
                            .noOcclusion()
            );

    public static final DeferredBlock<DinosaurEggBlock> BRACHIOSAURUS_EGG =
            BLOCKS.registerBlock(
                    "brachiosaurus_egg",
                    props -> new DinosaurEggBlock(
                            15.6F,
                            18.2F,
                            ModEntities.BRACHIOSAURUS::get,
                            props
                    ),
                    BlockBehaviour.Properties.of()
                            .strength(0.5F)
                            .sound(SoundType.STONE)
                            .noOcclusion()
            );
    public static final DeferredBlock<DinosaurEggBlock> TYRANNOSAURUS_EGG =
            BLOCKS.registerBlock(
                    "tyrannosaurus_egg",
                    props -> new DinosaurEggBlock(
                            15.6F,
                            18.2F,
                            ModEntities.TYRANNOSAURUS::get,
                            props
                    ),
                    BlockBehaviour.Properties.of()
                            .strength(0.5F)
                            .sound(SoundType.STONE)
                            .noOcclusion()
            );
    // Яйцо спинозавра

    public static final DeferredBlock<DinosaurEggBlock> SPINOSAURUS_EGG =
            BLOCKS.registerBlock(
                    "spinosaurus_egg",
                    props -> new DinosaurEggBlock(
                            15.6F,
                            18.2F,
                            ModEntities.SPINOSAURUS::get,
                            props
                    ),
                    BlockBehaviour.Properties.of()
                            .strength(0.5F)
                            .sound(SoundType.STONE)
                            .noOcclusion()
            );

    public static final DeferredBlock<DinosaurEggBlock> GIGANOTOSAURUS_EGG =
            BLOCKS.registerBlock(
                    "giganotosaurus_egg",
                    props -> new DinosaurEggBlock(
                            15.6F,
                            18.2F,
                            ModEntities.GIGANOTOSAURUS::get,
                            props
                    ),
                    BlockBehaviour.Properties.of()
                            .strength(0.5F)
                            .sound(SoundType.STONE)
                            .noOcclusion()
            );
    public static final DeferredBlock<DinosaurEggBlock> THERIZINOSAURUS_EGG =
            BLOCKS.registerBlock("therizinosaurus_egg",
                    props -> new DinosaurEggBlock(15.6F, 18.2F,
                            ModEntities.THERIZINOSAURUS::get, props),
                    BlockBehaviour.Properties.of().strength(0.5F)
                            .sound(SoundType.STONE).noOcclusion());



    public static final DeferredBlock<MosasaurusEggBlock> MOSASAURUS_EGG =
            BLOCKS.registerBlock(
                    "mosasaurus_egg",
                    MosasaurusEggBlock::new,
                    BlockBehaviour.Properties.of()
                            .strength(0.5F)
                            .sound(SoundType.STONE)
                            .noOcclusion()
            );
    public static final DeferredBlock<ElasmosaurusEggBlock> ELASMOSAURUS_EGG =
            BLOCKS.registerBlock(
                    "elasmosaurus_egg",
                    ElasmosaurusEggBlock::new,
                    BlockBehaviour.Properties.of()
                            .strength(0.5F)
                            .sound(SoundType.STONE)
                            .noOcclusion()
            );
    public static final DeferredBlock<DinosaurEggBlock> ARCHELON_EGG =
            BLOCKS.registerBlock(
                    "archelon_egg",
                    properties -> new DinosaurEggBlock(
                            12.0F,
                            14.0F,
                            ModEntities.ARCHELON::get,
                            properties
                    ),
                    BlockBehaviour.Properties.of()
                            .strength(0.5F)
                            .sound(SoundType.STONE)
                            .noOcclusion()
            );
    // Инкубационное яйцо Кетцалькоатля
    public static final DeferredBlock<DinosaurEggBlock> QUETZALCOATLUS_EGG =
            BLOCKS.registerBlock("quetzalcoatlus_egg",
                    props -> new DinosaurEggBlock(12.0F, 15.0F,
                            ModEntities.QUETZALCOATLUS::get, props),
                    BlockBehaviour.Properties.of().strength(0.5F)
                            .sound(SoundType.STONE).noOcclusion());

    // --- Саженцы (растут в настоящие деревья) ---
    public static final DeferredBlock<SaplingBlock> ABYSS_SAPLING = BLOCKS.registerBlock(
            "abyss_sapling",
            props -> new SaplingBlock(ABYSS_GROWER, props),
            BlockBehaviour.Properties.of().noCollision().randomTicks()
                    .strength(0.0f).sound(SoundType.GRASS));
    public static final DeferredItem<BlockItem> ABYSS_SAPLING_ITEM = ITEMS.registerSimpleBlockItem("abyss_sapling", ABYSS_SAPLING);

    public static final DeferredBlock<SaplingBlock> CHARRED_SAPLING = BLOCKS.registerBlock(
            "charred_sapling",
            props -> new SaplingBlock(CHARRED_GROWER, props),
            BlockBehaviour.Properties.of().noCollision().randomTicks()
                    .strength(0.0f).sound(SoundType.GRASS));
    public static final DeferredItem<BlockItem> CHARRED_SAPLING_ITEM = ITEMS.registerSimpleBlockItem("charred_sapling", CHARRED_SAPLING);

    public static final DeferredBlock<SaplingBlock> WHITE_SAPLING = BLOCKS.registerBlock(
            "white_sapling",
            props -> new SaplingBlock(WHITE_GROWER, props),
            BlockBehaviour.Properties.of().noCollision().randomTicks()
                    .strength(0.0f).sound(SoundType.GRASS));
    public static final DeferredItem<BlockItem> WHITE_SAPLING_ITEM = ITEMS.registerSimpleBlockItem("white_sapling", WHITE_SAPLING);

    // --- Элементальные руды (блоки) ---
    public static final DeferredBlock<Block> EARTH_ORE = BLOCKS.registerSimpleBlock(
            "earth_ore", BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(3.0f).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> EARTH_ORE_ITEM = ITEMS.registerSimpleBlockItem("earth_ore", EARTH_ORE);

    public static final DeferredBlock<Block> FIRE_ORE = BLOCKS.registerSimpleBlock(
            "fire_ore", BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(3.5f).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> FIRE_ORE_ITEM = ITEMS.registerSimpleBlockItem("fire_ore", FIRE_ORE);

    public static final DeferredBlock<Block> WATER_ORE = BLOCKS.registerSimpleBlock(
            "water_ore", BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(3.5f).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> WATER_ORE_ITEM = ITEMS.registerSimpleBlockItem("water_ore", WATER_ORE);

    public static final DeferredBlock<Block> WIND_ORE = BLOCKS.registerSimpleBlock(
            "wind_ore", BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_GRAY).strength(3.0f).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> WIND_ORE_ITEM = ITEMS.registerSimpleBlockItem("wind_ore", WIND_ORE);

    public static final DeferredBlock<Block> LIGHTNING_ORE = BLOCKS.registerSimpleBlock(
            "lightning_ore", BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(4.0f).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> LIGHTNING_ORE_ITEM = ITEMS.registerSimpleBlockItem("lightning_ore", LIGHTNING_ORE);

    // --- Сырая руда (предмет-дроп; у огня, воды, ветра и молнии есть выстрел) ---
    public static final DeferredItem<Item> EARTH_RAW =
            ITEMS.registerItem(
                    "earth_raw",
                    props -> new RawElementItem(
                            props,
                            RawElementItem.Element.EARTH
                    ),
                    new Item.Properties()
            );
    public static final DeferredItem<Item> FIRE_RAW = ITEMS.registerItem("fire_raw",
            props -> new RawElementItem(props, RawElementItem.Element.FIRE),
            new Item.Properties());
    public static final DeferredItem<Item> WATER_RAW = ITEMS.registerItem("water_raw",
            props -> new RawElementItem(props, RawElementItem.Element.WATER),
            new Item.Properties());
    public static final DeferredItem<Item> WIND_RAW = ITEMS.registerItem("wind_raw",
            props -> new RawElementItem(props, RawElementItem.Element.WIND),
            new Item.Properties());
    public static final DeferredItem<Item> LIGHTNING_RAW = ITEMS.registerItem("lightning_raw",
            props -> new RawElementItem(props, RawElementItem.Element.LIGHTNING),
            new Item.Properties());

    // --- Слитки (эффектов нет) ---
    public static final DeferredItem<Item> EARTH_INGOT = ITEMS.registerSimpleItem("earth_ingot", new Item.Properties());
    public static final DeferredItem<Item> FIRE_INGOT = ITEMS.registerSimpleItem("fire_ingot", new Item.Properties());
    public static final DeferredItem<Item> WATER_INGOT = ITEMS.registerSimpleItem("water_ingot", new Item.Properties());
    public static final DeferredItem<Item> WIND_INGOT = ITEMS.registerSimpleItem("wind_ingot", new Item.Properties());
    public static final DeferredItem<Item> LIGHTNING_INGOT = ITEMS.registerSimpleItem("lightning_ingot", new Item.Properties());

    // --- Драгоценные руды ---
    public static final DeferredBlock<Block> RUBY_ORE = BLOCKS.registerSimpleBlock(
            "ruby_ore", BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(3.5f).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> RUBY_ORE_ITEM = ITEMS.registerSimpleBlockItem("ruby_ore", RUBY_ORE);

    public static final DeferredBlock<Block> SAPPHIRE_ORE = BLOCKS.registerSimpleBlock(
            "sapphire_ore", BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(3.5f).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> SAPPHIRE_ORE_ITEM = ITEMS.registerSimpleBlockItem("sapphire_ore", SAPPHIRE_ORE);

    public static final DeferredBlock<Block> AMBER_ORE = BLOCKS.registerSimpleBlock(
            "amber_ore", BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(3.0f).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> AMBER_ORE_ITEM = ITEMS.registerSimpleBlockItem("amber_ore", AMBER_ORE);

    public static final DeferredItem<Item> RUBY = ITEMS.registerSimpleItem("ruby", new Item.Properties());
    public static final DeferredItem<Item> SAPPHIRE = ITEMS.registerSimpleItem("sapphire", new Item.Properties());
    public static final DeferredItem<Item> AMBER = ITEMS.registerSimpleItem("amber", new Item.Properties());

    // --- Сломанные слитки ---
    public static final DeferredItem<Item> EARTH_BROKEN_INGOT = ITEMS.registerSimpleItem("earth_broken_ingot", new Item.Properties());
    public static final DeferredItem<Item> FIRE_BROKEN_INGOT = ITEMS.registerSimpleItem("fire_broken_ingot", new Item.Properties());
    public static final DeferredItem<Item> WATER_BROKEN_INGOT = ITEMS.registerSimpleItem("water_broken_ingot", new Item.Properties());
    public static final DeferredItem<Item> WIND_BROKEN_INGOT = ITEMS.registerSimpleItem("wind_broken_ingot", new Item.Properties());
    public static final DeferredItem<Item> LIGHTNING_BROKEN_INGOT = ITEMS.registerSimpleItem("lightning_broken_ingot", new Item.Properties());

    // --- Кузница Бездны ---
    public static final DeferredBlock<AbyssForgeBlock> ABYSS_FORGE = BLOCKS.registerBlock("abyss_forge",
            AbyssForgeBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(5.0f, 1200.0f).sound(SoundType.STONE)
                    .lightLevel(state -> state.getValue(AbyssForgeBlock.LIT) ? 15 : 0));
    public static final DeferredItem<BlockItem> ABYSS_FORGE_ITEM = ITEMS.registerSimpleBlockItem("abyss_forge", ABYSS_FORGE);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AbyssForgeBlockEntity>> ABYSS_FORGE_BE =
            BLOCK_ENTITY_TYPES.register("abyss_forge",
                    () -> new BlockEntityType<>(AbyssForgeBlockEntity::new, Set.of(ABYSS_FORGE.get())));

    public static final DeferredHolder<MenuType<?>, MenuType<AbyssForgeMenu>> ABYSS_FORGE_MENU =
            MENU_TYPES.register("abyss_forge", () -> IMenuTypeExtension.create(AbyssForgeMenu::new));

    // Предметы и артефакты
    public static final DeferredItem<Item> EXAMPLE_ITEM = ITEMS.registerSimpleItem("example_item",
            new Item.Properties().food(new FoodProperties.Builder().alwaysEdible().nutrition(1).saturationModifier(2f).build()));
    public static final DeferredItem<Item> ABYSS_ARTIFACT = ITEMS.registerSimpleItem("abyss_artifact",
            new Item.Properties().stacksTo(16));

    public static final DeferredBlock<RotatedPillarBlock> BAOBAB_LOG = BLOCKS.registerBlock(
            "baobab_log", RotatedPillarBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(6.0f).sound(SoundType.WOOD));
    public static final DeferredItem<BlockItem> BAOBAB_LOG_ITEM = ITEMS.registerSimpleBlockItem("baobab_log", BAOBAB_LOG);

    public static final DeferredBlock<EmptyOreBlock> EMPTY_ORE = BLOCKS.registerBlock("empty_ore",
            EmptyOreBlock::new,
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0f).requiresCorrectToolForDrops());
    public static final DeferredItem<BlockItem> EMPTY_ORE_ITEM =
            ITEMS.registerSimpleBlockItem("empty_ore", EMPTY_ORE);

    public static final DeferredItem<Item> BAOBAB_FRUIT = ITEMS.registerSimpleItem("baobab_fruit",
            new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.3f).build()));

    public static final DeferredHolder<MobEffect, MobEffect> PALE_WARD = MOB_EFFECTS.register("pale_ward",
            () -> new PaleWardEffect(MobEffectCategory.BENEFICIAL, 0x9BD2AA));

    public static final DeferredItem<Item> PALE_ANTIDOTE_FRUIT = ITEMS.registerItem("pale_antidote_fruit",
            props -> new AntidoteFruitItem(props.food(
                    new FoodProperties.Builder()
                            .nutrition(3)
                            .saturationModifier(0.4f)
                            .build(),
                    Consumables.defaultFood()
                            .onConsume(new ApplyStatusEffectsConsumeEffect(
                                    new MobEffectInstance(PALE_WARD, 3800, 0), 1.0f))
                            .build())),
            new Item.Properties());

    // --- Земляной набор ---
    public static final DeferredItem<Item> EARTH_SWORD = ITEMS.registerItem("earth_sword",
            p -> new Item(p.sword(EarthMaterials.EARTH_TOOL, 17.0F, -5.4F)));

    public static final DeferredItem<Item> EARTH_PICKAXE = ITEMS.registerItem("earth_pickaxe",
            p -> new Item(p.pickaxe(EarthMaterials.EARTH_TOOL, 1.0F, -2.8F)));

    public static final DeferredItem<AxeItem> EARTH_AXE = ITEMS.registerItem("earth_axe",
            p -> new AxeItem(EarthMaterials.EARTH_TOOL, 6.0F, -3.1F, p));

    public static final DeferredItem<ShovelItem> EARTH_SHOVEL = ITEMS.registerItem("earth_shovel",
            p -> new ShovelItem(EarthMaterials.EARTH_TOOL, 1.5F, -3.0F, p));

    public static final DeferredItem<HoeItem> EARTH_HOE = ITEMS.registerItem("earth_hoe",
            p -> new HoeItem(EarthMaterials.EARTH_TOOL, -2.0F, -1.0F, p));

    public static final DeferredItem<BowItem> EARTH_BOW = ITEMS.registerItem("earth_bow",
            p -> new BowItem(p.durability(5000).repairable(EarthMaterials.REPAIRS_EARTH)));

    public static final DeferredItem<Item> EARTH_HELMET = ITEMS.registerItem("earth_helmet",
            p -> new Item(p.humanoidArmor(EarthMaterials.EARTH_ARMOR, ArmorType.HELMET)
                    .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)));

    public static final DeferredItem<Item> EARTH_CHESTPLATE = ITEMS.registerItem("earth_chestplate",
            p -> new Item(p.humanoidArmor(EarthMaterials.EARTH_ARMOR, ArmorType.CHESTPLATE)
                    .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)));

    public static final DeferredItem<Item> EARTH_LEGGINGS = ITEMS.registerItem("earth_leggings",
            p -> new Item(p.humanoidArmor(EarthMaterials.EARTH_ARMOR, ArmorType.LEGGINGS)
                    .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)));

    public static final DeferredItem<Item> EARTH_BOOTS = ITEMS.registerItem("earth_boots",
            p -> new Item(p.humanoidArmor(EarthMaterials.EARTH_ARMOR, ArmorType.BOOTS)
                    .component(DataComponents.UNBREAKABLE, Unit.INSTANCE)));

    public static final DeferredItem<Item> EARTH_SHIELD = ITEMS.registerItem("earth_shield",
            p -> new Item(p
                    .durability(5000)
                    .component(DataComponents.BASE_COLOR, DyeColor.BROWN)
                    .repairable(EarthMaterials.REPAIRS_EARTH)
                    .component(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(
                            0.25F, 1.0F,
                            List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, 1.0F)),
                            new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
                            Optional.of(DamageTypeTags.BYPASSES_SHIELD),
                            Optional.of(SoundEvents.SHIELD_BLOCK),
                            Optional.of(SoundEvents.SHIELD_BREAK)))));

    public static final DeferredItem<EarthStaffItem> EARTH_STAFF = ITEMS.registerItem("earth_staff",
            p -> new EarthStaffItem(p.stacksTo(1).rarity(Rarity.RARE)));

    public static final DeferredItem<EarthEyeItem> EARTH_EYE = ITEMS.registerItem("earth_eye",
            p -> new EarthEyeItem(p.stacksTo(1).rarity(Rarity.EPIC)));

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<EarthStoneProjectile>> EARTH_STONE =
            ENTITY_TYPES.register("earth_stone", () -> EntityType.Builder
                    .<EarthStoneProjectile>of(EarthStoneProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,
                            ResourceLocation.fromNamespaceAndPath(MODID, "earth_stone"))));

    public static final DeferredHolder<EntityType<?>, EntityType<WaterBladeProjectile>> WATER_BLADE =
            ENTITY_TYPES.register("water_blade", () -> EntityType.Builder
                    .<WaterBladeProjectile>of(WaterBladeProjectile::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE,
                            ResourceLocation.fromNamespaceAndPath(MODID, "water_blade"))));

    // --- Набор воды ---
    // Меч: атрибут 6 + бонус материала 3 = урон +9
    public static final DeferredItem<Item> WATER_SWORD = ITEMS.registerItem("water_sword",
            p -> new Item(p.sword(WaterMaterials.WATER_TOOL, 6.0F, -2.4F)));

    public static final DeferredItem<Item> WATER_PICKAXE = ITEMS.registerItem("water_pickaxe",
            p -> new Item(p.pickaxe(WaterMaterials.WATER_TOOL, 1.0F, -2.8F)));

    public static final DeferredItem<AxeItem> WATER_AXE = ITEMS.registerItem("water_axe",
            p -> new AxeItem(WaterMaterials.WATER_TOOL, 5.0F, -3.0F, p));

    public static final DeferredItem<ShovelItem> WATER_SHOVEL = ITEMS.registerItem("water_shovel",
            p -> new ShovelItem(WaterMaterials.WATER_TOOL, 1.5F, -3.0F, p));

    public static final DeferredItem<HoeItem> WATER_HOE = ITEMS.registerItem("water_hoe",
            p -> new HoeItem(WaterMaterials.WATER_TOOL, -3.0F, 0.0F, p));

    public static final DeferredItem<BowItem> WATER_BOW = ITEMS.registerItem("water_bow",
            p -> new BowItem(p.durability(1000).repairable(WaterMaterials.REPAIRS_WATER)));

    public static final DeferredItem<Item> WATER_HELMET = ITEMS.registerItem("water_helmet",
            p -> new Item(p.humanoidArmor(WaterMaterials.WATER_ARMOR, ArmorType.HELMET)));

    public static final DeferredItem<Item> WATER_CHESTPLATE = ITEMS.registerItem("water_chestplate",
            p -> new Item(p.humanoidArmor(WaterMaterials.WATER_ARMOR, ArmorType.CHESTPLATE)));

    public static final DeferredItem<Item> WATER_LEGGINGS = ITEMS.registerItem("water_leggings",
            p -> new Item(p.humanoidArmor(WaterMaterials.WATER_ARMOR, ArmorType.LEGGINGS)));

    public static final DeferredItem<Item> WATER_BOOTS = ITEMS.registerItem("water_boots",
            p -> new Item(p.humanoidArmor(WaterMaterials.WATER_ARMOR, ArmorType.BOOTS)));

    // Щит воды: прочность 1200 (у земли 5000), но защищает в более широком секторе (130 градусов
    // вместо 90), поднимается быстрее и блокирует урон снарядов и оружия полностью.
    public static final DeferredItem<Item> WATER_SHIELD = ITEMS.registerItem("water_shield",
            p -> new Item(p
                    .durability(1200)
                    .repairable(WaterMaterials.REPAIRS_WATER)
                    .component(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(
                            0.1F, 1.0F,
                            List.of(new BlocksAttacks.DamageReduction(130.0F, Optional.empty(), 0.0F, 1.0F)),
                            new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 0.5F),
                            Optional.of(DamageTypeTags.BYPASSES_SHIELD),
                            Optional.of(SoundEvents.SHIELD_BLOCK),
                            Optional.of(SoundEvents.SHIELD_BREAK)))));

    public static final DeferredItem<WaterStaffItem> WATER_STAFF = ITEMS.registerItem("water_staff",
            p -> new WaterStaffItem(p.stacksTo(1).rarity(Rarity.RARE)));

    public static final DeferredItem<WaterEyeItem> WATER_EYE = ITEMS.registerItem("water_eye",
            p -> new WaterEyeItem(p.stacksTo(1).rarity(Rarity.EPIC)));

    // --- Набор огня ---
    // Меч: атрибут 7 + бонус материала 4 = урон +11 (плюс +4 огнём в FireGearEvents)
    public static final DeferredItem<Item> FIRE_SWORD = ITEMS.registerItem("fire_sword",
            p -> new Item(p.sword(FireMaterials.FIRE_TOOL, 7.0F, -2.4F).fireResistant()));

    public static final DeferredItem<Item> FIRE_PICKAXE = ITEMS.registerItem("fire_pickaxe",
            p -> new Item(p.pickaxe(FireMaterials.FIRE_TOOL, 1.0F, -2.8F).fireResistant()));

    public static final DeferredItem<AxeItem> FIRE_AXE = ITEMS.registerItem("fire_axe",
            p -> new AxeItem(FireMaterials.FIRE_TOOL, 5.0F, -3.0F, p.fireResistant()));

    public static final DeferredItem<ShovelItem> FIRE_SHOVEL = ITEMS.registerItem("fire_shovel",
            p -> new ShovelItem(FireMaterials.FIRE_TOOL, 1.5F, -3.0F, p.fireResistant()));

    public static final DeferredItem<HoeItem> FIRE_HOE = ITEMS.registerItem("fire_hoe",
            p -> new HoeItem(FireMaterials.FIRE_TOOL, -4.0F, 0.0F, p.fireResistant()));

    public static final DeferredItem<BowItem> FIRE_BOW = ITEMS.registerItem("fire_bow",
            p -> new BowItem(p.durability(2031).repairable(FireMaterials.REPAIRS_FIRE).fireResistant()));

    public static final DeferredItem<Item> FIRE_HELMET = ITEMS.registerItem("fire_helmet",
            p -> new Item(p.humanoidArmor(FireMaterials.FIRE_ARMOR, ArmorType.HELMET).fireResistant()));

    public static final DeferredItem<Item> FIRE_CHESTPLATE = ITEMS.registerItem("fire_chestplate",
            p -> new Item(p.humanoidArmor(FireMaterials.FIRE_ARMOR, ArmorType.CHESTPLATE).fireResistant()));

    public static final DeferredItem<Item> FIRE_LEGGINGS = ITEMS.registerItem("fire_leggings",
            p -> new Item(p.humanoidArmor(FireMaterials.FIRE_ARMOR, ArmorType.LEGGINGS).fireResistant()));

    public static final DeferredItem<Item> FIRE_BOOTS = ITEMS.registerItem("fire_boots",
            p -> new Item(p.humanoidArmor(FireMaterials.FIRE_ARMOR, ArmorType.BOOTS).fireResistant()));

    // Щит огня: обычный сектор блока 90 градусов; обжигание атакующего считается в FireGearEvents
    public static final DeferredItem<Item> FIRE_SHIELD = ITEMS.registerItem("fire_shield",
            p -> new Item(p
                    .durability(2031)
                    .repairable(FireMaterials.REPAIRS_FIRE)
                    .fireResistant()
                    .component(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(
                            0.25F, 1.0F,
                            List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, 1.0F)),
                            new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
                            Optional.of(DamageTypeTags.BYPASSES_SHIELD),
                            Optional.of(SoundEvents.SHIELD_BLOCK),
                            Optional.of(SoundEvents.SHIELD_BREAK)))));

    public static final DeferredItem<FireStaffItem> FIRE_STAFF = ITEMS.registerItem("fire_staff",
            p -> new FireStaffItem(p.stacksTo(1).rarity(Rarity.RARE).fireResistant()));

    public static final DeferredItem<FireEyeItem> FIRE_EYE = ITEMS.registerItem("fire_eye",
            p -> new FireEyeItem(p.stacksTo(1).rarity(Rarity.EPIC).fireResistant()));

    // --- Набор ветра ---
    public static final TagKey<Block> INCORRECT_FOR_WIND_TOOL =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(MODID, "incorrect_for_wind_tool"));
    public static final TagKey<Item> REPAIRS_WIND_TOOL =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "repairs_wind_tool"));

    public static final ToolMaterial WIND_MATERIAL = new ToolMaterial(
            INCORRECT_FOR_WIND_TOOL, 250, 20.0f, 4.0f, 14, REPAIRS_WIND_TOOL);

    public static final ResourceKey<EquipmentAsset> WIND_EQUIPMENT_ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath(MODID, "wind"));

    public static final ArmorMaterial WIND_ARMOR_MATERIAL = new ArmorMaterial(
            15,
            Util.make(new EnumMap<>(ArmorType.class), map -> {
                map.put(ArmorType.BOOTS, 3);
                map.put(ArmorType.LEGGINGS, 6);
                map.put(ArmorType.CHESTPLATE, 8);
                map.put(ArmorType.HELMET, 3);
                map.put(ArmorType.BODY, 11);
            }),
            9,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            3.0f,
            0.1f,
            REPAIRS_WIND_TOOL,
            WIND_EQUIPMENT_ASSET
    );

    public static final DeferredItem<Item> WIND_SWORD = ITEMS.registerItem("wind_sword",
            p -> new WindSwordItem(p.sword(WIND_MATERIAL, 6.0F, -2.4F)));

    public static final DeferredItem<Item> WIND_PICKAXE = ITEMS.registerItem("wind_pickaxe",
            p -> new Item(p.pickaxe(WIND_MATERIAL, 1.0F, -2.8F)));

    public static final DeferredItem<AxeItem> WIND_AXE = ITEMS.registerItem("wind_axe",
            p -> new AxeItem(WIND_MATERIAL, 6.0F, -3.1F, p));

    public static final DeferredItem<ShovelItem> WIND_SHOVEL = ITEMS.registerItem("wind_shovel",
            p -> new ShovelItem(WIND_MATERIAL, 1.5F, -3.0F, p));

    public static final DeferredItem<HoeItem> WIND_HOE = ITEMS.registerItem("wind_hoe",
            p -> new HoeItem(WIND_MATERIAL, -2.0F, -1.0F, p));

    public static final DeferredItem<BowItem> WIND_BOW = ITEMS.registerItem("wind_bow",
            p -> new BowItem(p.durability(250).repairable(REPAIRS_WIND_TOOL)));

    public static final DeferredItem<Item> WIND_HELMET = ITEMS.registerItem("wind_helmet",
            p -> new Item(p.humanoidArmor(WIND_ARMOR_MATERIAL, ArmorType.HELMET)));
    public static final DeferredItem<Item> WIND_CHESTPLATE = ITEMS.registerItem("wind_chestplate",
            p -> new Item(p.humanoidArmor(WIND_ARMOR_MATERIAL, ArmorType.CHESTPLATE)));
    public static final DeferredItem<Item> WIND_LEGGINGS = ITEMS.registerItem("wind_leggings",
            p -> new Item(p.humanoidArmor(WIND_ARMOR_MATERIAL, ArmorType.LEGGINGS)));
    public static final DeferredItem<Item> WIND_BOOTS = ITEMS.registerItem("wind_boots",
            p -> new Item(p.humanoidArmor(WIND_ARMOR_MATERIAL, ArmorType.BOOTS)));

    public static final DeferredItem<Item> WIND_SHIELD = ITEMS.registerItem("wind_shield",
            p -> new Item(p
                    .durability(250)
                    .repairable(REPAIRS_WIND_TOOL)
                    .component(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(
                            0.25F, 1.0F,
                            List.of(new BlocksAttacks.DamageReduction(90.0F, Optional.empty(), 0.0F, 1.0F)),
                            new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
                            Optional.of(DamageTypeTags.BYPASSES_SHIELD),
                            Optional.of(SoundEvents.SHIELD_BLOCK),
                            Optional.of(SoundEvents.SHIELD_BREAK)))));

    public static final DeferredItem<WindStaffItem> WIND_STAFF = ITEMS.registerItem("wind_staff",
            p -> new WindStaffItem(p.stacksTo(1).rarity(Rarity.RARE)));

    public static final DeferredItem<WindEyeItem> WIND_EYE = ITEMS.registerItem("wind_eye",
            p -> new WindEyeItem(p.stacksTo(1).rarity(Rarity.EPIC)));

    // НАБОР МОЛНИИ
    // МЕЧ
    public static final DeferredItem<Item> LIGHTNING_SWORD = ITEMS.registerItem("lightning_sword", p -> new LightningSwordItem(
            p.sword(LightningMaterials.LIGHTNING_TOOL,
                    // высокий урон
                    10.0F,
                    // очень быстрая атака
                    -1.2F)));
    // КИРКА
    public static final DeferredItem<Item> LIGHTNING_PICKAXE = ITEMS.registerItem("lightning_pickaxe", p -> new Item(
            p.pickaxe(LightningMaterials.LIGHTNING_TOOL, 1.0F, -2.0F)));
    // ТОПОР
    public static final DeferredItem<AxeItem> LIGHTNING_AXE = ITEMS.registerItem("lightning_axe", p -> new AxeItem(
            LightningMaterials.LIGHTNING_TOOL, 6.0F, -2.0F, p));
    // ЛОПАТА
    public static final DeferredItem<ShovelItem> LIGHTNING_SHOVEL =
            ITEMS.registerItem(
                    "lightning_shovel",
                    p -> new ShovelItem(
                            LightningMaterials.LIGHTNING_TOOL,
                            1.5F,
                            -2.0F,
                            p
                    )
            );
    // МОТЫГА
    public static final DeferredItem<HoeItem> LIGHTNING_HOE =
            ITEMS.registerItem(
                    "lightning_hoe",
                    p -> new HoeItem(
                            LightningMaterials.LIGHTNING_TOOL,
                            -4.0F,
                            0.0F,
                            p
                    )
            );
    // ЛУК
    public static final DeferredItem<BowItem> LIGHTNING_BOW =
            ITEMS.registerItem(
                    "lightning_bow",
                    p -> new BowItem(
                            p.durability(2031)
                                    .repairable(
                                            LightningMaterials.REPAIRS_LIGHTNING
                                    )
                    )
            );

    // БРОНЯ
    public static final DeferredItem<Item> LIGHTNING_HELMET =
            ITEMS.registerItem(
                    "lightning_helmet",
                    p -> new Item(
                            p.humanoidArmor(
                                    LightningMaterials.LIGHTNING_ARMOR,
                                    ArmorType.HELMET
                            )
                    )
            );
    public static final DeferredItem<Item> LIGHTNING_CHESTPLATE =
            ITEMS.registerItem(
                    "lightning_chestplate",
                    p -> new Item(
                            p.humanoidArmor(
                                    LightningMaterials.LIGHTNING_ARMOR,
                                    ArmorType.CHESTPLATE
                            )
                    )
            );
    public static final DeferredItem<Item> LIGHTNING_LEGGINGS = ITEMS.registerItem("lightning_leggings", p -> new Item(
            p.humanoidArmor(LightningMaterials.LIGHTNING_ARMOR, ArmorType.LEGGINGS)));
    public static final DeferredItem<Item> LIGHTNING_BOOTS = ITEMS.registerItem("lightning_boots", p -> new Item(
            p.humanoidArmor(LightningMaterials.LIGHTNING_ARMOR, ArmorType.BOOTS)));
    // ЩИТ
    public static final DeferredItem<Item> LIGHTNING_SHIELD = ITEMS.registerItem("lightning_shield", p -> new Item(
            p.durability(2031).repairable(LightningMaterials.REPAIRS_LIGHTNING)
                    .component(DataComponents.BLOCKS_ATTACKS, new BlocksAttacks(0.1F, 1.0F, List.of(
                            new BlocksAttacks.DamageReduction(110.0F, Optional.empty(), 0.0F, 1.0F)),
                            new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 0.5F),
                            Optional.of(DamageTypeTags.BYPASSES_SHIELD),
                            Optional.of(SoundEvents.SHIELD_BLOCK),
                            Optional.of(SoundEvents.SHIELD_BREAK)))));
    public static final DeferredItem<Item> LIGHTNING_STAFF = ITEMS.registerItem("lightning_staff", p -> new LightningStaffItem(
            p.stacksTo(1).durability(2031).repairable(LightningMaterials.REPAIRS_LIGHTNING)));
    public static final DeferredItem<Item> LIGHTNING_EYE = ITEMS.registerItem("lightning_eye", p -> new LightningEyeItem(
            p.stacksTo(1).durability(2031).repairable(LightningMaterials.REPAIRS_LIGHTNING)));

// ============================================================
// МЕЧ ВЕЧНОГО МОРОЗА
// ============================================================

    public static final TagKey<Block> INCORRECT_FOR_ETERNAL_FROST =
            TagKey.create(
                    Registries.BLOCK,
                    ResourceLocation.fromNamespaceAndPath(
                            MODID,
                            "incorrect_for_eternal_frost"
                    )
            );

    public static final TagKey<Item> REPAIRS_ETERNAL_FROST =
            TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(
                            MODID,
                            "repairs_eternal_frost"
                    )
            );


// ============================================================
// МАТЕРИАЛ МЕЧА ВЕЧНОГО МОРОЗА
// ============================================================

    public static final ToolMaterial ETERNAL_FROST_MATERIAL =
            new ToolMaterial(
                    INCORRECT_FOR_ETERNAL_FROST,

                    // Прочность материала.
                    // Сам меч ниже будет UNBREAKABLE.
                    9999,

                    // Скорость инструмента
                    15.0F,

                    // Бонус урона материала
                    0.0F,

                    // Зачаровываемость
                    30,

                    REPAIRS_ETERNAL_FROST
            );


// ============================================================
// ТЕХНИЧЕСКИЙ ITEM ДЛЯ ЛЕДЯНОГО СНАРЯДА
// ============================================================

    public static final DeferredItem<Item> ICE_BLADE_PROJECTILE_ITEM =
            ITEMS.registerItem(
                    "ice_blade_projectile_item",
                    properties -> new Item(
                            properties.stacksTo(1)
                    )
            );
// ------------------------------------------------------------
// МЕЧ ВЕЧНОГО МОРОЗА
// ------------------------------------------------------------

    public static final DeferredItem<Item> ETERNAL_FROST_SWORD =
            ITEMS.registerItem(
                    "eternal_frost_sword",

                    properties ->
                            new EternalFrostSwordItem(
                                    properties
                                            .sword(
                                                    ETERNAL_FROST_MATERIAL,

                                                    // Урон меча
                                                    20.0F,

                                                    // Скорость атаки
                                                    -2.4F
                                            )

                                            .stacksTo(1)

                                            // Меч невозможно сломать
                                            .component(
                                                    DataComponents.UNBREAKABLE,
                                                    Unit.INSTANCE
                                            )

                                            .rarity(Rarity.EPIC)
                            )
            );


// ------------------------------------------------------------
// ПОСОХ ЧЁРНОЙ ДЫРЫ
// ------------------------------------------------------------

    public static final DeferredItem<Item> BLACK_HOLE_STAFF =
            ITEMS.registerItem(
                    "black_hole_staff",
                    properties ->
                            new BlackHoleStaffItem(
                                    properties
                                            .stacksTo(1)
                                            .component(
                                                    DataComponents.UNBREAKABLE,
                                                    Unit.INSTANCE
                                            )
                                            .rarity(Rarity.EPIC)
                            )
            );


// ============================================================
// НАГРУДНИК СПЕКТАТОРА
// ============================================================

    public static final DeferredItem<Item> SPECTATOR_CHESTPLATE =
            ITEMS.registerItem(
                    "spectator_chestplate",

                    properties ->
                            new SpectatorChestplateItem(

                                    properties

                                            // Делаем предмет настоящим нагрудником
                                            .humanoidArmor(
                                                    SpectatorMaterials.SPECTATOR_ARMOR,
                                                    ArmorType.CHESTPLATE
                                            )

                                            .stacksTo(1)

                                            // Нагрудник невозможно сломать
                                            .component(
                                                    DataComponents.UNBREAKABLE,
                                                    Unit.INSTANCE
                                            )

                                            .rarity(Rarity.EPIC)
                            )
            );


// ------------------------------------------------------------
// ОРУЖИЕ БЕСКОНЕЧНОЙ ЭНЕРГИИ
// ------------------------------------------------------------

    public static final DeferredItem<Item> INFINITE_ENERGY_GUN =
            ITEMS.registerItem(
                    "infinite_energy_gun",
                    properties ->
                            new EnergyBlasterItem(properties)
            );


// ============================================================
// ENTITY ЛЕДЯНОГО ЛЕЗВИЯ
// ============================================================

    public static final DeferredHolder<
            EntityType<?>,
            EntityType<IceBladeProjectile>
            > ICE_BLADE_PROJECTILE =

            ENTITY_TYPES.register(
                    "ice_blade_projectile",

                    () -> EntityType.Builder
                            .<IceBladeProjectile>of(
                                    IceBladeProjectile::new,
                                    MobCategory.MISC
                            )

                            .sized(
                                    0.6F,
                                    0.6F
                            )

                            .clientTrackingRange(8)

                            .updateInterval(1)

                            .build(
                                    ResourceKey.create(
                                            Registries.ENTITY_TYPE,

                                            ResourceLocation.fromNamespaceAndPath(
                                                    MODID,
                                                    "ice_blade_projectile"
                                            )
                                    )
                            )
            );

    public static final DeferredHolder<
            EntityType<?>,
            EntityType<BlackHoleEntity>
            > BLACK_HOLE_ENTITY =

            ENTITY_TYPES.register(
                    "black_hole",

                    () -> EntityType.Builder
                            .<BlackHoleEntity>of(
                                    BlackHoleEntity::new,
                                    MobCategory.MISC
                            )

                            .sized(
                                    2.0F,
                                    2.0F
                            )

                            .clientTrackingRange(12)

                            .updateInterval(1)

                            .build(
                                    ResourceKey.create(
                                            Registries.ENTITY_TYPE,
                                            ResourceLocation.fromNamespaceAndPath(
                                                    MODID,
                                                    "black_hole"
                                            )
                                    )
                            )
            );

    public static final DeferredHolder<EntityType<?>, EntityType<EnergyBlastProjectile>>
            ENERGY_BLAST =
            ENTITY_TYPES.register(
                    "energy_blast",
                    () ->
                            EntityType.Builder
                                    .<EnergyBlastProjectile>of(
                                            EnergyBlastProjectile::new,
                                            MobCategory.MISC
                                    )
                                    .sized(
                                            0.25F,
                                            0.25F
                                    )
                                    .clientTrackingRange(64)
                                    .updateInterval(1)
                                    .build(
                                            ResourceKey.create(
                                                    Registries.ENTITY_TYPE,
                                                    ResourceLocation.fromNamespaceAndPath(
                                                            MODID,
                                                            "energy_blast"
                                                    )
                                            )
                                    )
            );

    public static final DeferredBlock<EternalFrostSwordPedestalBlock> ETERNAL_FROST_SWORD_PEDESTAL =
            BLOCKS.registerBlock(
                    "eternal_frost_sword_pedestal",
                    EternalFrostSwordPedestalBlock::new,
                    BlockBehaviour.Properties.of()
                            .strength(-1.0F, 3600000.0F)
                            .noLootTable()
            );
    public static final DeferredHolder<
            BlockEntityType<?>,
            BlockEntityType<EternalFrostSwordPedestalBlockEntity>
            > ETERNAL_FROST_SWORD_PEDESTAL_BE =

            BLOCK_ENTITY_TYPES.register(
                    "eternal_frost_sword_pedestal",

                    () -> new BlockEntityType<>(
                            EternalFrostSwordPedestalBlockEntity::new,
                            Set.of(
                                    ETERNAL_FROST_SWORD_PEDESTAL.get()
                            )
                    )
            );
    public static final DeferredItem<BlockItem> ETERNAL_FROST_SWORD_PEDESTAL_ITEM =
            ITEMS.registerItem(
                    "eternal_frost_sword_pedestal",
                    properties -> new BlockItem(
                            ETERNAL_FROST_SWORD_PEDESTAL.get(),
                            properties
                    )
            );

    public static final DeferredItem<BlockItem> QUETZALCOATLUS_EGG_ITEM =
            ITEMS.registerSimpleBlockItem("quetzalcoatlus_egg", QUETZALCOATLUS_EGG);

    public static final DeferredItem<BlockItem> TRICERATOPS_EGG_ITEM =
            ITEMS.registerSimpleBlockItem("triceratops_egg", TRICERATOPS_EGG);

    public static final DeferredItem<BlockItem> STEGOSAURUS_EGG_ITEM =
            ITEMS.registerSimpleBlockItem("stegosaurus_egg", STEGOSAURUS_EGG);

    public static final DeferredItem<BlockItem> PARASAUROLOPHUS_EGG_ITEM =
            ITEMS.registerSimpleBlockItem("parasaurolophus_egg", PARASAUROLOPHUS_EGG);

    public static final DeferredItem<BlockItem> ANKYLOSAURUS_EGG_ITEM =
            ITEMS.registerSimpleBlockItem("ankylosaurus_egg", ANKYLOSAURUS_EGG);

    public static final DeferredItem<BlockItem> BRACHIOSAURUS_EGG_ITEM =
            ITEMS.registerSimpleBlockItem("brachiosaurus_egg", BRACHIOSAURUS_EGG);
    public static final DeferredItem<BlockItem> TYRANNOSAURUS_EGG_ITEM =
            ITEMS.registerSimpleBlockItem(
                    "tyrannosaurus_egg",
                    TYRANNOSAURUS_EGG
            );
    // Предмет яйца спинозавра
    public static final DeferredItem<BlockItem> SPINOSAURUS_EGG_ITEM =
            ITEMS.registerSimpleBlockItem(
                    "spinosaurus_egg",
                    SPINOSAURUS_EGG
            );
    public static final DeferredItem<BlockItem> THERIZINOSAURUS_EGG_ITEM =
            ITEMS.registerSimpleBlockItem("therizinosaurus_egg", THERIZINOSAURUS_EGG);


    public static final DeferredItem<BlockItem> MOSASAURUS_EGG_ITEM =
            ITEMS.registerSimpleBlockItem(
                    "mosasaurus_egg",
                    MOSASAURUS_EGG
            );

    public static final DeferredItem<BlockItem> GIGANOTOSAURUS_EGG_ITEM =
            ITEMS.registerSimpleBlockItem(
                    "giganotosaurus_egg",
                    GIGANOTOSAURUS_EGG
            );
    public static final DeferredItem<BlockItem> ELASMOSAURUS_EGG_ITEM =
            ITEMS.registerSimpleBlockItem(
                    "elasmosaurus_egg",
                    ELASMOSAURUS_EGG
            );
    public static final DeferredItem<BlockItem> ARCHELON_EGG_ITEM =
            ITEMS.registerSimpleBlockItem(
                    "archelon_egg",
                    ARCHELON_EGG
            );
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DinosaurEggBlockEntity>>
            DINOSAUR_EGG_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "dinosaur_egg",
            () -> new BlockEntityType<>(
                    DinosaurEggBlockEntity::new,
                    TRICERATOPS_EGG.get(),
                    STEGOSAURUS_EGG.get(),
                    PARASAUROLOPHUS_EGG.get(),
                    ANKYLOSAURUS_EGG.get(),
                    BRACHIOSAURUS_EGG.get(),
                    TYRANNOSAURUS_EGG.get(),
                    SPINOSAURUS_EGG.get(),
                    GIGANOTOSAURUS_EGG.get(),
                    THERIZINOSAURUS_EGG.get(),
                    MOSASAURUS_EGG.get(),
                    ELASMOSAURUS_EGG.get(),
                    ARCHELON_EGG.get(),
                    QUETZALCOATLUS_EGG.get()
            )
    );
    // Креативная вкладка мода
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.abyss"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(EXAMPLE_ITEM.get());
                output.accept(ABYSS_ARTIFACT.get());
                output.accept(ABYSS_GRASS.get());
                output.accept(ABYSS_GRASS_WET.get());
                output.accept(ABYSS_SOIL.get());
                output.accept(ABYSS_CHARRED_GROUND.get());
                output.accept(ABYSS_LIGHT_STONE.get());
                output.accept(ABYSS_LOG.get());
                output.accept(ABYSS_CHARRED_LOG.get());
                output.accept(ABYSS_WHITE_LOG.get());
                output.accept(RED_LEAVES.get());
                output.accept(YELLOW_LEAVES.get());
                output.accept(GREEN_LEAVES.get());
                output.accept(PURPLE_LEAVES.get());
                output.accept(ABYSS_SAPLING.get());
                output.accept(CHARRED_SAPLING.get());
                output.accept(WHITE_SAPLING.get());
                output.accept(PALE_ANTIDOTE_FRUIT.get());

                // --- Руды ---
                output.accept(EARTH_ORE.get());
                output.accept(FIRE_ORE.get());
                output.accept(WATER_ORE.get());
                output.accept(WIND_ORE.get());
                output.accept(LIGHTNING_ORE.get());

                // --- Сырьё ---
                output.accept(EARTH_RAW.get());
                output.accept(FIRE_RAW.get());
                output.accept(WATER_RAW.get());
                output.accept(WIND_RAW.get());
                output.accept(LIGHTNING_RAW.get());

                // --- Слитки ---
                output.accept(EARTH_INGOT.get());
                output.accept(FIRE_INGOT.get());
                output.accept(WATER_INGOT.get());
                output.accept(WIND_INGOT.get());
                output.accept(LIGHTNING_INGOT.get());

                output.accept(BAOBAB_LOG.get());
                output.accept(BAOBAB_FRUIT.get());
                output.accept(EMPTY_ORE_ITEM.get().getDefaultInstance());

                output.accept(ABYSS_FORGE.get());
                output.accept(RUBY_ORE.get());
                output.accept(SAPPHIRE_ORE.get());
                output.accept(AMBER_ORE.get());
                output.accept(RUBY.get());
                output.accept(SAPPHIRE.get());
                output.accept(AMBER.get());
                output.accept(EARTH_BROKEN_INGOT.get());
                output.accept(FIRE_BROKEN_INGOT.get());
                output.accept(WATER_BROKEN_INGOT.get());
                output.accept(WIND_BROKEN_INGOT.get());
                output.accept(LIGHTNING_BROKEN_INGOT.get());

                output.accept(EARTH_SWORD.get());
                output.accept(EARTH_PICKAXE.get());
                output.accept(EARTH_AXE.get());
                output.accept(EARTH_SHOVEL.get());
                output.accept(EARTH_HOE.get());
                output.accept(EARTH_BOW.get());
                output.accept(EARTH_SHIELD.get());
                output.accept(EARTH_HELMET.get());
                output.accept(EARTH_CHESTPLATE.get());
                output.accept(EARTH_LEGGINGS.get());
                output.accept(EARTH_BOOTS.get());
                output.accept(EARTH_STAFF.get());
                output.accept(EARTH_EYE.get());

                output.accept(WATER_SWORD.get());
                output.accept(WATER_PICKAXE.get());
                output.accept(WATER_AXE.get());
                output.accept(WATER_SHOVEL.get());
                output.accept(WATER_HOE.get());
                output.accept(WATER_BOW.get());
                output.accept(WATER_SHIELD.get());
                output.accept(WATER_HELMET.get());
                output.accept(WATER_CHESTPLATE.get());
                output.accept(WATER_LEGGINGS.get());
                output.accept(WATER_BOOTS.get());
                output.accept(WATER_STAFF.get());
                output.accept(WATER_EYE.get());

                output.accept(FIRE_SWORD.get());
                output.accept(FIRE_PICKAXE.get());
                output.accept(FIRE_AXE.get());
                output.accept(FIRE_SHOVEL.get());
                output.accept(FIRE_HOE.get());
                output.accept(FIRE_BOW.get());
                output.accept(FIRE_SHIELD.get());
                output.accept(FIRE_HELMET.get());
                output.accept(FIRE_CHESTPLATE.get());
                output.accept(FIRE_LEGGINGS.get());
                output.accept(FIRE_BOOTS.get());
                output.accept(FIRE_STAFF.get());
                output.accept(FIRE_EYE.get());

                output.accept(WIND_SWORD.get());
                output.accept(WIND_AXE.get());
                output.accept(WIND_PICKAXE.get());
                output.accept(WIND_SHOVEL.get());
                output.accept(WIND_HOE.get());
                output.accept(WIND_BOW.get());
                output.accept(WIND_HELMET.get());
                output.accept(WIND_CHESTPLATE.get());
                output.accept(WIND_LEGGINGS.get());
                output.accept(WIND_BOOTS.get());
                output.accept(WIND_SHIELD.get());
                output.accept(WIND_STAFF.get());
                output.accept(WIND_EYE.get());

                // --- Молния ---
                output.accept(LIGHTNING_SWORD.get());
                output.accept(LIGHTNING_PICKAXE.get());
                output.accept(LIGHTNING_AXE.get());
                output.accept(LIGHTNING_SHOVEL.get());
                output.accept(LIGHTNING_HOE.get());
                output.accept(LIGHTNING_BOW.get());

                output.accept(LIGHTNING_HELMET.get());
                output.accept(LIGHTNING_CHESTPLATE.get());
                output.accept(LIGHTNING_LEGGINGS.get());
                output.accept(LIGHTNING_BOOTS.get());
                output.accept(LIGHTNING_STAFF.get());
                output.accept(LIGHTNING_SHIELD.get());
                output.accept(LIGHTNING_EYE.get());

                output.accept(ETERNAL_FROST_SWORD.get());
                output.accept(BLACK_HOLE_STAFF.get());
                output.accept(SPECTATOR_CHESTPLATE.get());
                output.accept(INFINITE_ENERGY_GUN.get());
                output.accept(ETERNAL_FROST_SWORD_PEDESTAL_ITEM.get());

                // --- Яйца призыва динозавров ---
                output.accept(ModEntities.TRICERATOPS_SPAWN_EGG.get());
                output.accept(ModEntities.STEGOSAURUS_SPAWN_EGG.get());
                output.accept(ModEntities.PARASAUROLOPHUS_SPAWN_EGG.get());
                output.accept(ModEntities.ANKYLOSAURUS_SPAWN_EGG.get());
                output.accept(ModEntities.BRACHIOSAURUS_SPAWN_EGG.get());
                output.accept(ModEntities.TYRANNOSAURUS_SPAWN_EGG.get());
                output.accept(ModEntities.SPINOSAURUS_SPAWN_EGG.get());
                output.accept(ModEntities.GIGANOTOSAURUS_SPAWN_EGG.get());
                output.accept(ModEntities.THERIZINOSAURUS_SPAWN_EGG.get());
                output.accept(ModEntities.MOSASAURUS_SPAWN_EGG.get());
                output.accept(ModEntities.XIPHACTINUS_SPAWN_EGG.get());
                output.accept(ModEntities.ELASMOSAURUS_SPAWN_EGG.get());
                output.accept(ModEntities.ARCHELON_SPAWN_EGG.get());
                output.accept(ModEntities.TRILOBITE_SPAWN_EGG.get());
                output.accept(ModEntities.ORTHOCERAS_SPAWN_EGG.get());
                output.accept(ModEntities.QUETZALCOATLUS_SPAWN_EGG.get());

                // Яйца динозавров для кладки
                output.accept(TRICERATOPS_EGG_ITEM.get());
                output.accept(QUETZALCOATLUS_EGG_ITEM.get());
                output.accept(STEGOSAURUS_EGG_ITEM.get());
                output.accept(PARASAUROLOPHUS_EGG_ITEM.get());
                output.accept(ANKYLOSAURUS_EGG_ITEM.get());
                output.accept(BRACHIOSAURUS_EGG_ITEM.get());
                output.accept(TYRANNOSAURUS_EGG_ITEM.get());
                output.accept(GIGANOTOSAURUS_EGG_ITEM.get());
                output.accept(THERIZINOSAURUS_EGG_ITEM.get());
                output.accept(SPINOSAURUS_EGG_ITEM.get());
                output.accept(MOSASAURUS_EGG_ITEM.get());
                output.accept(ELASMOSAURUS_EGG_ITEM.get());
                output.accept(ARCHELON_EGG_ITEM .get());

                ModPlants.ITEM_LIST.forEach(item -> output.accept(item.get()));
            }).build());

    public Abyss(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        ModPlants.init();
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModFluids.FLUID_BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        DATA_COMPONENTS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModEntities.ITEMS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(SpectatorChestplateEvents.class);
        NeoForge.EVENT_BUS.register(SpectatorClientEvents.class);
        modEventBus.addListener(this::addCreative);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
        AbyssForgeBlockEntity.bootstrap();
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(EXAMPLE_BLOCK_ITEM);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");
        }
        @SubscribeEvent
        public static void onRegisterScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
            event.register(Abyss.ABYSS_FORGE_MENU.get(), com.akashi.abyss.abyss.block.AbyssForgeScreen::new);
        }
        @SubscribeEvent
        public static void onRegisterRenderers(
                EntityRenderersEvent.RegisterRenderers event
        ) {

            event.registerEntityRenderer(
                    Abyss.EARTH_STONE.get(),
                    ThrownItemRenderer::new
            );

            event.registerEntityRenderer(
                    Abyss.WATER_BLADE.get(),
                    ThrownItemRenderer::new
            );

            event.registerEntityRenderer(
                    Abyss.ICE_BLADE_PROJECTILE.get(),
                    IceBladeRenderer::new
            );

            event.registerEntityRenderer(
                    Abyss.BLACK_HOLE_ENTITY.get(),
                    BlackHoleRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.GIGANOTOSAURUS.get(),
                    com.akashi.abyss.abyss.entity.GiganotosaurusRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.THERIZINOSAURUS.get(),
                    com.akashi.abyss.abyss.entity.TherizinosaurusRenderer::new
            );
            // Мозазавр
            event.registerEntityRenderer(
                    ModEntities.MOSASAURUS.get(),
                    com.akashi.abyss.abyss.entity.MosasaurusRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.XIPHACTINUS.get(),
                    XiphactinusRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.ELASMOSAURUS.get(),
                    ElasmosaurusRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.ARCHELON.get(),
                    ArchelonRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.TRILOBITE.get(),
                    TrilobiteRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.ORTHOCERAS.get(),
                    OrthocerasRenderer::new
            );
            event.registerEntityRenderer(
                    ModEntities.QUETZALCOATLUS.get(),
                    QuetzalcoatlusRenderer::new
            );
        }
        @SubscribeEvent
        public static void onRegisterBlockEntityRenderers(
                net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event
        ) {
            event.registerBlockEntityRenderer(
                    Abyss.ETERNAL_FROST_SWORD_PEDESTAL_BE.get(),
                    EternalFrostSwordPedestalRenderer::new
            );
        }
    }
}