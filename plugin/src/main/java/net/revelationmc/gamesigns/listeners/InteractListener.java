package net.revelationmc.gamesigns.listeners;

import net.revelationmc.gamesigns.GameSignsPlugin;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class InteractListener implements Listener {
    private final GameSignsPlugin plugin;

    public InteractListener(GameSignsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void on(PlayerInteractEvent event) {
        final Block block = event.getClickedBlock();

        if (block == null || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        final Location location = block.getLocation();

        this.plugin.getGameSignManager().getByLocation(location)
                .ifPresent(sign -> this.send(event.getPlayer(), sign.getServer()));
    }

    private void send(Player player, String server) {
        try (final ByteArrayOutputStream byteOutput = new ByteArrayOutputStream()) {
            try (final DataOutputStream dataOutput = new DataOutputStream(byteOutput)) {
                dataOutput.writeUTF("Connect");
                dataOutput.writeUTF(server);
                player.sendPluginMessage(this.plugin, "BungeeCord", byteOutput.toByteArray());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
