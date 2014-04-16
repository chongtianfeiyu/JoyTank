package com.joytank.net.lobby;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;

/**
 * 
 */
@Immutable
public class JoinPlayResponse implements Serializable {
	private static final long serialVersionUID = 9210526166769064200L;

	private final boolean isAccepted;

	private final ServerDesc serverDesc;

	public boolean isAccepted() {
		return isAccepted;
	}

	public JoinPlayResponse(boolean isAccepted, ServerDesc serverDesc) {
		super();
		this.isAccepted = isAccepted;
		this.serverDesc = serverDesc;
	}

	public ServerDesc getServerDesc() {
		return serverDesc;
	}

}
