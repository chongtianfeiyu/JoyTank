package com.joytank.net.lobby;

import java.io.Serializable;
import java.net.SocketAddress;

public class JoinPlayResponse implements Serializable {
	private static final long serialVersionUID = 9210526166769064200L;

	private final SocketAddress clientPublicAddress;

	private final boolean isAccepted;

	public JoinPlayResponse(SocketAddress clientPublicAddress, boolean isAccepted) {
		super();
		this.clientPublicAddress = clientPublicAddress;
		this.isAccepted = isAccepted;
	}

	public SocketAddress getClientPublicAddress() {
		return clientPublicAddress;
	}

	public boolean isAccepted() {
		return isAccepted;
	}

}
