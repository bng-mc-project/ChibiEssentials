package ru.chibiessentials.util;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import ru.chibiessentials.ChibiEssentials;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

public final class OfflinePlayerStorage {
    public static final int INVENTORY_SLOTS = 41;
    public static final int ENDER_CHEST_SLOTS = 27;

    private static final String INVENTORY_TAG = "Inventory";
    private static final String ENDER_ITEMS_TAG = "EnderItems";

    private final Path file;
    private CompoundTag root;

    private OfflinePlayerStorage(Path file, CompoundTag root) {
        this.file = file;
        this.root = root;
    }

    public static Optional<OfflinePlayerStorage> load(MinecraftServer server, UUID uuid) {
        Path file = playerDataFile(server, uuid);
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try {
            return Optional.of(new OfflinePlayerStorage(file, NbtIo.readCompressed(file.toFile())));
        } catch (IOException e) {
            ChibiEssentials.LOGGER.error("Failed to load offline player data for {}", uuid, e);
            return Optional.empty();
        }
    }

    public NonNullList<ItemStack> loadInventory() {
        NonNullList<ItemStack> items = NonNullList.withSize(INVENTORY_SLOTS, ItemStack.EMPTY);
        loadPlayerInventory(root, items);
        return items;
    }

    public NonNullList<ItemStack> loadEnderChest() {
        NonNullList<ItemStack> items = NonNullList.withSize(ENDER_CHEST_SLOTS, ItemStack.EMPTY);
        loadItems(root, items, ENDER_ITEMS_TAG);
        return items;
    }

    public void saveInventory(NonNullList<ItemStack> items) {
        savePlayerInventory(root, items);
    }

    public void saveEnderChest(NonNullList<ItemStack> items) {
        saveItems(root, items, ENDER_ITEMS_TAG);
    }

    public void save() {
        try {
            Files.createDirectories(file.getParent());
            NbtIo.writeCompressed(root, file.toFile());
        } catch (IOException e) {
            ChibiEssentials.LOGGER.error("Failed to save offline player data {}", file, e);
        }
    }

    private static void loadPlayerInventory(CompoundTag root, NonNullList<ItemStack> items) {
        ListTag list = root.getList(INVENTORY_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            int nbtSlot = itemTag.getByte("Slot") & 255;
            int slot = mapNbtSlotToInventory(nbtSlot);
            if (slot >= 0 && slot < items.size()) {
                items.set(slot, ItemStack.of(itemTag));
            }
        }
    }

    private static void savePlayerInventory(CompoundTag root, NonNullList<ItemStack> items) {
        ListTag list = new ListTag();
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (stack.isEmpty()) {
                continue;
            }
            int nbtSlot = mapInventorySlotToNbt(slot);
            if (nbtSlot < 0) {
                continue;
            }
            CompoundTag itemTag = new CompoundTag();
            itemTag.putByte("Slot", (byte) nbtSlot);
            stack.save(itemTag);
            list.add(itemTag);
        }
        root.put(INVENTORY_TAG, list);
    }

    /** Vanilla stores armor at 100-103 and offhand at 150 in player .dat files. */
    private static int mapNbtSlotToInventory(int nbtSlot) {
        if (nbtSlot >= 0 && nbtSlot < 36) {
            return nbtSlot;
        }
        if (nbtSlot >= 100 && nbtSlot < 104) {
            return nbtSlot - 100 + 36;
        }
        if (nbtSlot == 150) {
            return 40;
        }
        return -1;
    }

    private static int mapInventorySlotToNbt(int slot) {
        if (slot >= 0 && slot < 36) {
            return slot;
        }
        if (slot >= 36 && slot < 40) {
            return slot - 36 + 100;
        }
        if (slot == 40) {
            return 150;
        }
        return -1;
    }

    private static void loadItems(CompoundTag root, NonNullList<ItemStack> items, String key) {
        ListTag list = root.getList(key, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < items.size()) {
                items.set(slot, ItemStack.of(itemTag));
            }
        }
    }

    private static void saveItems(CompoundTag root, NonNullList<ItemStack> items, String key) {
        ListTag list = new ListTag();
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putByte("Slot", (byte) slot);
                stack.save(itemTag);
                list.add(itemTag);
            }
        }
        root.put(key, list);
    }

    private static Path playerDataFile(MinecraftServer server, UUID uuid) {
        return server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(uuid + ".dat");
    }
}
