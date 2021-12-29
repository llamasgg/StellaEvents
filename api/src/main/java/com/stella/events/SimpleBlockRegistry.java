package com.stella.events;

import org.bukkit.block.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SimpleBlockRegistry implements BlockRegistry {

    private final List<BlockState> blockStates = new ArrayList<>();

    @NotNull @Override public List<BlockState> getAll(@NotNull Consumer<BlockState> consumer) {
        for (BlockState state : this.blockStates) {
            consumer.accept(state);
        }
        return this.blockStates;
    }

    @Override public void register(@NotNull BlockState blockState) {
        this.blockStates.add(blockState);
    }
}
