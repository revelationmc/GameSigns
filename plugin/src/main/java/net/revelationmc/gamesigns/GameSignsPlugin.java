package net.revelationmc.gamesigns;

import net.revelationmc.gamesigns.api.GameSignsAPI;
import net.revelationmc.gamesigns.api.GameState;
import net.revelationmc.gamesigns.listeners.BuildListener;
import net.revelationmc.gamesigns.listeners.ConnectionListener;
import net.revelationmc.gamesigns.messenger.Messenger;
import net.revelationmc.gamesigns.messenger.implementation.SqlMessenger;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class GameSignsPlugin extends JavaPlugin implements GameSignsAPI {
    private Set<String> servers = new HashSet<>();

    private Messenger messenger;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getServer().getPluginManager()
                .registerEvents(new BuildListener(this), this);
        this.getServer().getPluginManager()
                .registerEvents(new ConnectionListener(this), this);

        this.messenger = new SqlMessenger(this);
        this.messenger.init();

        this.messenger.register(this.getConfig().getString("server-name"), this.getServer().getMaxPlayers());
        this.setState(GameState.WAITING);

        this.getServer().getScheduler().runTaskTimerAsynchronously(this, () ->
                        this.servers = this.messenger.getAllServers()
                , 0L, 5L);

        this.getServer().getServicesManager()
                .register(GameSignsAPI.class, this, this, ServicePriority.High);
    }

    @Override
    public void onDisable() {
        this.getServer().getServicesManager()
                .unregister(GameSignsAPI.class);
        this.setState(GameState.OFFLINE);
        this.messenger.shutdown();
    }

    @Override
    public void setState(GameState state) {
        this.getServer().getScheduler().runTaskAsynchronously(this, () ->
                this.messenger.sendGameState(state)
        );
    }

    public boolean serverExists(String name) {
        return this.servers.contains(name);
    }

    public Messenger getMessenger() {
        return this.messenger;
    }
}
