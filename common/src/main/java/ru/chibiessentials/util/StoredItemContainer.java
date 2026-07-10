package ru.chibiessentials.util;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class StoredItemContainer implements Container {
    private final NonNullList<ItemStack> items;
    private final boolean readOnly;

    public StoredItemContainer(NonNullList<ItemStack> items, boolean readOnly) {
        this.items = items;
        this.readOnly = readOnly;
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < items.size() ? items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (readOnly || slot < 0 || slot >= items.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = items.get(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = stack.split(amount);
        if (stack.isEmpty()) {
            items.set(slot, ItemStack.EMPTY);
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (readOnly || slot < 0 || slot >= items.size()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = items.get(slot);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = stack.copy();
        stack.setCount(0);
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (readOnly || slot < 0 || slot >= items.size()) {
            return;
        }
        items.set(slot, stack);
    }

    @Override
    public void setChanged() {
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
        items.replaceAll(ignored -> ItemStack.EMPTY);
    }
}
