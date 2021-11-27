package net.revelationmc.gamesigns.api;

public interface GameSignsAPI {
    /**
     * Sets the game status.
     *
     * @param state - Current game state.
     */
    void setState(GameState state);
}
