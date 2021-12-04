package net.revelationmc.gamesigns.tasks;

import net.revelationmc.gamesigns.GameSignsPlugin;
import net.revelationmc.gamesigns.messenger.MessageConsumer;
import net.revelationmc.gamesigns.model.server.ServerData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Set;
import java.util.logging.Level;

/**
 * This task is responsible for fetching up-to-date
 * server information used for updating the game signs.
 */
public class ServerDataFetchTask implements Runnable {
    private final GameSignsPlugin plugin;
    private final MessageConsumer consumer;

    private long mostRecentTimestamp = System.currentTimeMillis();

    public ServerDataFetchTask(GameSignsPlugin plugin, MessageConsumer consumer) {
        this.plugin = plugin;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        Set<String> newServers;
        if (this.plugin.getServersIds().size() == 1) {
            newServers = this.plugin.getMessenger().getAllServers();
        } else {
            newServers = this.plugin.getMessenger().getServersAddedAfter(this.mostRecentTimestamp);
        }

        if (!newServers.isEmpty()) {
            final int newServerCount = newServers.size();
            this.plugin.getLogger().log(Level.INFO, "Discovered " + newServerCount + " new server" + (newServerCount == 1 ? '.' : "s."));
            this.mostRecentTimestamp = System.currentTimeMillis();
            this.plugin.getServersIds().addAll(newServers);
        }

        if (Bukkit.getOnlinePlayers().isEmpty()) {
            return;
        }

        final ConfigurationSection section = this.plugin.getServerSignConfiguration()
                .getConfigurationSection("signs");

        if (section == null) {
            return;
        }

        final Set<ServerData> servers = this.plugin.getMessenger().getServers(section.getKeys(false));
        this.plugin.getServer().getScheduler().runTask(this.plugin, new LocalMessageDispatcher(servers));
    }

    private class LocalMessageDispatcher implements Runnable {
        private final Set<ServerData> servers;

        public LocalMessageDispatcher(Set<ServerData> servers) {
            this.servers = servers;
        }

        @Override
        public void run() {
            for (ServerData data : this.servers) {
                ServerDataFetchTask.this.consumer.accept(data);
            }
        }
    }
}
