package net.revelationmc.gamesigns.listeners;

import net.revelationmc.gamesigns.GameSignsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener {
    private final GameSignsPlugin plugin;

    public ConnectionListener(GameSignsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () ->
                this.plugin.getMessenger().sendPlayerCount(this.plugin.getServer().getOnlinePlayers().size())
        );
    }

    @EventHandler
    public void on(PlayerQuitEvent event) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () ->
                this.plugin.getMessenger().sendPlayerCount(this.plugin.getServer().getOnlinePlayers().size())
        );
    }
}
