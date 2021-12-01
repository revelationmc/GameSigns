package net.revelationmc.gamesigns.model.server;

import net.revelationmc.gamesigns.api.GameState;

public class ServerData {
    private final String name;
    private final int maxPlayers;
    private final int onlinePlayers;
    private final GameState state;

    public ServerData(String name, int maxPlayers, int onlinePlayers, GameState state) {
        this.name = name;
        this.maxPlayers = maxPlayers;
        this.onlinePlayers = onlinePlayers;
        this.state = state;
    }

    public String getName() {
        return this.name;
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public int getOnlinePlayers() {
        return this.onlinePlayers;
    }

    public GameState getState() {
        return this.state;
    }
}
