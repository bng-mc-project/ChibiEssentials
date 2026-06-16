package ru.chibiessentials.util;

public final class TeleportHistoryHelper {
    private static final ThreadLocal<Boolean> SKIP = ThreadLocal.withInitial(() -> false);

    private TeleportHistoryHelper() {}

    public static boolean shouldSkip() {
        return SKIP.get();
    }

    public static void runWithoutHistory(Runnable action) {
        boolean prev = SKIP.get();
        SKIP.set(true);
        try {
            action.run();
        } finally {
            SKIP.set(prev);
        }
    }
}
