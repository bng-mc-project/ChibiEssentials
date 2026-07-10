package ru.chibiessentials.util;

import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class InvSeeMenu extends ChestMenu {
    private static final int ROWS = 5;

    private final boolean editable;
    private final OtherPlayerInventory targetInventory;

    public InvSeeMenu(int containerId, Inventory playerInventory, OtherPlayerInventory targetInventory, boolean editable) {
        super(MenuType.GENERIC_9x5, containerId, playerInventory, targetInventory, ROWS);
        this.editable = editable;
        this.targetInventory = targetInventory;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (!editable && isTargetMenuSlot(slotId)) {
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (!editable && isTargetMenuSlot(index)) {
            return ItemStack.EMPTY;
        }
        return super.quickMoveStack(player, index);
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        if (!editable && intersectsTargetSlots(startIndex, endIndex)) {
            return false;
        }
        return super.moveItemStackTo(stack, startIndex, endIndex, reverseDirection);
    }

    @Override
    public boolean canDragTo(Slot slot) {
        if (!editable && slot.container == targetInventory) {
            return false;
        }
        return super.canDragTo(slot);
    }

    private boolean isTargetMenuSlot(int slotId) {
        return slotId >= 0 && slotId < ROWS * 9;
    }

    private boolean intersectsTargetSlots(int startIndex, int endIndex) {
        return startIndex < ROWS * 9 && endIndex > 0;
    }
}
