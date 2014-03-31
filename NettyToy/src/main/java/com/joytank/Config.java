package com.joytank;

import org.codehaus.jackson.annotate.JsonProperty;

public class Config {

	@JsonProperty("server_host")
	private String serverHost;

	@JsonProperty("server_port")
	private int serverPort;

	public String getServerHost() {
		return serverHost;
	}

	public int getServerPort() {
		return serverPort;
	}
}
