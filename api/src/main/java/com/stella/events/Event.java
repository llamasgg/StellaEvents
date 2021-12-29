package com.stella.events;

import org.jetbrains.annotations.NotNull;

public interface Event {

    @NotNull String getName();

    void start();

    void stop();
}
