package com.joytank.net.game;

import java.io.Serializable;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.joytank.game.GameState;

/**
 * 
 */
public final class JoinResponse implements Serializable {
	private static final long serialVersionUID = 8337151580366885976L;

	private final int cliendId;
	private final GameState gameState;

	/**
	 * 
	 * @return
	 */
	public int getCliendId() {
		return cliendId;
	}

	/**
	 * Get the game state object
	 * 
	 * @return
	 */
	@Nonnull
	public GameState getGameState() {
		return gameState;
	}

	/**
	 * Construct a join response
	 * 
	 * @param cliendId server generated unique ID
	 * @param gameState {@link Nonnull} a representation of the current game state
	 */
	public JoinResponse(int cliendId, GameState gameState) {
		super();
		Preconditions.checkNotNull(gameState);
		
		this.cliendId = cliendId;
		this.gameState = gameState;
	}
}
