package net.revelationmc.gamesigns.listeners;

import net.revelationmc.gamesigns.GameSignsPlugin;
import net.revelationmc.gamesigns.model.sign.GameSign;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.util.UUID;

public class BuildListener implements Listener {
    private final GameSignsPlugin plugin;

    public BuildListener(GameSignsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void on(SignChangeEvent event) {
        final String[] lines = event.getLines();

        if (lines[0].equalsIgnoreCase(this.plugin.getConfig().getString("sign-header"))) {
            final String server = lines[1];
            if (server.isBlank()) {
                event.setLine(0, "");
                event.setLine(1, ChatColor.RED + "You must provide");
                event.setLine(2, ChatColor.RED + "a server name!");
                event.setLine(3, "");
                return;
            }

            if (!this.plugin.getServersIds().contains(server)) {
                event.setLine(0, "");
                event.setLine(1, ChatColor.RED + "Server \"" + server + "\"");
                event.setLine(2, ChatColor.RED + "does not exist!");
                event.setLine(3, "");
                return;
            }

            if (this.plugin.getGameSignManager().getByLocation(event.getBlock().getLocation()).isPresent()) {
                return;
            }

            final GameSign sign = this.plugin.getGameSignManager().getOrCreate(UUID.randomUUID());
            sign.setLocation(event.getBlock().getLocation());
            sign.setServer(server);

            this.plugin.getServerSignConfiguration().set("signs." + sign.getUniqueId() + ".location", event.getBlock().getLocation());
            this.plugin.getServerSignConfiguration().set("signs." + sign.getUniqueId() + ".destination", server);
            this.plugin.saveServerSignConfiguration();

            event.setLine(0, "");
            event.setLine(1, ChatColor.GREEN + "Waiting for data...");
            event.setLine(2, ChatColor.GREEN + "Please wait.");
            event.setLine(3, "");

            event.getPlayer().sendMessage(ChatColor.GREEN + "Server sign successfully created!");
        }
    }

    @EventHandler
    public void on(BlockBreakEvent event) {
        final Location location = event.getBlock().getLocation();

        this.plugin.getGameSignManager().getByLocation(location).ifPresent(sign -> {
            this.plugin.getServerSignConfiguration().set("signs." + sign.getUniqueId(), null);
            this.plugin.saveServerSignConfiguration();
            this.plugin.getGameSignManager().unload(sign.getUniqueId());
        });
    }
}
