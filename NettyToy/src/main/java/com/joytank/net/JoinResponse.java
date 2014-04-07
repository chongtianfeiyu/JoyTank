package com.joytank.net;

import java.io.Serializable;

/**
 * 
 */
public final class JoinResponse implements Serializable {
	private static final long serialVersionUID = 8337151580366885976L;

	private final int cliendId;
	private final boolean isAccepted;

	public int getCliendId() {
		return cliendId;
	}

	public boolean isAccepted() {
		return isAccepted;
	}

	public JoinResponse(int cliendId, boolean isAccepted) {
		this.cliendId = cliendId;
		this.isAccepted = isAccepted;
	}
}
