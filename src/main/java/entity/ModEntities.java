package com.akashi.abyss.abyss.entity;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.akashi.abyss.abyss.entity.water.XiphactinusEntity;
import com.akashi.abyss.abyss.entity.water.ElasmosaurusEntity;
import com.akashi.abyss.abyss.entity.water.ArchelonEntity;
import com.akashi.abyss.abyss.entity.water.TrilobiteEntity;
import com.akashi.abyss.abyss.entity.water.OrthocerasEntity;
import com.akashi.abyss.abyss.entity.heaven.QuetzalcoatlusEntity;

public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Abyss.MODID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(Abyss.MODID);

    // Трицератопс
    public static final DeferredHolder<EntityType<?>, EntityType<TriceratopsEntity>> TRICERATOPS =
            ENTITY_TYPES.register("triceratops", id ->
                    EntityType.Builder.of(TriceratopsEntity::new, MobCategory.CREATURE)
                            .sized(3.0F, 3.5F)
                            .clientTrackingRange(10)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredItem<Item> TRICERATOPS_SPAWN_EGG =
            ITEMS.registerItem("triceratops_spawn_egg",
                    props -> new SpawnEggItem(props.spawnEgg(TRICERATOPS.get())));

    // Стегозавр
    public static final DeferredHolder<EntityType<?>, EntityType<StegosaurusEntity>> STEGOSAURUS =
            ENTITY_TYPES.register("stegosaurus", id ->
                    EntityType.Builder.of(StegosaurusEntity::new, MobCategory.CREATURE)
                            .sized(2.6F, 3.5F)
                            .clientTrackingRange(10)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredItem<Item> STEGOSAURUS_SPAWN_EGG =
            ITEMS.registerItem("stegosaurus_spawn_egg",
                    props -> new SpawnEggItem(props.spawnEgg(STEGOSAURUS.get())));

    // Паразауролоф — обитатель Белого ядовитого леса
    public static final DeferredHolder<EntityType<?>, EntityType<ParasaurolophusEntity>> PARASAUROLOPHUS =
            ENTITY_TYPES.register("parasaurolophus", id ->
                    EntityType.Builder.of(ParasaurolophusEntity::new, MobCategory.CREATURE)
                            .sized(2.4F, 3.2F)
                            .clientTrackingRange(10)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredItem<Item> PARASAUROLOPHUS_SPAWN_EGG =
            ITEMS.registerItem("parasaurolophus_spawn_egg",
                    props -> new SpawnEggItem(props.spawnEgg(PARASAUROLOPHUS.get())));


    public static final DeferredHolder<EntityType<?>, EntityType<AnkylosaurusEntity>> ANKYLOSAURUS =
            ENTITY_TYPES.register("ankylosaurus", id ->
                    EntityType.Builder.of(
                                    AnkylosaurusEntity::new,
                                    MobCategory.CREATURE
                            )
                            .sized(2.8F, 2.0F)
                            .clientTrackingRange(10)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredItem<Item> ANKYLOSAURUS_SPAWN_EGG =
            ITEMS.registerItem(
                    "ankylosaurus_spawn_egg",
                    props -> new SpawnEggItem(
                            props.spawnEgg(ANKYLOSAURUS.get())
                    )
            );

    // Брахиозавр — гигантский мирный травоядный динозавр
    public static final DeferredHolder<EntityType<?>, EntityType<BrachiosaurusEntity>> BRACHIOSAURUS =
            ENTITY_TYPES.register("brachiosaurus", id ->
                    EntityType.Builder.of(
                                    BrachiosaurusEntity::new,
                                    MobCategory.CREATURE
                            )
                            .sized(10.5F, 24.0F)
                            .clientTrackingRange(12)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredItem<Item> BRACHIOSAURUS_SPAWN_EGG =
            ITEMS.registerItem(
                    "brachiosaurus_spawn_egg",
                    props -> new SpawnEggItem(
                            props.spawnEgg(BRACHIOSAURUS.get())
                    )
            );

    // Тираннозавр рекс — главный наземный хищник
    public static final DeferredHolder<EntityType<?>, EntityType<TyrannosaurusEntity>> TYRANNOSAURUS =
            ENTITY_TYPES.register("tyrannosaurus", id ->
                    EntityType.Builder.of(
                                    TyrannosaurusEntity::new,
                                    MobCategory.CREATURE
                            )
                            .sized(3.5F, 5.0F)
                            .clientTrackingRange(12)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredItem<Item> TYRANNOSAURUS_SPAWN_EGG =
            ITEMS.registerItem(
                    "tyrannosaurus_spawn_egg",
                    props -> new SpawnEggItem(
                            props.spawnEgg(TYRANNOSAURUS.get())
                    )
            );
    // Спинозавр — крупный полуводный хищник
    public static final DeferredHolder<EntityType<?>, EntityType<SpinosaurusEntity>> SPINOSAURUS =
            ENTITY_TYPES.register("spinosaurus", id ->
                    EntityType.Builder.of(
                                    SpinosaurusEntity::new,
                                    MobCategory.CREATURE
                            )
                            .sized(3.5F, 5.0F)
                            .clientTrackingRange(12)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredItem<Item> SPINOSAURUS_SPAWN_EGG =
            ITEMS.registerItem(
                    "spinosaurus_spawn_egg",
                    props -> new SpawnEggItem(
                            props.spawnEgg(SPINOSAURUS.get())
                    )
            );
    // Гиганотозавр — огромный наземный хищник
    public static final DeferredHolder<EntityType<?>, EntityType<GiganotosaurusEntity>> GIGANOTOSAURUS =
            ENTITY_TYPES.register("giganotosaurus", id ->
                    EntityType.Builder.of(
                                    GiganotosaurusEntity::new,
                                    MobCategory.CREATURE
                            )
                            .sized(3.8F, 5.2F)
                            .clientTrackingRange(12)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, id))
            );

    // Яйцо призыва гиганотозавра
    public static final DeferredItem<Item> GIGANOTOSAURUS_SPAWN_EGG =
            ITEMS.registerItem(
                    "giganotosaurus_spawn_egg",
                    props -> new SpawnEggItem(
                            props.spawnEgg(GIGANOTOSAURUS.get())
                    )
            );

    public static final DeferredHolder<EntityType<?>, EntityType<TherizinosaurusEntity>> THERIZINOSAURUS =
            ENTITY_TYPES.register("therizinosaurus", id ->
                    EntityType.Builder.of(TherizinosaurusEntity::new, MobCategory.CREATURE)
                            .sized(3.0F, 4.3F)
                            .clientTrackingRange(12)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredItem<Item> THERIZINOSAURUS_SPAWN_EGG =
            ITEMS.registerItem("therizinosaurus_spawn_egg",
                    props -> new SpawnEggItem(props.spawnEgg(THERIZINOSAURUS.get())));


    // Мозазавр — огромный подводный хищник
    public static final DeferredHolder<EntityType<?>, EntityType<MosasaurusEntity>> MOSASAURUS =
            ENTITY_TYPES.register("mosasaurus", id ->
                    EntityType.Builder.of(
                                    MosasaurusEntity::new,
                                    MobCategory.WATER_CREATURE
                            )
                            .sized(3.8F, 3.0F)
                            .clientTrackingRange(16)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, id))
            );

    // Яйцо призыва Мозазавра
    public static final DeferredItem<Item> MOSASAURUS_SPAWN_EGG =
            ITEMS.registerItem(
                    "mosasaurus_spawn_egg",
                    props -> new SpawnEggItem(
                            props.spawnEgg(MOSASAURUS.get())
                    )
            );

    // Ксифактин — крупная хищная рыба первого биома Бездны
    public static final DeferredHolder<EntityType<?>, EntityType<XiphactinusEntity>> XIPHACTINUS =
            ENTITY_TYPES.register("xiphactinus", id ->
                    EntityType.Builder.of(
                                    XiphactinusEntity::new,
                                    MobCategory.WATER_CREATURE
                            )
                            .sized(2.8F, 1.6F)
                            .clientTrackingRange(12)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, id))
            );

    // Яйцо призыва Ксифактина
    public static final DeferredItem<Item> XIPHACTINUS_SPAWN_EGG =
            ITEMS.registerItem(
                    "xiphactinus_spawn_egg",
                    props -> new SpawnEggItem(
                            props.spawnEgg(XIPHACTINUS.get())
                    )
            );

    // Эласмозавр — мирная морская рептилия первого биома Бездны
    public static final DeferredHolder<EntityType<?>, EntityType<ElasmosaurusEntity>> ELASMOSAURUS =
            ENTITY_TYPES.register("elasmosaurus", id ->
                    EntityType.Builder.of(
                                    ElasmosaurusEntity::new,
                                    MobCategory.WATER_CREATURE
                            )
                            .sized(2.8F, 1.5F)
                            .clientTrackingRange(12)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, id))
            );

    // Яйцо призыва Эласмозавра
    public static final DeferredItem<Item> ELASMOSAURUS_SPAWN_EGG =
            ITEMS.registerItem(
                    "elasmosaurus_spawn_egg",
                    props -> new SpawnEggItem(
                            props.spawnEgg(ELASMOSAURUS.get())
                    )
            );
    // Архелон — гигантская древняя морская черепаха
    public static final DeferredHolder<EntityType<?>, EntityType<ArchelonEntity>> ARCHELON =
            ENTITY_TYPES.register("archelon", id ->
                    EntityType.Builder.of(
                                    ArchelonEntity::new,
                                    MobCategory.WATER_CREATURE
                            )
                            .sized(3.5F, 1.8F)
                            .clientTrackingRange(12)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, id))
            );

    public static final DeferredItem<Item> ARCHELON_SPAWN_EGG =
            ITEMS.registerItem(
                    "archelon_spawn_egg",
                    properties -> new SpawnEggItem(
                            properties.spawnEgg(ARCHELON.get())
                    )
            );
    // Трилобит — мирный обитатель морского дна первого биома
    public static final DeferredHolder<EntityType<?>, EntityType<TrilobiteEntity>> TRILOBITE =
            ENTITY_TYPES.register("trilobite", id ->
                    EntityType.Builder.of(TrilobiteEntity::new, MobCategory.WATER_CREATURE)
                            .sized(0.15F, 0.50F)
                            .clientTrackingRange(8)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredItem<Item> TRILOBITE_SPAWN_EGG =
            ITEMS.registerItem("trilobite_spawn_egg",
                    props -> new SpawnEggItem(props.spawnEgg(TRILOBITE.get())));

    // Ортокерас — мирный древний головоногий моллюск
    public static final DeferredHolder<EntityType<?>, EntityType<OrthocerasEntity>> ORTHOCERAS =
            ENTITY_TYPES.register("orthoceras", id ->
                    EntityType.Builder.of(OrthocerasEntity::new, MobCategory.WATER_CREATURE)
                            .sized(0.9F, 1.3F)
                            .clientTrackingRange(10)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredItem<Item> ORTHOCERAS_SPAWN_EGG =
            ITEMS.registerItem(
                    "orthoceras_spawn_egg",
                    props -> new SpawnEggItem(
                            props.spawnEgg(ORTHOCERAS.get())
                    )
            );
    public static final DeferredHolder<EntityType<?>, EntityType<QuetzalcoatlusEntity>>
            QUETZALCOATLUS =
            ENTITY_TYPES.register("quetzalcoatlus", id ->
                    EntityType.Builder.of(
                                    QuetzalcoatlusEntity::new,
                                    MobCategory.CREATURE
                            )
                            .sized(3.0F, 6.0F)
                            .clientTrackingRange(16)
                            .build(ResourceKey.create(Registries.ENTITY_TYPE, id))
            );

    public static final DeferredItem<Item> QUETZALCOATLUS_SPAWN_EGG =
            ITEMS.registerItem(
                    "quetzalcoatlus_spawn_egg",
                    props -> new SpawnEggItem(
                            props.spawnEgg(QUETZALCOATLUS.get())
                    )
            );
    private ModEntities() {}
}
