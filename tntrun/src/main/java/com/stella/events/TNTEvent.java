package com.stella.events;

import com.stella.events.player.GamePlayer;
import com.stella.events.player.GamePlayerState;
import me.lucko.helper.Events;
import me.lucko.helper.Helper;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class TNTEvent implements Event {

    private final Map<UUID, GamePlayer> players = new HashMap<>();
    private final BlockRegistry blockRegistry;
    private final Location location;

    public TNTEvent(@NotNull Location location) {
        this.location = location;
        this.blockRegistry= new SimpleBlockRegistry();

        Events.subscribe(PlayerMoveEvent.class).handler(event -> {
            Block block = event.getFrom().clone().subtract(0, 1,0).getBlock();
            if (block == null || block.getType() != Material.SAND) return;
            blockRegistry.register(block.getState());
            block.setType(Material.AIR, false);
        });

        Events.subscribe(PlayerQuitEvent.class).handler(event -> {
           players.remove(event.getPlayer().getUniqueId());
           end();
        });

        Events.subscribe(PlayerDeathEvent.class).handler(event -> {
            Player player = event.getEntity();
            if (!player.getWorld().getName().equals(location.getWorld().getName())) {
                return;
            }

            if (players.containsKey(player.getUniqueId())) {
                players.get(player.getUniqueId()).setState(GamePlayerState.DEAD);
                player.setGameMode(GameMode.SPECTATOR);
                event.setDeathMessage(ChatColor.RED + player.getName() + " has been eliminated");
                end();
            }
        });
    }

    @Override public void start() {

        location.getWorld().getPlayers().forEach(player -> players.put(player.getUniqueId(), new GamePlayer(player)));

        players.values().stream().map(GamePlayer::getPlayer).forEach(it -> {
            it.setGameMode(GameMode.ADVENTURE);
            it.teleport(location);
            it.sendMessage(ChatColor.GREEN + "Game starting...");
        });
    }

    @Override public void stop() {
        for (Player player : players.values().stream().map(GamePlayer::getPlayer).collect(Collectors.toList())) {
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(player.getMaxHealth());
            Helper.executeCommand("spawn " + player.getName());
        }
        this.players.clear();
        this.blockRegistry.getAll(blockState -> blockState.update(true));
        this.blockRegistry.getAll().clear();
    }

    public void end() {
        if (players.values().stream().filter(value -> value.getState() == GamePlayerState.ALIVE).count() <= 1) {
            for (GamePlayer gamePlayer : players.values()) {
                if (gamePlayer.getState() == GamePlayerState.ALIVE) {
                    Bukkit.broadcastMessage(ChatColor.GREEN + gamePlayer.getPlayer().getName() + " has won!");
                }
            }
            stop();
        }
    }

    @NotNull @Override public String getName() {
        return "TntRun";
    }

    public Map<UUID, GamePlayer> getPlayers() {
        return players;
    }
}
