package com.stellamc.events.player;

import org.bukkit.entity.Player;

public class GamePlayer {

    private final Player player;
    private int kills;

    public GamePlayer(Player player) {
        this.player = player;
        this.kills = 0;
    }

    public void addKill() {
        this.kills++;
    }

    public int getKills() {
        return kills;
    }

    public Player getPlayer() {
        return player;
    }
}
