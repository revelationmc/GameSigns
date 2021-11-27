package net.revelationmc.gamesigns.listeners;

import net.revelationmc.gamesigns.GameSignsPlugin;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

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

            if (!this.plugin.serverExists(server)) {
                event.setLine(0, "");
                event.setLine(1, ChatColor.RED + "Server \"" + server + "\"");
                event.setLine(2, ChatColor.RED + "does not exist!");
                event.setLine(3, "");
                return;
            }

            this.plugin.getConfig().set("signs." + server, event.getBlock().getLocation());
            this.plugin.saveConfig();

            event.setLine(0, "");
            event.setLine(1, ChatColor.GREEN + "Waiting for data...");
            event.setLine(2, ChatColor.GREEN + "Please wait.");
            event.setLine(3, "");

            event.getPlayer().sendMessage(ChatColor.GREEN + "Server sign successfully created!");
        }
    }

    @EventHandler
    public void on(BlockBreakEvent event) {
        final Block block = event.getBlock();

        if (!(block.getState() instanceof Sign)) {
            System.out.println("Not a sign.");
            return;
        }

        final Sign sign = (Sign) block.getState();

        try {
            final String header = sign.getLine(0);
            if (header.equals(this.plugin.getConfig().getString("sign-header"))) {
                this.plugin.getConfig().set("signs." + sign.getLine(1), null);
                this.plugin.saveConfig();
            }
        } catch (IndexOutOfBoundsException ignored) {
        }
    }
}
