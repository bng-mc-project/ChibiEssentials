package ru.chibiessentials.command.cheat;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import ru.chibiessentials.config.ChibiLang;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import ru.chibiessentials.data.PlayerDataManager;
import ru.chibiessentials.permission.ChibiPermissions;
import ru.chibiessentials.permission.PermissionNodes;
import ru.chibiessentials.util.InvSeeMenu;
import ru.chibiessentials.util.OfflinePlayerStorage;
import ru.chibiessentials.util.OtherPlayerInventory;
import ru.chibiessentials.util.PlayerNameArgument;
import ru.chibiessentials.util.PlayerResolver;

public final class CheatCommands {
    private CheatCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        registerToggle(dispatcher, "fly", PermissionNodes.FLY, CheatCommands::fly);
        registerToggle(dispatcher, "god", PermissionNodes.GOD, CheatCommands::god);

        dispatcher.register(Commands.literal("heal")
                .requires(ChibiPermissions.require(PermissionNodes.HEAL))
                .executes(ctx -> heal(ctx.getSource().getPlayerOrException()))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> heal(EntityArgument.getPlayer(ctx, "player")))));

        dispatcher.register(Commands.literal("feed")
                .requires(ChibiPermissions.require(PermissionNodes.FEED))
                .executes(ctx -> feed(ctx.getSource().getPlayerOrException(), ctx.getSource().getPlayerOrException()))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> feed(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player")))));

        dispatcher.register(Commands.literal("invsee")
                .requires(ChibiPermissions.require(PermissionNodes.INVSEE))
                .then(PlayerNameArgument.player("player")
                        .executes(ctx -> invsee(ctx.getSource().getPlayerOrException(),
                                PlayerNameArgument.find(ctx, "player").orElse(null)))));
    }

    private static void registerToggle(CommandDispatcher<CommandSourceStack> dispatcher, String name, String node,
                                       java.util.function.BiFunction<ServerPlayer, ServerPlayer, Integer> executor) {
        dispatcher.register(Commands.literal(name)
                .requires(ChibiPermissions.require(node))
                .executes(ctx -> executor.apply(ctx.getSource().getPlayerOrException(), ctx.getSource().getPlayerOrException()))
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> executor.apply(ctx.getSource().getPlayerOrException(), EntityArgument.getPlayer(ctx, "player")))));
    }

    public static int fly(ServerPlayer executor, ServerPlayer target) {
        return PlayerDataManager.getOrCreate(target).map(data -> {
            boolean enabled = !data.isFly();
            data.setFly(enabled);
            applyFly(target, enabled);
            executor.displayClientMessage(ChibiLang.get(enabled ? "chibiessentials.fly.on" : "chibiessentials.fly.off", target.getDisplayName()), false);
            return 1;
        }).orElse(0);
    }

    public static int god(ServerPlayer executor, ServerPlayer target) {
        return PlayerDataManager.getOrCreate(target).map(data -> {
            boolean enabled = !data.isGod();
            data.setGod(enabled);
            executor.displayClientMessage(ChibiLang.get(enabled ? "chibiessentials.god.on" : "chibiessentials.god.off", target.getDisplayName()), false);
            return 1;
        }).orElse(0);
    }

    public static int heal(ServerPlayer target) {
        target.setHealth(target.getMaxHealth());
        FoodData food = target.getFoodData();
        food.setFoodLevel(20);
        food.setSaturation(20f);
        target.clearFire();
        target.getActiveEffects().stream().map(MobEffectInstance::getEffect).forEach(target::removeEffect);
        target.displayClientMessage(ChibiLang.get("chibiessentials.heal.done"), false);
        return 1;
    }

    public static int feed(ServerPlayer executor, ServerPlayer target) {
        FoodData food = target.getFoodData();
        food.setFoodLevel(20);
        food.setSaturation(20f);
        target.displayClientMessage(ChibiLang.get("chibiessentials.feed.done"), false);
        if (!executor.equals(target)) {
            executor.displayClientMessage(ChibiLang.get("chibiessentials.feed.done_other", target.getDisplayName()), false);
        }
        return 1;
    }

    public static int invsee(ServerPlayer viewer, PlayerResolver.Target target) {
        if (target == null) {
            viewer.displayClientMessage(ChibiLang.get("chibiessentials.player.not_found"), false);
            return 0;
        }
        if (!target.isOnline() && OfflinePlayerStorage.load(viewer.server, target.uuid()).isEmpty()) {
            viewer.displayClientMessage(ChibiLang.get("chibiessentials.player.no_data", target.name()), false);
            return 0;
        }

        boolean editable = ChibiPermissions.has(viewer, PermissionNodes.INVSEE_EDIT);
        MenuProvider provider = new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal(target.name());
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
                return buildInvSeeMenu(id, inv, viewer, target, editable);
            }
        };
        viewer.openMenu(provider);
        return 1;
    }

    private static AbstractContainerMenu buildInvSeeMenu(int id, Inventory inv, ServerPlayer viewer,
                                                         PlayerResolver.Target target, boolean editable) {
        if (target.isOnline()) {
            OtherPlayerInventory targetInventory = new OtherPlayerInventory(target.online(), !editable);
            return new InvSeeMenu(id, inv, targetInventory, editable);
        }

        OfflinePlayerStorage storage = OfflinePlayerStorage.load(viewer.server, target.uuid()).orElseThrow();
        var items = storage.loadInventory();
        OtherPlayerInventory targetInventory = new OtherPlayerInventory(items, !editable);
        Runnable onClose = editable ? () -> {
            storage.saveInventory(items);
            storage.save();
        } : null;
        return new InvSeeMenu(id, inv, targetInventory, editable, onClose);
    }

    public static void applyFly(ServerPlayer player, boolean enabled) {
        boolean allowFly = enabled || player.isCreative() || player.isSpectator();
        player.getAbilities().mayfly = allowFly;
        if (!allowFly) {
            player.getAbilities().flying = false;
        }
        player.onUpdateAbilities();
    }

    public static void reapplyStates(ServerPlayer player) {
        PlayerDataManager.getOrCreate(player).ifPresent(data -> applyFly(player, data.isFly()));
    }
}
