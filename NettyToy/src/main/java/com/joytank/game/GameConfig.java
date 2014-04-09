package com.joytank.game;

import org.codehaus.jackson.annotate.JsonProperty;

public class GameConfig {

	@JsonProperty("serverHost")
	private String serverHost;

	@JsonProperty("serverPort")
	private int serverPort;
	
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

	public String getServerHost() {
		return serverHost;
	}

	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}

	public int getSamples() {
		return samples;
	}

	public void setSamples(int samples) {
		this.samples = samples;
	}

	public boolean isVSync() {
		return isVSync;
	}

	public void setVSync(boolean isVSync) {
		this.isVSync = isVSync;
	}

	public boolean isFullscreen() {
		return isFullscreen;
	}

	public void setFullscreen(boolean isFullscreen) {
		this.isFullscreen = isFullscreen;
	}
}
