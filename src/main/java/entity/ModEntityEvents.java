package com.akashi.abyss.abyss.entity;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import com.akashi.abyss.abyss.entity.BrachiosaurusEntity;
import com.akashi.abyss.abyss.entity.MosasaurusEntity;
import com.akashi.abyss.abyss.entity.water.XiphactinusEntity;
import com.akashi.abyss.abyss.entity.water.ElasmosaurusEntity;
import com.akashi.abyss.abyss.entity.water.ArchelonEntity;
import net.minecraft.tags.FluidTags;
import com.akashi.abyss.abyss.entity.water.OrthocerasEntity;
import com.akashi.abyss.abyss.entity.water.TrilobiteEntity;
import com.akashi.abyss.abyss.entity.water.OrthocerasEntity;
import com.akashi.abyss.abyss.entity.heaven.QuetzalcoatlusEntity;


@EventBusSubscriber(modid = Abyss.MODID)
public final class ModEntityEvents {
    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.TRICERATOPS.get(), TriceratopsEntity.createAttributes().build());
        event.put(ModEntities.STEGOSAURUS.get(), StegosaurusEntity.createAttributes().build());
        event.put(
                ModEntities.PARASAUROLOPHUS.get(),
                ParasaurolophusEntity.createAttributes().build()
        );
        event.put(
                ModEntities.ANKYLOSAURUS.get(),
                AnkylosaurusEntity.createAttributes().build()
        );
        event.put(
                ModEntities.BRACHIOSAURUS.get(),
                BrachiosaurusEntity.createAttributes().build()
        );
        event.put(
                ModEntities.TYRANNOSAURUS.get(),
                TyrannosaurusEntity.createAttributes().build()
        );
        event.put(
                ModEntities.SPINOSAURUS.get(),
                SpinosaurusEntity.createAttributes().build()
        );
        event.put(
                ModEntities.GIGANOTOSAURUS.get(),
                GiganotosaurusEntity.createAttributes().build()
        );
        event.put(ModEntities.THERIZINOSAURUS.get(), TherizinosaurusEntity.createAttributes().build());
        event.put(
                ModEntities.MOSASAURUS.get(),
                MosasaurusEntity.createAttributes().build()
        );
        event.put(
                ModEntities.XIPHACTINUS.get(),
                XiphactinusEntity.createAttributes().build()
        );
        event.put(
                ModEntities.ELASMOSAURUS.get(),
                ElasmosaurusEntity.createAttributes().build()
        );
        event.put(
                ModEntities.ARCHELON.get(),
                ArchelonEntity.createAttributes().build()
        );
        event.put(ModEntities.ORTHOCERAS.get(), OrthocerasEntity.createAttributes().build());
        event.put(
                ModEntities.TRILOBITE.get(),
                TrilobiteEntity.createAttributes().build()
        );
        event.put(
                ModEntities.QUETZALCOATLUS.get(),
                QuetzalcoatlusEntity.createAttributes().build()
        );
    }

    @SubscribeEvent
    public static void spawns(RegisterSpawnPlacementsEvent event) {
        event.register(ModEntities.TRICERATOPS.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) ->
                        level.getBlockState(pos.below()).is(Abyss.ABYSS_GRASS.get()),
                RegisterSpawnPlacementsEvent.Operation.OR);
        event.register(
                ModEntities.STEGOSAURUS.get(),
                SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) ->
                        level.getBlockState(pos.below()).isSolidRender(),
                RegisterSpawnPlacementsEvent.Operation.OR
        );
        event.register(
                ModEntities.MOSASAURUS.get(),
                SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) ->
                        level.getFluidState(pos)
                                .is(FluidTags.WATER)
                                && level.getFluidState(pos.above())
                                .is(FluidTags.WATER),
                RegisterSpawnPlacementsEvent.Operation.OR
        );
        event.register(
                ModEntities.ARCHELON.get(),
                SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) ->
                        level.getFluidState(pos).is(FluidTags.WATER)
                                && level.getFluidState(pos.above()).is(FluidTags.WATER),
                RegisterSpawnPlacementsEvent.Operation.OR
        );
    }
    @SubscribeEvent
    public static void creativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModEntities.TRICERATOPS_SPAWN_EGG.get());
            event.accept(ModEntities.STEGOSAURUS_SPAWN_EGG.get());
            event.accept(ModEntities.PARASAUROLOPHUS_SPAWN_EGG.get());
            event.accept(ModEntities.MOSASAURUS_SPAWN_EGG.get());
            event.accept(ModEntities.ARCHELON_SPAWN_EGG.get());
            event.accept(ModEntities.ORTHOCERAS_SPAWN_EGG.get());
        }
    }
}