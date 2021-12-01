package net.revelationmc.gamesigns.api;

import java.util.concurrent.CompletableFuture;

public interface GameSignsAPI {
    /**
     * Sets the game status.
     *
     * @param state - Current game state.
     * @deprecated This method provides no way to know when/if the game state was successfully changed.
     * The recommended method to use is {@link #setGameState(GameState)}.
     */
    @Deprecated
    void setState(GameState state);

    /**
     * Updates the game's current state.
     *
     * @param state - New game state.
     */
    CompletableFuture<Void> setGameState(GameState state);
}
