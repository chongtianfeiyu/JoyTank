package com.joytank.net;

import java.io.Serializable;

/**
 * Ping message
 * 
 * @author lizhaoliu
 * 
 */
public class PingMsg implements Serializable {
  private static final long serialVersionUID = 1186115716469776164L;

  private final int pingId;
  private final int clientId;

  public PingMsg(int pindId, int clientId) {
    this.pingId = pindId;
    this.clientId = clientId;
  }

  public long getPingId() {
    return pingId;
  }

  public int getClientId() {
    return clientId;
  }

}
