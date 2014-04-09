package com.joytank.game;

import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;

/**
 * Game state that needs to be broadcasted from server to clients
 */
public final class GameState implements Serializable {
  private static final long serialVersionUID = -1783867358295954256L;

  private final ConcurrentMap<Integer, PlayerState> playerStateMap = Maps.newConcurrentMap();

	public ConcurrentMap<Integer, PlayerState> getPlayerStateMap() {
		return playerStateMap;
	}
}
