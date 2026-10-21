package com.akashi.abyss.abyss.block;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.Containers;
import com.akashi.abyss.abyss.item.ElementCharge;

import java.util.HashMap;
import java.util.Map;

public class AbyssForgeBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_RUBY = 1;
    public static final int SLOT_COAL = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int SLOT_COUNT = 4;

    public static final int MAX_COOK_TIME = 200; // 10 секунд на одну руду
    public static final int RUBY_USES = 4;       // 1 рубин = 4 руды
    public static final int COAL_USES = 8;       // 1 уголь = 8 руд

    private record Recipe(DeferredItem<Item> ingot, DeferredItem<Item> brokenIngot) {}

    private static final Map<Item, Recipe> RECIPES = new HashMap<>();

    public static void bootstrap() {
        RECIPES.put(Abyss.EARTH_RAW.get(), new Recipe(Abyss.EARTH_INGOT, Abyss.EARTH_BROKEN_INGOT));
        RECIPES.put(Abyss.FIRE_RAW.get(), new Recipe(Abyss.FIRE_INGOT, Abyss.FIRE_BROKEN_INGOT));
        RECIPES.put(Abyss.WATER_RAW.get(), new Recipe(Abyss.WATER_INGOT, Abyss.WATER_BROKEN_INGOT));
        RECIPES.put(Abyss.WIND_RAW.get(), new Recipe(Abyss.WIND_INGOT, Abyss.WIND_BROKEN_INGOT));
        RECIPES.put(Abyss.LIGHTNING_RAW.get(), new Recipe(Abyss.LIGHTNING_INGOT, Abyss.LIGHTNING_BROKEN_INGOT));
    }

    public static boolean isSmeltable(ItemStack stack) { return RECIPES.containsKey(stack.getItem()); }

    public static boolean isCoal(ItemStack stack) { return stack.is(Items.COAL) || stack.is(Items.CHARCOAL); }

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case SLOT_INPUT -> isSmeltable(stack);
                case SLOT_RUBY -> stack.is(Abyss.RUBY.get());
                case SLOT_COAL -> isCoal(stack);
                default -> false;
            };
        }
    };

    private int cookProgress;
    private int rubyCharge; // сколько руд ещё можно переплавить на текущем рубине
    private int coalCharge; // то же для угля

    /** [0] прогресс плавки, [1] заряд рубина, [2] заряд угля. */
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> cookProgress;
                case 1 -> rubyCharge;
                case 2 -> coalCharge;
                default -> 0;
            };
        }
        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> cookProgress = value;
                case 1 -> rubyCharge = value;
                case 2 -> coalCharge = value;
            }
        }
        @Override public int getCount() { return 3; }
    };

    public AbyssForgeBlockEntity(BlockPos pos, BlockState state) {
        super(Abyss.ABYSS_FORGE_BE.get(), pos, state);
    }

    public ItemStackHandler getItems() { return items; }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AbyssForgeBlockEntity be) {
        boolean dirty = false;

        ItemStack input = be.items.getStackInSlot(SLOT_INPUT);
        Recipe recipe = input.isEmpty() ? null : RECIPES.get(input.getItem());
        Item resultItem = null;
        if (recipe != null) {
            resultItem = ElementCharge.isUsed(input) ? recipe.brokenIngot().get() : recipe.ingot().get();
        }

        ItemStack output = be.items.getStackInSlot(SLOT_OUTPUT);
        boolean outputFree = resultItem != null
                && (output.isEmpty() || (output.is(resultItem) && output.getCount() < output.getMaxStackSize()));

        // Плавка идёт только если есть И рубин, И уголь (либо заряд, либо предмет в слоте)
        boolean smelting = false;
        if (outputFree) {
            ItemStack rubyStack = be.items.getStackInSlot(SLOT_RUBY);
            ItemStack coalStack = be.items.getStackInSlot(SLOT_COAL);
            boolean rubyOk = be.rubyCharge > 0 || rubyStack.is(Abyss.RUBY.get());
            boolean coalOk = be.coalCharge > 0 || isCoal(coalStack);

            if (rubyOk && coalOk) {
                if (be.rubyCharge <= 0) {
                    rubyStack.shrink(1);
                    be.rubyCharge = RUBY_USES;
                }
                if (be.coalCharge <= 0) {
                    coalStack.shrink(1);
                    be.coalCharge = COAL_USES;
                }
                smelting = true;
            }
        }

        if (smelting) {
            be.cookProgress++;
            dirty = true;
            if (be.cookProgress >= MAX_COOK_TIME) {
                be.cookProgress = 0;
                if (output.isEmpty()) {
                    be.items.setStackInSlot(SLOT_OUTPUT, new ItemStack(resultItem, 1));
                } else {
                    output.grow(1);
                }
                input.shrink(1);
                be.rubyCharge--;
                be.coalCharge--;
            }
        } else if (be.cookProgress > 0) {
            be.cookProgress = Math.max(0, be.cookProgress - 2);
            dirty = true;
        }

        // Кузница светится и «горит» только пока идёт плавка
        if (state.getValue(AbyssForgeBlock.LIT) != smelting) {
            state = state.setValue(AbyssForgeBlock.LIT, smelting);
            level.setBlock(pos, state, 3);
            dirty = true;
        }

        if (dirty) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (int i = 0; i < SLOT_COUNT; i++) {
            output.store("Slot" + i, ItemStack.OPTIONAL_CODEC, items.getStackInSlot(i));
        }
        output.putInt("CookProgress", cookProgress);
        output.putInt("RubyCharge", rubyCharge);
        output.putInt("CoalCharge", coalCharge);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < SLOT_COUNT; i++) {
            items.setStackInSlot(i, input.read("Slot" + i, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
        }
        cookProgress = input.getIntOr("CookProgress", 0);
        rubyCharge = input.getIntOr("RubyCharge", 0);
        coalCharge = input.getIntOr("CoalCharge", 0);
    }

    @Override
    public Component getDisplayName() { return Component.translatable("block.abyss.abyss_forge"); }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level == null || level.isClientSide()) return;
        for (int i = 0; i < items.getSlots(); i++) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, items.getStackInSlot(i));
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new AbyssForgeMenu(id, playerInv, this, data);
    }
}