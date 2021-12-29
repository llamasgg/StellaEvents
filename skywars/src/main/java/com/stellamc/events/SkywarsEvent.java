package com.stellamc.events;

import com.google.common.collect.ImmutableSet;
import com.stella.events.BlockRegistry;
import com.stella.events.Event;
import com.stella.events.SimpleBlockRegistry;
import com.stellamc.events.player.GamePlayer;
import me.lucko.helper.Events;
import me.lucko.helper.Helper;
import me.lucko.helper.Schedulers;
import me.lucko.helper.bucket.Cycle;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Item;
import me.lucko.helper.random.RandomSelector;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SkywarsEvent implements Event {

    private final Map<UUID, GamePlayer> alive = new HashMap<>();
    private final Set<GamePlayer> dead = new HashSet<>();
    private final Cycle<Location> locations;
    private final List<Location> chests;
    private final Set<ItemStack> items = ImmutableSet.of(ItemStackBuilder.of(Material.DIAMOND_SWORD).build());
    private final RandomSelector<ItemStack> selector = RandomSelector.uniform(items);
    private final BlockRegistry blockRegistry;

    private GameState state;

    public SkywarsEvent(List<Location> locations, List<Location> chests) {
        this.locations = Cycle.of(locations);
        this.chests = chests;
        this.state = GameState.WAITING;
        this.blockRegistry = new SimpleBlockRegistry();

        Events.subscribe(BlockPlaceEvent.class).handler(event -> {
            blockRegistry.register(event.getBlock().getState());
        });

        Events.subscribe(BlockBreakEvent.class).handler(event -> {
            blockRegistry.register(event.getBlock().getState());;
        });

        Events.subscribe(PlayerQuitEvent.class).handler(event -> {
            alive.remove(event.getPlayer().getUniqueId());
            dead.removeIf(gamePlayer -> gamePlayer.getPlayer().getUniqueId().equals(event.getPlayer().getUniqueId()));
            if (state == GameState.ACTIVE) {
                end();
            }
        });


        Events.subscribe(EntityDamageEvent.class).filter($ -> state == GameState.WAITING).handler(event -> {
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
            }
        });

        Events.subscribe(PlayerDeathEvent.class).handler(event -> {
            if (state != GameState.ACTIVE) return;
            Player player = event.getEntity();
            dead.add(alive.remove(player.getUniqueId()));

            player.setGameMode(GameMode.SPECTATOR);

            if (player.getKiller() != null) {
                Player killer = player.getKiller();
                if (alive.containsKey(killer.getUniqueId())) {
                    alive.get(killer.getUniqueId()).addKill();
                }

                end();
            }


            event.setDeathMessage(ChatColor.RED  + player.getName() + " has died to " + (player.getKiller() == null ? "Void" : player.getKiller().getName()));
        });
    }

    @Override public void start() {
        state = GameState.ACTIVE;
        Bukkit.getWorld("world").getPlayers().forEach(player -> alive.put(player.getUniqueId(), new GamePlayer(player)));

        if (alive.size() > locations.getBacking().size()) {
            throw new IndexOutOfBoundsException("Not enough locations for players to spawn!");
        }

        if (!chests.isEmpty()) {
            chests.forEach(location -> {
                Block block = location.getBlock();
                BlockState state = block.getState();
                if (state instanceof Chest) {
                    Inventory inventory = ((Chest) state).getInventory();
                    inventory.clear();
                    for (int i = 0; i < 5; i++) {
                        inventory.addItem(selector.pick());
                    }
                }
            });
        }

        alive.values().stream().map(GamePlayer::getPlayer).forEach(it -> {
            it.setGameMode(GameMode.ADVENTURE);
            it.teleport(locations.next());
            it.sendMessage(ChatColor.GREEN + "Game starting...");
        });

        Schedulers.sync().runLater(() -> {
            alive.values().stream().map(GamePlayer::getPlayer).forEach(it -> {
                it.setGameMode(GameMode.SURVIVAL);
                it.sendMessage(ChatColor.GREEN + "Game started!");
                handleRest();
            });
        }, 10, TimeUnit.SECONDS);
    }

    public void handleRest() {
        Location endWorld = new Location(Bukkit.getWorld("world"),1,1,1);
        Schedulers.sync().runLater(() -> {
            alive.values().stream().map(GamePlayer::getPlayer).forEach(it -> {
                it.teleport(endWorld);
                it.sendMessage(ChatColor.RED + "Sudden death!");
            });
        }, 10, TimeUnit.MINUTES);
    }

    public void end() {
        if (alive.size() <= 1) {
            for (GamePlayer gamePlayer : alive.values()) {
                Bukkit.broadcastMessage(ChatColor.GREEN + gamePlayer.getPlayer().getName() + " has won!");
            }

            stop();
        }
    }

    @Override public void stop() {
        state = GameState.WAITING;
        Bukkit.broadcastMessage(ChatColor.RED + "Game over");
        for (Player player : alive.values().stream().map(GamePlayer::getPlayer).collect(Collectors.toList())) {
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(player.getMaxHealth());
            Helper.executeCommand("spawn " + player.getName());
        }

        for (Player player : dead.stream().map(GamePlayer::getPlayer).collect(Collectors.toList())) {
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(player.getMaxHealth());
            Helper.executeCommand("spawn " + player.getName());
        }
        this.alive.clear();
        this.dead.clear();
        this.blockRegistry.getAll(blockState -> blockState.update(true));
        this.blockRegistry.getAll().clear();
    }

    @NotNull @Override public String getName() {
        return "Skywars";
    }

    private enum GameState {
        ACTIVE,
        WAITING
    }
}
