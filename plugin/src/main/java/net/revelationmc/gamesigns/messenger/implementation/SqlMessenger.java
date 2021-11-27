package net.revelationmc.gamesigns.messenger.implementation;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.revelationmc.gamesigns.GameSignsPlugin;
import net.revelationmc.gamesigns.api.GameState;
import net.revelationmc.gamesigns.messenger.MessageConsumer;
import net.revelationmc.gamesigns.messenger.Messenger;
import net.revelationmc.gamesigns.model.ServerData;
import net.revelationmc.gamesigns.tasks.SqlMessageFetchTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SqlMessenger implements Messenger {
    private final GameSignsPlugin plugin;

    private SqlMessageFetchTask housekeeper;
    private HikariDataSource dataSource;

    public SqlMessenger(GameSignsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        final HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:mysql://"
                + this.plugin.getConfig().getString("messenger.sql.host")
                + ":" + this.plugin.getConfig().getString("messenger.sql.port")
                + "/" + this.plugin.getConfig().getString("messenger.sql.database")
        );
        config.setUsername(this.plugin.getConfig().getString("messenger.sql.username"));
        config.setPassword(this.plugin.getConfig().getString("messenger.sql.password"));

        config.setPoolName("gamesigns-hikari");

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(TimeUnit.MINUTES.toMillis(30));

        this.dataSource = new HikariDataSource(config);

        this.setupTables();

        this.housekeeper = new SqlMessageFetchTask(new MessageConsumer(this.plugin), this.plugin);
        this.plugin.getServer().getScheduler()
                .runTaskTimerAsynchronously(this.plugin, this.housekeeper, 0L, 10L);
    }

    private void setupTables() {
        try (final Connection connection = this.dataSource.getConnection()) {
            try (final PreparedStatement statement =
                         connection.prepareStatement("CREATE TABLE IF NOT EXISTS gamesign_data(server_id VARCHAR(50) PRIMARY KEY NOT NULL, game_state ENUM('IN_GAME', 'WAITING', 'OFFLINE'), current_players INT, max_players INT);")) {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        this.dataSource.close();
    }

    @Override
    public void register(String name, int maxPlayers) {
        try (final Connection connection = this.dataSource.getConnection()) {
            try (final PreparedStatement statement =
                         connection.prepareStatement("INSERT INTO gamesign_data (server_id, max_players) VALUES (?, ?) ON DUPLICATE KEY UPDATE max_players = ?;")) {
                statement.setString(1, this.plugin.getConfig().getString("server-name"));
                statement.setInt(2, maxPlayers);
                statement.setInt(3, maxPlayers);

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendGameState(GameState gameState) {
        try (final Connection connection = this.dataSource.getConnection()) {
            try (final PreparedStatement statement =
                         connection.prepareStatement("INSERT INTO gamesign_data (server_id, game_state) VALUES (?, ?) ON DUPLICATE KEY UPDATE game_state = ?;")) {
                statement.setString(1, this.plugin.getConfig().getString("server-name"));
                statement.setString(2, gameState.name());
                statement.setString(3, gameState.name());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendPlayerCount(int playerCount) {
        try (final Connection connection = this.dataSource.getConnection()) {
            try (final PreparedStatement statement =
                         connection.prepareStatement("INSERT INTO gamesign_data (server_id, current_players) VALUES (?, ?) ON DUPLICATE KEY UPDATE current_players = ?;")) {
                statement.setString(1, this.plugin.getConfig().getString("server-name"));
                statement.setInt(2, playerCount);
                statement.setInt(3, playerCount);

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ServerData getServerData(String server) {
        try (final Connection connection = this.dataSource.getConnection()) {
            try (final PreparedStatement statement = connection.prepareStatement("SELECT * FROM gamesign_data WHERE server_id = ?;")) {
                statement.setString(1, server);
                try (final ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        return null;
                    }

                    final String state = result.getString("game_state");

                    return new ServerData(
                            result.getString("server_id"),
                            result.getInt("max_players"),
                            result.getInt("current_players"),
                            state == null ? GameState.UNKNOWN : GameState.valueOf(state)
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Set<String> getAllServers() {
        final Set<String> servers = new HashSet<>();
        try (final Connection connection = this.dataSource.getConnection()) {
            try (final PreparedStatement statement = connection.prepareStatement("SELECT server_id FROM gamesign_data;")) {
                try (final ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        servers.add(result.getString("server_id"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return servers;
    }
}
