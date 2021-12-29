package com.stella.events.player;

import org.bukkit.entity.Player;

public class GamePlayer {

    private final Player player;
    private GamePlayerState state;

    public GamePlayer(Player player) {
        this.player = player;
        this.state = GamePlayerState.ALIVE;
    }

    public void setState(GamePlayerState state) {
        this.state = state;
    }

    public Player getPlayer() {
        return player;
    }

    public GamePlayerState getState() {
        return state;
    }
}
