package ru.chibiessentials.util;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class OtherPlayerInventory implements Container {
    public static final int SIZE = 45;

    private static final int[] SLOT_MAP = buildSlotMap();

    private final Inventory inventory;
    private final boolean readOnly;

    public OtherPlayerInventory(Player player) {
        this(player, false);
    }

    public OtherPlayerInventory(Player player, boolean readOnly) {
        this.inventory = player.getInventory();
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
        return mapped < 0 ? ItemStack.EMPTY : inventory.getItem(mapped);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (readOnly) {
            return ItemStack.EMPTY;
        }
        int mapped = mapSlot(slot);
        return mapped < 0 ? ItemStack.EMPTY : inventory.removeItem(mapped, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (readOnly) {
            return ItemStack.EMPTY;
        }
        int mapped = mapSlot(slot);
        return mapped < 0 ? ItemStack.EMPTY : inventory.removeItemNoUpdate(mapped);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (readOnly) {
            return;
        }
        int mapped = mapSlot(slot);
        if (mapped >= 0) {
            inventory.setItem(mapped, stack);
        }
    }

    @Override
    public void setChanged() {
        inventory.setChanged();
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

    private static int mapSlot(int slot) {
        return slot >= 0 && slot < SLOT_MAP.length ? SLOT_MAP[slot] : -1;
    }
}
