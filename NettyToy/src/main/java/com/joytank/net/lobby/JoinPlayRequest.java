package com.joytank.net.lobby;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;

/**
 * 
 */
@Immutable
public class JoinPlayRequest implements Serializable {
  private static final long serialVersionUID = -5099927052650285022L;
  
  private final ServerDesc serverDesc;

	public JoinPlayRequest(ServerDesc serverDesc) {
	  super();
	  this.serverDesc = serverDesc;
  }

	public ServerDesc getServerDesc() {
		return serverDesc;
	}
}
