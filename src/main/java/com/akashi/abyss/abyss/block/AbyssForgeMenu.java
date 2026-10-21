package com.akashi.abyss.abyss.block;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.SlotItemHandler;

public class AbyssForgeMenu extends AbstractContainerMenu {

    private static final int SLOT_COUNT = 4;

    private final AbyssForgeBlockEntity blockEntity;
    private final ContainerData data;

    public AbyssForgeMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv, (AbyssForgeBlockEntity) playerInv.player.level().getBlockEntity(buf.readBlockPos()),
                new SimpleContainerData(3));
    }

    public AbyssForgeMenu(int id, Inventory playerInv, AbyssForgeBlockEntity be, ContainerData data) {
        super(Abyss.ABYSS_FORGE_MENU.get(), id);
        this.blockEntity = be;
        this.data = data;

        // Слоты кузницы (координаты совпадают с рамками в abyss_forge.png)
        addSlot(new SlotItemHandler(be.getItems(), AbyssForgeBlockEntity.SLOT_INPUT, 80, 20) {
            @Override public boolean mayPlace(ItemStack stack) { return AbyssForgeBlockEntity.isSmeltable(stack); }
        });
        addSlot(new SlotItemHandler(be.getItems(), AbyssForgeBlockEntity.SLOT_RUBY, 44, 58) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.is(Abyss.RUBY.get()); }
        });
        addSlot(new SlotItemHandler(be.getItems(), AbyssForgeBlockEntity.SLOT_COAL, 80, 58) {
            @Override public boolean mayPlace(ItemStack stack) { return isCoal(stack); }
        });
        addSlot(new SlotItemHandler(be.getItems(), AbyssForgeBlockEntity.SLOT_OUTPUT, 116, 58) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });

        // Инвентарь игрока
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // Хотбар
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }

        addDataSlots(data);
    }

    private static boolean isCoal(ItemStack stack) {
        return stack.is(Items.COAL) || stack.is(Items.CHARCOAL);
    }

    /** Данные: [0] прогресс плавки, [1] остаток заряда рубина, [2] остаток заряда угля. */
    public int getCookProgress() { return data.get(0) * 24 / AbyssForgeBlockEntity.MAX_COOK_TIME; }
    public int getRubyCharge() { return data.get(1); }
    public int getCoalCharge() { return data.get(2); }
    public boolean isLit() { return data.get(0) > 0; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < SLOT_COUNT) {
            // из кузницы в инвентарь
            if (!moveItemStackTo(stack, SLOT_COUNT, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            // из инвентаря в нужный слот кузницы
            int target;
            if (stack.is(Abyss.RUBY.get())) target = AbyssForgeBlockEntity.SLOT_RUBY;
            else if (isCoal(stack)) target = AbyssForgeBlockEntity.SLOT_COAL;
            else target = AbyssForgeBlockEntity.SLOT_INPUT;
            if (!moveItemStackTo(stack, target, target + 1, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();

        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null && !blockEntity.isRemoved();
    }
}