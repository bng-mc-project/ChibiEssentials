package ru.chibiessentials.command.misc;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import ru.chibiessentials.config.ChibiLang;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlot;
import ru.chibiessentials.permission.ChibiPermissions;
import ru.chibiessentials.permission.PermissionNodes;
import ru.chibiessentials.util.HeadUtil;
import ru.chibiessentials.util.SitHandler;

public final class MiscCommands {
    private MiscCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ec")
                .requires(ChibiPermissions.require(PermissionNodes.EC))
                .executes(ctx -> enderChest(ctx.getSource().getPlayerOrException(), null))
                .then(Commands.argument("player", EntityArgument.player())
                        .requires(ChibiPermissions.require(PermissionNodes.EC_OTHERS))
                        .executes(ctx -> enderChest(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player")))));

        dispatcher.register(Commands.literal("head")
                .requires(ChibiPermissions.require(PermissionNodes.HEAD))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> head(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player")))));

        dispatcher.register(Commands.literal("hat")
                .requires(ChibiPermissions.require(PermissionNodes.HAT))
                .executes(ctx -> hat(ctx.getSource().getPlayerOrException())));

        dispatcher.register(Commands.literal("workbench")
                .requires(ChibiPermissions.require(PermissionNodes.WORKBENCH))
                .executes(ctx -> workbench(ctx.getSource().getPlayerOrException())));

        dispatcher.register(Commands.literal("sit")
                .requires(ChibiPermissions.require(PermissionNodes.SIT))
                .executes(ctx -> sit(ctx.getSource().getPlayerOrException())));
    }

    public static int enderChest(ServerPlayer viewer, ServerPlayer target) {
        ServerPlayer ecOwner = target != null ? target : viewer;
        viewer.openMenu(new net.minecraft.world.SimpleMenuProvider(
                (id, inv, player) -> ChestMenu.threeRows(id, inv, ecOwner.getEnderChestInventory()),
                Component.translatable("container.enderchest")
        ));
        return 1;
    }

    public static int head(ServerPlayer receiver, ServerPlayer target) {
        ItemStack head = HeadUtil.createPlayerHead(target.getGameProfile());
        if (!receiver.getInventory().add(head)) {
            receiver.drop(head, false);
        }
        receiver.displayClientMessage(ChibiLang.get("chibiessentials.head.given", target.getDisplayName()), false);
        return 1;
    }

    public static int hat(ServerPlayer player) {
        ItemStack hand = player.getMainHandItem();
        if (hand.isEmpty()) {
            player.displayClientMessage(ChibiLang.get("chibiessentials.hat.empty"), false);
            return 0;
        }
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD).copy();
        player.setItemSlot(EquipmentSlot.HEAD, hand.copy());
        player.setItemSlot(EquipmentSlot.MAINHAND, helmet);
        player.displayClientMessage(ChibiLang.get("chibiessentials.hat.equipped"), false);
        return 1;
    }

    public static int workbench(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new CraftingMenu(id, inv),
                Component.translatable("container.crafting")
        ));
        return 1;
    }

    public static int sit(ServerPlayer player) {
        int result = SitHandler.sit(player);
        if (result == 0) {
            return 0;
        }
        player.displayClientMessage(ChibiLang.get(result == 1 ? "chibiessentials.sit.on" : "chibiessentials.sit.off"), false);
        return 1;
    }
}
