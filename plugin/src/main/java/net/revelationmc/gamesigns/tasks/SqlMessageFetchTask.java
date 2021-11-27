package net.revelationmc.gamesigns.tasks;

import net.revelationmc.gamesigns.GameSignsPlugin;
import net.revelationmc.gamesigns.model.ServerData;
import org.bukkit.configuration.ConfigurationSection;

import java.util.function.Consumer;

public class SqlMessageFetchTask implements Runnable {
    private final Consumer<ServerData> consumer;
    private final GameSignsPlugin plugin;

    public SqlMessageFetchTask(Consumer<ServerData> consumer, GameSignsPlugin plugin) {
        this.consumer = consumer;
        this.plugin = plugin;
    }


    @Override
    public void run() {
        final ConfigurationSection section = this.plugin.getConfig().getConfigurationSection("signs");

        if (section == null) {
            return;
        }

        for (String server : section.getKeys(false)) {
            this.plugin.getServer().getScheduler().runTask(this.plugin, () ->
                    this.consumer.accept(this.plugin.getMessenger().getServerData(server))
            );
        }
    }
}
