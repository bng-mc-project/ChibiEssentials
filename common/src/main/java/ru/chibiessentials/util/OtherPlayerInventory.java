package ru.chibiessentials.util;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class OtherPlayerInventory implements Container {
    public static final int SIZE = 45;
    public static final int PLAYER_SLOTS = 41;

    private static final int[] SLOT_MAP = buildSlotMap();

    private final Inventory inventory;
    private final NonNullList<ItemStack> offlineItems;
    private final boolean readOnly;

    public OtherPlayerInventory(Player player) {
        this(player, false);
    }

    public OtherPlayerInventory(Player player, boolean readOnly) {
        this.inventory = player.getInventory();
        this.offlineItems = null;
        this.readOnly = readOnly;
    }

    public OtherPlayerInventory(NonNullList<ItemStack> offlineItems, boolean readOnly) {
        this.inventory = null;
        this.offlineItems = offlineItems;
        this.readOnly = readOnly;
    }

    private static int[] buildSlotMap() {
        int[] map = new int[SIZE];
        int index = 0;
        for (int slot = 9; slot <= 35; slot++) {
            map[index++] = slot;
        }
        for (int slot = 0; slot <= 8; slot++) {
            map[index++] = slot;
        }
        map[index++] = 39;
        map[index++] = 38;
        map[index++] = 37;
        map[index++] = 36;
        map[index++] = 40;
        while (index < SIZE) {
            map[index++] = -1;
        }
        return map;
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (int slot = 0; slot < SIZE; slot++) {
            if (!getItem(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        int mapped = mapSlot(slot);
        return mapped < 0 ? ItemStack.EMPTY : getStoredItem(mapped);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (readOnly) {
            return ItemStack.EMPTY;
        }
        int mapped = mapSlot(slot);
        if (mapped < 0) {
            return ItemStack.EMPTY;
        }
        if (inventory != null) {
            return inventory.removeItem(mapped, amount);
        }
        ItemStack stack = offlineItems.get(mapped);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = stack.split(amount);
        if (stack.isEmpty()) {
            offlineItems.set(mapped, ItemStack.EMPTY);
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (readOnly) {
            return ItemStack.EMPTY;
        }
        int mapped = mapSlot(slot);
        if (mapped < 0) {
            return ItemStack.EMPTY;
        }
        if (inventory != null) {
            return inventory.removeItemNoUpdate(mapped);
        }
        ItemStack stack = offlineItems.get(mapped);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = stack.copy();
        offlineItems.set(mapped, ItemStack.EMPTY);
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (readOnly) {
            return;
        }
        int mapped = mapSlot(slot);
        if (mapped < 0) {
            return;
        }
        if (inventory != null) {
            inventory.setItem(mapped, stack);
        } else {
            offlineItems.set(mapped, stack);
        }
    }

    @Override
    public void setChanged() {
        if (inventory != null) {
            inventory.setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        if (readOnly) {
            return;
        }
        for (int slot = 0; slot < SIZE; slot++) {
            setItem(slot, ItemStack.EMPTY);
        }
    }

    private ItemStack getStoredItem(int mapped) {
        if (inventory != null) {
            return inventory.getItem(mapped);
        }
        return offlineItems.get(mapped);
    }

    private static int mapSlot(int slot) {
        return slot >= 0 && slot < SLOT_MAP.length ? SLOT_MAP[slot] : -1;
    }
}
