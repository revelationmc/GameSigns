package net.revelationmc.gamesigns.model.sign;

import org.bukkit.Location;
import org.bukkit.block.Sign;

public class GameSign {
    private final Location location;
    private final Sign bukkitSign;

    private String server;

    public GameSign(Location location) {
        this.location = location;
        this.bukkitSign = (Sign) location.getBlock().getState();
    }

    public void setServer(String server) {
        this.server = server;
    }

    public Location getLocation() {
        return this.location;
    }

    public String getServer() {
        return this.server;
    }

    public Sign getBukkitSign() {
        return this.bukkitSign;
    }
}
