package com.joytank.net;

import java.io.Serializable;

/**
 * A heart beat package
 */
public final class HeartBeat implements Serializable {
	private static final long serialVersionUID = 3684110247005568594L;

	private int clientId;

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}
}
