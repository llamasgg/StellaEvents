package com.stella.events;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public interface EventRegistry {

    @NotNull Optional<Event> find(@NotNull String id);

    @NotNull Collection<Event> getAll();

    void register(@NotNull String id, @NotNull Event event);

}
