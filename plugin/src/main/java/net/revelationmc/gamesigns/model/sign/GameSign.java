package net.revelationmc.gamesigns.model.sign;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import java.util.Objects;
import java.util.UUID;

public class GameSign {
    private final UUID uniqueId;

    private Sign bukkitSign;
    private Location location;
    private String server;

    public GameSign(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public void setLocation(Location location) {
        final Block block = Objects.requireNonNull(location.getWorld()).getBlockAt(location);
        if (!(block.getState() instanceof Sign)) {
            throw new IllegalStateException();
        }
        this.bukkitSign = (Sign) block.getState();
        this.location = location;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public Sign getBukkitSign() {
        return this.bukkitSign;
    }

    public Location getLocation() {
        return this.location;
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public String getServer() {
        return this.server;
    }
}
