package net.revelationmc.gamesigns.api;

public enum GameState {
    IN_GAME("In game"),
    WAITING("Waiting"),
    RESTARTING("Restarting");

    private final String name;

    GameState(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
