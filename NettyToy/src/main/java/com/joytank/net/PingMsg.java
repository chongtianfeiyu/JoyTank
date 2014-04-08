package com.joytank.net;

import java.io.Serializable;

/**
 * Ping message
 * 
 * @author lizhaoliu
 * 
 */
public class PingMsg implements Serializable {
	private static final long serialVersionUID = 1186115716469776164L;

	private final int pingId;
	private final int clientId;
	private final long timestamp;

	public int getPingId() {
		return pingId;
	}

	public int getClientId() {
		return clientId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public PingMsg(int pingId, int clientId, long timestamp) {
		super();
		this.pingId = pingId;
		this.clientId = clientId;
		this.timestamp = timestamp;
	}
}
