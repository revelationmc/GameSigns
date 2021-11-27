package net.revelationmc.gamesigns.api;

public enum GameState {
    IN_GAME("In game"),
    WAITING("Waiting"),
    OFFLINE("Offline"),
    UNKNOWN("Unknown");

    private final String name;

    GameState(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
