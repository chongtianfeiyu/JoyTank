package com.joytank.game;

import javax.annotation.concurrent.Immutable;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Game configuration
 */
@Immutable
public class GameConfig {

	@JsonProperty("lobbyHost")
	private String lobbyHost;

	@JsonProperty("lobbyPort")
	private int lobbyPort;

	@JsonProperty("screenWidth")
	private int screenWidth;

	@JsonProperty("screenHeight")
	private int screenHeight;

	@JsonProperty("samples")
	private int samples;

	@JsonProperty("isVSync")
	private boolean isVSync;

	@JsonProperty("isFullscreen")
	private boolean isFullscreen;

	public int getScreenWidth() {
		return screenWidth;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	public int getSamples() {
		return samples;
	}

	public boolean isVSync() {
		return isVSync;
	}

	public boolean isFullscreen() {
		return isFullscreen;
	}

	public String getLobbyHost() {
		return lobbyHost;
	}

	public int getLobbyPort() {
		return lobbyPort;
	}
}
