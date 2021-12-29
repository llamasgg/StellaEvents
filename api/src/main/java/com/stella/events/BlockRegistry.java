package com.stella.events;

import org.bukkit.block.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public interface BlockRegistry {

    @NotNull List<BlockState> getAll(@NotNull Consumer<BlockState> consumer);

    default @NotNull List<BlockState> getAll() {
        return getAll(blockState -> {});
    }

    void register(@NotNull BlockState blockState);
}
