package com.joytank.net;

import java.io.Serializable;
import java.net.SocketAddress;

import com.google.common.base.Preconditions;

public final class JoinRequest implements Serializable {
	private static final long serialVersionUID = -6503804384688527303L;

	private final SocketAddress address;

	public SocketAddress getAddress() {
		return address;
	}

	public JoinRequest(SocketAddress address) {
		Preconditions.checkState(address != null);

		this.address = address;
	}

}
