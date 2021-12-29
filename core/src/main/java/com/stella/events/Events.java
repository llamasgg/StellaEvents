package com.stella.events;

import org.jetbrains.annotations.NotNull;

public class Events {

    private static final EventRegistry eventRegistry;

    static {
        System.out.println("synced");
        eventRegistry = new SimpleEventRegistry();
    }

    public static void create(@NotNull String id, @NotNull Event event) {
        eventRegistry.register(id, event);
    }

    public static EventRegistry getEventRegistry() {
        return eventRegistry;
    }
}
