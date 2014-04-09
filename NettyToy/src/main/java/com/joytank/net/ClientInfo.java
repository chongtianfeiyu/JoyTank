package com.joytank.net;

import java.net.SocketAddress;

/**
 * 
 */
public final class ClientInfo {

	private final SocketAddress clientAddress;

	public SocketAddress getClientAddress() {
		return clientAddress;
	}

	public ClientInfo(SocketAddress clientAddress) {
		super();
		this.clientAddress = clientAddress;
	}
}
