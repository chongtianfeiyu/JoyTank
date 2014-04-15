package com.joytank.net.lobby;

import java.io.Serializable;

public class RefreshResponse implements Serializable {
	private static final long serialVersionUID = 2404831007784064468L;

	private final ServerDesc[] serverDescs;

	public RefreshResponse(ServerDesc[] serverDescs) {
		super();
		this.serverDescs = serverDescs;
	}

	public ServerDesc[] getServerDescs() {
		return serverDescs;
	}
}
