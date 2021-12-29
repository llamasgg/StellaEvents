package com.stella.events;

import com.stellamc.events.SkywarsEvent;
import me.lucko.helper.Commands;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Arrays;

public final class EventPlugin extends ExtendedJavaPlugin {

    @Override protected void enable() {
        Events.create("tntrun", new TNTEvent(new Location(Bukkit.getWorld("world"),1,100,1)));
        Events.create("skywars", new SkywarsEvent(Arrays.asList(new Location(Bukkit.getWorld("world"),1,100,1), new Location(Bukkit.getWorld("world"),-1,100,1)), Arrays.asList(new Location(Bukkit.getWorld("world"),1,100,1), new Location(Bukkit.getWorld("world"),-1,100,1))));
        Commands.create().assertOp().assertUsage("<eventName>").handler(command -> Events.getEventRegistry().find(command.arg(0).parseOrFail(String.class)).ifPresent(Event::start)).registerAndBind(this, "startevent");
        Commands.create().assertPlayer().handler(command -> Events.getEventRegistry().getAll().forEach(event -> command.reply(event.getName()))).registerAndBind(this, "events");
    }

}
