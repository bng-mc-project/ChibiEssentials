package ru.chibiessentials.command.misc;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import ru.chibiessentials.permission.ChibiPermissions;
import ru.chibiessentials.permission.PermissionNodes;
import ru.chibiessentials.util.HeadUtil;

public final class MiscCommands {
    private MiscCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ec")
                .requires(ChibiPermissions.require(PermissionNodes.EC, 0))
                .executes(ctx -> enderChest(ctx.getSource().getPlayerOrException(), null))
                .then(Commands.argument("player", EntityArgument.player())
                        .requires(ChibiPermissions.require(PermissionNodes.EC_OTHERS, 2))
                        .executes(ctx -> enderChest(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player")))));

        dispatcher.register(Commands.literal("head")
                .requires(ChibiPermissions.require(PermissionNodes.HEAD, 2))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> head(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player")))));
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
        receiver.displayClientMessage(Component.translatable("chibiessentials.head.given", target.getDisplayName()), false);
        return 1;
    }
}
