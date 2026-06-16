package ru.chibiessentials.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class HeadUtil {
    private HeadUtil() {}

    public static ItemStack createPlayerHead(GameProfile profile) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        CompoundTag tag = new CompoundTag();
        NbtUtils.writeGameProfile(tag, profile);
        stack.getOrCreateTag().put("SkullOwner", tag);
        return stack;
    }
}
