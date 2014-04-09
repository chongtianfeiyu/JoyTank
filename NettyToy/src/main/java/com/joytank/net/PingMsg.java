package com.joytank.net;

import java.io.Serializable;

/**
 * Ping message
 */
public class PingMsg implements Serializable {
	private static final long serialVersionUID = 1186115716469776164L;

	private final int clientId;
	private final long timestamp;

	public int getClientId() {
		return clientId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * 
	 * @param clientId
	 * @param timestamp
	 */
	public PingMsg(int clientId, long timestamp) {
		this.clientId = clientId;
		this.timestamp = timestamp;
	}
}
