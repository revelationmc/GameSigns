package net.revelationmc.gamesigns;

import com.google.common.collect.Sets;
import net.revelationmc.gamesigns.api.GameSignsAPI;
import net.revelationmc.gamesigns.api.GameState;
import net.revelationmc.gamesigns.listeners.BuildListener;
import net.revelationmc.gamesigns.listeners.ConnectionListener;
import net.revelationmc.gamesigns.listeners.InteractListener;
import net.revelationmc.gamesigns.messenger.MessageConsumer;
import net.revelationmc.gamesigns.messenger.Messenger;
import net.revelationmc.gamesigns.messenger.implementation.SqlMessenger;
import net.revelationmc.gamesigns.model.sign.GameSignManager;
import net.revelationmc.gamesigns.tasks.ServerDataFetchTask;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class GameSignsPlugin extends JavaPlugin implements GameSignsAPI {
    private final Set<String> serversIds = Sets.newConcurrentHashSet();

    private FileConfiguration serverSignConfiguration;
    private Path serverSignConfigPath;

    private GameSignManager gameSignManager;
    private Messenger messenger;

    @Override
    public void onEnable() {
        this.initConfig();
        this.initMessenger();
        this.initManagers();
        this.initTasks();
        this.initListeners();

        // Register the API last to make sure all required components have successfully initialized.
        this.getServer().getServicesManager()
                .register(GameSignsAPI.class, this, this, ServicePriority.High);
    }

    @Override
    public void onDisable() {
        // Unregister the API first, before shutting down required components.
        this.getServer().getServicesManager()
                .unregister(GameSignsAPI.class);

        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
        this.messenger.shutdown();
    }

    @Override
    public void setState(GameState state) {
        this.getServer().getScheduler().runTaskAsynchronously(this, () -> {
            try {
                this.messenger.sendGameState(state);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture<Void> setGameState(GameState state) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        this.getServer().getScheduler().runTaskAsynchronously(this, () -> {
            try {
                this.messenger.sendGameState(state);
                future.complete(null);
            } catch (SQLException e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    private void initConfig() {
        this.saveDefaultConfig();

        this.serverSignConfigPath = this.getDataFolder().toPath().resolve("signs.yml");
        if (Files.notExists(this.serverSignConfigPath)) {
            try {
                Files.createFile(this.serverSignConfigPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (final BufferedReader reader = Files.newBufferedReader(this.serverSignConfigPath)) {
            this.serverSignConfiguration = YamlConfiguration.loadConfiguration(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initMessenger() {
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        this.messenger = new SqlMessenger(this);
        this.messenger.init();
        try {
            this.messenger.sendGameState(GameState.WAITING);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.serversIds.add(this.getConfig().getString("server-name"));
    }

    private void initManagers() {
        this.gameSignManager = new GameSignManager(this);
        this.gameSignManager.loadAllSigns();
    }

    private void initTasks() {
        // Schedule the server data fetcher task.
        this.getServer().getScheduler()
                .runTaskTimerAsynchronously(this, new ServerDataFetchTask(this,
                        new MessageConsumer(this)), 0L, 10L);
    }

    private void initListeners() {
        final PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new BuildListener(this), this);
        pluginManager.registerEvents(new ConnectionListener(this), this);
        pluginManager.registerEvents(new InteractListener(this), this);
    }

    public void saveServerSignConfiguration() {
        try {
            this.serverSignConfiguration.save(this.serverSignConfigPath.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Set<String> getServersIds() {
        return this.serversIds;
    }

    public FileConfiguration getServerSignConfiguration() {
        return this.serverSignConfiguration;
    }

    public GameSignManager getGameSignManager() {
        return this.gameSignManager;
    }

    public Messenger getMessenger() {
        return this.messenger;
    }
}
