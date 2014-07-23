package com.joytank.net.game;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;

/**
 * Ping message
 */
@Immutable
public class PingMsg implements Serializable {
  private static final long serialVersionUID = 1186115716469776164L;

  private final int clientId;
  private final long timestamp;

  /**
   * Get the client ID that sent this Ping message
   * 
   * @return
   */
  public int getClientId() {
    return clientId;
  }

  /**
   * Get the time stamp when this Ping message is created
   * 
   * @return
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * Constructs a Ping message
   * 
   * @param clientId
   *          the ID of the client sending this Ping message
   */
  public PingMsg(int clientId) {
    this.clientId = clientId;
    this.timestamp = System.currentTimeMillis();
  }
}
