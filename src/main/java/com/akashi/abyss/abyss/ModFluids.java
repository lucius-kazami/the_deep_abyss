package com.akashi.abyss.abyss;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Abyss.MODID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, Abyss.MODID);
    public static final DeferredRegister.Blocks FLUID_BLOCKS =
            DeferredRegister.createBlocks(Abyss.MODID);

    public static final DeferredHolder<FluidType, FluidType> HEALING_WATER_TYPE =
            FLUID_TYPES.register("healing_water", () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("block.abyss.healing_water")
                    .canExtinguish(true)
                    .lightLevel(4)));

    public static final DeferredHolder<Fluid, BaseFlowingFluid.Source> HEALING_WATER =
            FLUIDS.register("healing_water", () -> new BaseFlowingFluid.Source(ModFluids.PROPERTIES));
    public static final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> HEALING_WATER_FLOWING =
            FLUIDS.register("flowing_healing_water", () -> new BaseFlowingFluid.Flowing(ModFluids.PROPERTIES));

    public static final DeferredBlock<LiquidBlock> HEALING_WATER_BLOCK =
            FLUID_BLOCKS.registerBlock("healing_water",
                    props -> new LiquidBlock(HEALING_WATER.get(), props),
                    BlockBehaviour.Properties.of().mapColor(MapColor.WATER).replaceable().noCollision()
                            .strength(100.0F).pushReaction(PushReaction.DESTROY).noLootTable().liquid()
                            .lightLevel(state -> 4));

    // должен идти после полей выше; обращаемся к нему как ModFluids.PROPERTIES из лямбд
    private static final BaseFlowingFluid.Properties PROPERTIES =
            new BaseFlowingFluid.Properties(HEALING_WATER_TYPE, HEALING_WATER, HEALING_WATER_FLOWING)
                    .block(HEALING_WATER_BLOCK);
}