package com.joytank.net.game;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;

/**
 * Ping message, which serves as a measure of network transmission speed and heart beat for disconnection detection
 * purpose
 */
@Immutable
public class Ping implements Serializable {
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
  public Ping(int clientId) {
    this.clientId = clientId;
    this.timestamp = System.currentTimeMillis();
  }
}
