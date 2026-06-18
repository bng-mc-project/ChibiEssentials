package ru.chibiessentials.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import ru.chibiessentials.ChibiEssentials;
import ru.chibiessentials.util.MessageUtil;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class ChibiLang {
    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>() {}.getType();
    private static Map<String, String> strings = Map.of();

    private ChibiLang() {}

    public static void load() {
        try {
            ensureLangFiles();
            Path path = ChibiConfig.getConfigDir().resolve("lang").resolve(ChibiConfig.language() + ".json");
            if (Files.exists(path)) {
                try (Reader reader = Files.newBufferedReader(path)) {
                    Map<String, String> loaded = GSON.fromJson(reader, MAP_TYPE);
                    strings = loaded != null ? loaded : Map.of();
                }
            } else {
                ChibiEssentials.LOGGER.error("Language file not found: {}", path);
                strings = Map.of();
            }
        } catch (IOException e) {
            ChibiEssentials.LOGGER.error("Failed to load language file", e);
            strings = Map.of();
        }
    }

    private static void ensureLangFiles() throws IOException {
        Path langDir = ChibiConfig.getConfigDir().resolve("lang");
        Files.createDirectories(langDir);
        for (String locale : List.of("en_us", "ru_ru")) {
            Path target = langDir.resolve(locale + ".json");
            if (!Files.exists(target)) {
                try (var stream = ChibiLang.class.getResourceAsStream("/chibiessentials/lang/" + locale + ".json")) {
                    if (stream != null) {
                        Files.copy(stream, target);
                    }
                }
            }
        }
    }

    public static MutableComponent get(String key, Object... args) {
        String template = strings.getOrDefault(key, key);
        MutableComponent result = Component.empty();
        int argIndex = 0;
        int lastEnd = 0;
        for (int i = 0; i < template.length() - 1; i++) {
            if (template.charAt(i) == '%' && template.charAt(i + 1) == 's') {
                if (i > lastEnd) {
                    result.append(MessageUtil.colorize(template.substring(lastEnd, i)));
                }
                if (argIndex < args.length) {
                    Object arg = args[argIndex++];
                    if (arg instanceof Component component) {
                        result.append(component);
                    } else {
                        result.append(MessageUtil.colorize(String.valueOf(arg)));
                    }
                }
                lastEnd = i + 2;
                i++;
            }
        }
        if (lastEnd < template.length()) {
            result.append(MessageUtil.colorize(template.substring(lastEnd)));
        }
        return result;
    }
}
