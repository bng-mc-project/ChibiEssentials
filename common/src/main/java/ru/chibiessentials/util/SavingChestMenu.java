package ru.chibiessentials.util;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class SavingChestMenu extends ChestMenu {
    private final boolean editable;
    private final Container targetContainer;
    private final Runnable onClose;
    private final int containerSlots;

    public SavingChestMenu(MenuType<?> type, int containerId, net.minecraft.world.entity.player.Inventory playerInventory,
                           Container container, int rows, boolean editable, Container targetContainer, Runnable onClose) {
        super(type, containerId, playerInventory, container, rows);
        this.editable = editable;
        this.targetContainer = targetContainer;
        this.onClose = onClose;
        this.containerSlots = rows * 9;
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
        if (!editable && slot.container == targetContainer) {
            return false;
        }
        return super.canDragTo(slot);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (onClose != null) {
            onClose.run();
        }
    }

    private boolean isTargetMenuSlot(int slotId) {
        return slotId >= 0 && slotId < containerSlots;
    }

    private boolean intersectsTargetSlots(int startIndex, int endIndex) {
        return startIndex < containerSlots && endIndex > 0;
    }
}
