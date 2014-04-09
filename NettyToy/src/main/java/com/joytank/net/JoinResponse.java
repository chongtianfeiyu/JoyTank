package com.joytank.net;

import java.io.Serializable;

import com.joytank.game.GameState;

/**
 * 
 */
public final class JoinResponse implements Serializable {
	private static final long serialVersionUID = 8337151580366885976L;

	private final int cliendId;
	private final GameState gameState;

	public int getCliendId() {
		return cliendId;
	}

	public GameState getGameState() {
		return gameState;
	}

	public JoinResponse(int cliendId, GameState gameState) {
		super();
		this.cliendId = cliendId;
		this.gameState = gameState;
	}
}
