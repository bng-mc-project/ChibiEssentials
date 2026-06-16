package ru.chibiessentials.command.home;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import ru.chibiessentials.data.PlayerData;
import ru.chibiessentials.data.PlayerDataManager;
import ru.chibiessentials.permission.ChibiPermissions;
import ru.chibiessentials.permission.PermissionNodes;
import ru.chibiessentials.util.TeleportPos;

import java.util.Locale;

public final class HomeCommands {
    private HomeCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> sethome = Commands.literal("sethome")
                .requires(ChibiPermissions.require(PermissionNodes.SETHOME, 0))
                .executes(ctx -> setHome(ctx.getSource().getPlayerOrException(), null))
                .then(Commands.argument("name", StringArgumentType.string())
                        .executes(ctx -> setHome(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "name"))));

        LiteralArgumentBuilder<CommandSourceStack> home = Commands.literal("home")
                .requires(ChibiPermissions.require(PermissionNodes.HOME, 0))
                .executes(ctx -> home(ctx.getSource().getPlayerOrException(), null))
                .then(Commands.argument("name", StringArgumentType.string())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                PlayerDataManager.getOrCreate(ctx.getSource().getPlayerOrException())
                                        .map(d -> d.getHomes().keySet()).orElse(java.util.Set.of()), builder))
                        .executes(ctx -> home(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "name"))));

        dispatcher.register(sethome);
        dispatcher.register(home);

        dispatcher.register(Commands.literal("delhome")
                .requires(ChibiPermissions.require(PermissionNodes.SETHOME, 0))
                .then(Commands.argument("name", StringArgumentType.string())
                        .executes(ctx -> delHome(ctx.getSource().getPlayerOrException(), StringArgumentType.getString(ctx, "name")))));

        dispatcher.register(Commands.literal("listhomes")
                .requires(ChibiPermissions.require(PermissionNodes.HOME, 0))
                .executes(ctx -> listHomes(ctx.getSource().getPlayerOrException())));
    }

    private static String resolveHomeName(ServerPlayer player, String name, boolean required) {
        int maxHomes = PlayerDataManager.getOrCreate(player).map(d -> d.getMaxHomes(player)).orElse(1);
        if (maxHomes <= 1) {
            if (name != null) {
                throw new IllegalStateException("single-home");
            }
            return "home";
        }
        if (name == null || name.isBlank()) {
            if (required) throw new IllegalStateException("multi-home-missing-name");
            return null;
        }
        return name.toLowerCase(Locale.ROOT);
    }

    public static int setHome(ServerPlayer player, String name) {
        return PlayerDataManager.getOrCreate(player).map(data -> {
            int maxHomes = data.getMaxHomes(player);
            String homeName;
            try {
                homeName = resolveHomeName(player, name, maxHomes > 1);
            } catch (IllegalStateException e) {
                if ("single-home".equals(e.getMessage())) {
                    player.displayClientMessage(Component.translatable("chibiessentials.home.use_without_name"), false);
                } else {
                    player.displayClientMessage(Component.translatable("chibiessentials.home.name_required"), false);
                }
                return 0;
            }

            if (maxHomes > 1 && !data.getHomes().containsKey(homeName) && data.getHomes().size() >= maxHomes) {
                player.displayClientMessage(Component.translatable("chibiessentials.home.limit", maxHomes), false);
                return 0;
            }

            data.getHomes().put(homeName, new TeleportPos(player));
            data.markDirty();
            player.displayClientMessage(Component.translatable("chibiessentials.home.set", homeName), false);
            return 1;
        }).orElse(0);
    }

    public static int home(ServerPlayer player, String name) {
        return PlayerDataManager.getOrCreate(player).map(data -> {
            int maxHomes = data.getMaxHomes(player);
            String homeName;
            try {
                homeName = resolveHomeName(player, name, maxHomes > 1);
            } catch (IllegalStateException e) {
                if ("single-home".equals(e.getMessage())) {
                    player.displayClientMessage(Component.translatable("chibiessentials.home.use_without_name"), false);
                } else {
                    player.displayClientMessage(Component.translatable("chibiessentials.home.name_required"), false);
                }
                return 0;
            }

            if (!data.getHomes().containsKey(homeName)) {
                player.displayClientMessage(Component.translatable("chibiessentials.home.not_found", homeName), false);
                return 0;
            }

            String finalHomeName = homeName;
            return data.homeTeleporter.teleport(player, p -> data.getHomes().get(finalHomeName)).runCommand(player);
        }).orElse(0);
    }

    public static int delHome(ServerPlayer player, String name) {
        return PlayerDataManager.getOrCreate(player).map(data -> {
            if (data.getMaxHomes(player) <= 1) {
                player.displayClientMessage(Component.translatable("chibiessentials.home.del_not_available"), false);
                return 0;
            }
            String homeName = name.toLowerCase(Locale.ROOT);
            if (data.getHomes().remove(homeName) != null) {
                data.markDirty();
                player.displayClientMessage(Component.translatable("chibiessentials.home.deleted", homeName), false);
                return 1;
            }
            player.displayClientMessage(Component.translatable("chibiessentials.home.not_found", homeName), false);
            return 0;
        }).orElse(0);
    }

    public static int listHomes(ServerPlayer player) {
        return PlayerDataManager.getOrCreate(player).map(data -> {
            if (data.getMaxHomes(player) <= 1) {
                player.displayClientMessage(Component.translatable("chibiessentials.home.list_not_available"), false);
                return 0;
            }
            if (data.getHomes().isEmpty()) {
                player.displayClientMessage(Component.translatable("chibiessentials.home.list_empty"), false);
                return 1;
            }
            player.displayClientMessage(Component.translatable("chibiessentials.home.list", String.join(", ", data.getHomes().keySet())), false);
            return 1;
        }).orElse(0);
    }
}
