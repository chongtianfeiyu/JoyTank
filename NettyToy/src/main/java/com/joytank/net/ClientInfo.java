package com.joytank.net;

import java.net.SocketAddress;

/**
 * 
 */
public final class ClientInfo {

	private final SocketAddress clientAddress;
	
	private long timeStamp;

	public SocketAddress getClientAddress() {
		return clientAddress;
	}

	public ClientInfo(SocketAddress clientAddress, long timeStamp) {
		super();
		this.clientAddress = clientAddress;
		this.timeStamp = timeStamp;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
}
