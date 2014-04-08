package com.joytank.net;

import java.io.Serializable;
import java.util.Collection;

/**
 * 
 */
public final class JoinResponse implements Serializable {
	private static final long serialVersionUID = 8337151580366885976L;

	private final int cliendId;
	private final boolean isAccepted;
	private final Collection<Integer> allClients;

	public Collection<Integer> getAllClients() {
		return allClients;
	}

	public int getCliendId() {
		return cliendId;
	}

	public boolean isAccepted() {
		return isAccepted;
	}

	public JoinResponse(int cliendId, boolean isAccepted, Collection<Integer> allClients) {
	  super();
	  this.cliendId = cliendId;
	  this.isAccepted = isAccepted;
	  this.allClients = allClients;
  }

}
