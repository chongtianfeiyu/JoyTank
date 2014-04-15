package com.joytank.net.lobby;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * 
 */
public class ServerDesc implements Serializable {
	private static final long serialVersionUID = -1408829863865202868L;

	@JsonProperty("serverName")
	private String serverName;

	@JsonProperty("serverHost")
	private String serverHost;

	@JsonProperty("serverPort")
	private int serverPort;

	public String getServerName() {
		return serverName;
	}

	public String getServerHost() {
		return serverHost;
	}

	public int getServerPort() {
		return serverPort;
	}
}
