package com.forest.access.core;

public final class AccessContextHolder {
    private static final ThreadLocal<AccessCheckContext> CONTEXT = new ThreadLocal<>();

    private AccessContextHolder() {
    }

    public static void set(AccessCheckContext context) {
        CONTEXT.set(context);
    }

    public static AccessCheckContext get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
