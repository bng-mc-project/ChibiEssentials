package ru.chibiessentials.command.teleport;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import ru.chibiessentials.config.ChibiLang;
import ru.chibiessentials.permission.ChibiPermissions;
import ru.chibiessentials.permission.PermissionNodes;
import ru.chibiessentials.util.TeleportHistoryHelper;
import ru.chibiessentials.util.TeleportPos;

import java.util.Locale;

public final class TpposCommands {
    private TpposCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var zArg = Commands.argument("z", DoubleArgumentType.doubleArg())
                .executes(ctx -> tppos(ctx, null, null, null))
                .then(Commands.argument("world", StringArgumentType.word())
                        .executes(ctx -> tppos(ctx, null, null, StringArgumentType.getString(ctx, "world"))))
                .then(Commands.argument("yaw", FloatArgumentType.floatArg())
                        .executes(ctx -> tppos(ctx, FloatArgumentType.getFloat(ctx, "yaw"), null, null))
                        .then(Commands.argument("pitch", FloatArgumentType.floatArg())
                                .executes(ctx -> tppos(ctx,
                                        FloatArgumentType.getFloat(ctx, "yaw"),
                                        FloatArgumentType.getFloat(ctx, "pitch"),
                                        null))
                                .then(Commands.argument("world", StringArgumentType.word())
                                        .executes(ctx -> tppos(ctx,
                                                FloatArgumentType.getFloat(ctx, "yaw"),
                                                FloatArgumentType.getFloat(ctx, "pitch"),
                                                StringArgumentType.getString(ctx, "world"))))));

        dispatcher.register(Commands.literal("tppos")
                .requires(ChibiPermissions.require(PermissionNodes.TPPOS, 2))
                .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                        .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                .then(zArg))));
    }

    private static int tppos(CommandContext<CommandSourceStack> ctx, Float yaw, Float pitch, String world) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        double x = DoubleArgumentType.getDouble(ctx, "x");
        double y = DoubleArgumentType.getDouble(ctx, "y");
        double z = DoubleArgumentType.getDouble(ctx, "z");

        ResourceKey<Level> dimension = resolveDimension(ctx.getSource(), world);
        if (dimension == null) {
            return TeleportPos.TeleportResult.failed(ChibiLang.get("chibiessentials.teleport.dimension_not_found")).runCommand(player);
        }

        var level = player.server.getLevel(dimension);
        if (level == null) {
            return TeleportPos.TeleportResult.failed(ChibiLang.get("chibiessentials.teleport.dimension_not_found")).runCommand(player);
        }

        int xpLevel = player.experienceLevel;
        float yawRot = yaw != null ? yaw : player.getYRot();
        float pitchRot = pitch != null ? pitch : player.getXRot();

        TeleportHistoryHelper.runWithoutHistory(() ->
                player.teleportTo(level, x, y, z, yawRot, pitchRot));
        player.setExperienceLevels(xpLevel);
        return 1;
    }

    private static ResourceKey<Level> resolveDimension(CommandSourceStack source, String world) {
        if (world == null) {
            return source.getLevel().dimension();
        }
        ResourceLocation id = parseWorldId(world);
        if (id == null) {
            return null;
        }
        return ResourceKey.create(Registries.DIMENSION, id);
    }

    private static ResourceLocation parseWorldId(String world) {
        if (world.contains(":")) {
            return ResourceLocation.tryParse(world);
        }
        return switch (world.toLowerCase(Locale.ROOT)) {
            case "overworld", "world" -> Level.OVERWORLD.location();
            case "nether", "the_nether" -> Level.NETHER.location();
            case "end", "the_end" -> Level.END.location();
            default -> ResourceLocation.tryParse("minecraft:" + world);
        };
    }
}
