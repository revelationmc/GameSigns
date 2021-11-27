package net.revelationmc.gamesigns.messenger;

import net.revelationmc.gamesigns.api.GameState;
import net.revelationmc.gamesigns.model.ServerData;

import java.util.Set;

public interface Messenger {
    void init();

    void shutdown();

    void register(String name, int maxPlayers);

    void sendGameState(GameState gameState);

    void sendPlayerCount(int playerCount);

    ServerData getServerData(String server);

    Set<String> getAllServers();
}
