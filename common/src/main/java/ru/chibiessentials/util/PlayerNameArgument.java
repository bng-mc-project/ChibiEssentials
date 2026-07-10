package ru.chibiessentials.util;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.Optional;

public final class PlayerNameArgument {
    private static final SuggestionProvider<CommandSourceStack> SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(context.getSource().getOnlinePlayerNames(), builder);

    private PlayerNameArgument() {}

    public static RequiredArgumentBuilder<CommandSourceStack, String> player(String name) {
        return Commands.argument(name, StringArgumentType.word()).suggests(SUGGESTIONS);
    }

    public static Optional<PlayerResolver.Target> find(CommandContext<CommandSourceStack> context, String name) {
        String playerName = StringArgumentType.getString(context, name);
        return PlayerResolver.resolve(context.getSource().getServer(), playerName);
    }
}
