package net.revelationmc.gamesigns.messenger.implementation;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.revelationmc.gamesigns.GameSignsPlugin;
import net.revelationmc.gamesigns.api.GameState;
import net.revelationmc.gamesigns.messenger.Messenger;
import net.revelationmc.gamesigns.model.server.ServerData;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SqlMessenger implements Messenger {
    private final GameSignsPlugin plugin;

    private HikariDataSource dataSource;

    public SqlMessenger(GameSignsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        final HikariConfig config = new HikariConfig();

        config.setDriverClassName("net.savagedev.lib.mysql.jdbc.Driver");

        config.setJdbcUrl("jdbc:mysql://"
                + this.plugin.getConfig().getString("messenger.sql.host", "localhost")
                + ":" + this.plugin.getConfig().getInt("messenger.sql.port", 3306)
                + "/" + this.plugin.getConfig().getString("messenger.sql.database")
                + "?useSSL=false"
        );
        config.setUsername(this.plugin.getConfig().getString("messenger.sql.username"));
        config.setPassword(this.plugin.getConfig().getString("messenger.sql.password"));

        config.setPoolName("gamesigns-hikari");

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(TimeUnit.MINUTES.toMillis(30));

        this.dataSource = new HikariDataSource(config);

        this.setupTables();

        final long currentTime = System.currentTimeMillis();

        try (final Connection connection = this.dataSource.getConnection()) {
            try (final PreparedStatement statement =
                         connection.prepareStatement("INSERT INTO gamesign_data (added, server_id, max_players) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE added = ?, max_players = ?;")) {
                statement.setLong(1, currentTime);
                statement.setString(2, this.plugin.getConfig().getString("server-name"));
                statement.setInt(3, Bukkit.getMaxPlayers());
                statement.setLong(4, currentTime);
                statement.setInt(5, Bukkit.getMaxPlayers());

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupTables() {
        try (final Connection connection = this.dataSource.getConnection()) {
            try (final PreparedStatement statement =
                         connection.prepareStatement("CREATE TABLE IF NOT EXISTS gamesign_data(server_id VARCHAR(50) PRIMARY KEY NOT NULL, added BIGINT, game_state ENUM('IN_GAME', 'WAITING', 'RESTARTING'), current_players INT, max_players INT);")) {
                statement.executeUpdate();
            }
            /*try (final PreparedStatement statement =
                         connection.prepareStatement("ALTER TABLE gamesign_data ADD COLUMN added BIGINT FIRST;")) {
                statement.executeUpdate();
            }*/
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        try (final Connection connection = this.dataSource.getConnection()) {
            try (final PreparedStatement statement =
                         connection.prepareStatement("UPDATE gamesign_data SET game_state = ? WHERE server_id = ?;")) {
                statement.setString(1, GameState.RESTARTING.name());
                statement.setString(2, this.plugin.getConfig().getString("server-name"));
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.dataSource.close();
    }

    @Override
    public void sendGameState(GameState gameState) throws SQLException {
        try (final Connection connection = this.dataSource.getConnection()) {
            try (final PreparedStatement statement =
                         connection.prepareStatement("INSERT INTO gamesign_data (server_id, game_state) VALUES (?, ?) ON DUPLICATE KEY UPDATE game_state = ?;")) {
                statement.setString(1, this.plugin.getConfig().getString("server-name"));
                statement.setString(2, gameState.name());
                statement.setString(3, gameState.name());

                statement.executeUpdate();
            }
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
    public Set<String> getServersAddedAfter(long timeAdded) {
        final Set<String> servers = new HashSet<>();
        try (final Connection connection = this.dataSource.getConnection()) {
            try (final PreparedStatement statement =
                         connection.prepareStatement("SELECT server_id FROM gamesign_data WHERE  added > " + timeAdded + ";")) {
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

    @Override
    public Set<ServerData> getServers(Set<String> serverIds) {
        final Set<ServerData> servers = new HashSet<>();
        try (final Connection connection = this.dataSource.getConnection()) {
            try (final PreparedStatement statement =
                         connection.prepareStatement("SELECT * FROM gamesign_data WHERE server_id IN ('" + String.join("', '", serverIds) + "');")) {
                try (final ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        final String state = result.getString("game_state");
                        servers.add(new ServerData(
                                result.getString("server_id"),
                                result.getInt("max_players"),
                                result.getInt("current_players"),
                                state == null ? GameState.RESTARTING : GameState.valueOf(state)
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return servers;
    }

    @Override
    public Set<String> getAllServers() {
        final Set<String> servers = new HashSet<>();
        try (final Connection connection = this.dataSource.getConnection()) {
            try (final PreparedStatement statement =
                         connection.prepareStatement("SELECT server_id FROM gamesign_data;")) {
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
