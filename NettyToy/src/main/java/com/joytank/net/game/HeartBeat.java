package com.joytank.net.game;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;

/**
 * A heart beat packet
 */
@Immutable
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
