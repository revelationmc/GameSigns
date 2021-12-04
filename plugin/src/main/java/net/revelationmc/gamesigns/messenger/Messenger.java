package net.revelationmc.gamesigns.messenger;

import net.revelationmc.gamesigns.api.GameState;
import net.revelationmc.gamesigns.model.server.ServerData;

import java.sql.SQLException;
import java.util.Set;

public interface Messenger {
    void init();

    void shutdown();

    void sendGameState(GameState gameState) throws SQLException;

    void sendPlayerCount(int playerCount);

    Set<String> getServersAddedAfter(long timeAdded);

    Set<ServerData> getServers(Set<String> serverIds);

    Set<String> getAllServers();
}
