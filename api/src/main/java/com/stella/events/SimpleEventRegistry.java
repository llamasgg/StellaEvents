package com.stella.events;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SimpleEventRegistry implements EventRegistry {

    private final Map<String, Event> events = new HashMap<>();

    @NotNull @Override public Optional<Event> find(@NotNull String id) {
        if (this.events.containsKey(id.toLowerCase())) {
            System.out.println("contains");
            return Optional.of(this.events.get(id.toLowerCase()));
        } else return Optional.empty();

    }

    @NotNull @Override public Collection<Event> getAll() {
        return this.events.values();
    }

    @Override public void register(@NotNull String id, @NotNull Event event) {
        this.events.put(id, event);
    }
}
