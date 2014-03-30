package com.joytank.net;

import java.io.Serializable;
import java.net.SocketAddress;

import com.google.common.base.Preconditions;

/**
 * Ping message
 * 
 * @author lizhaoliu
 * 
 */
public class PingMsg implements Serializable {
  private static final long serialVersionUID = 1186115716469776164L;

  private final int id;
  private final SocketAddress remoteAddress;

  public PingMsg(int id, SocketAddress remoteAddress) {
    Preconditions.checkState(remoteAddress != null, "remoteAddress is unexpectedly null.");
    this.id = id;
    this.remoteAddress = remoteAddress;
  }

  public long getId() {
    return id;
  }

  public SocketAddress getRemoteAddress() {
    return remoteAddress;
  }

  @Override
  public String toString() {
    return "HeartBeat [id=" + id + ", remoteAddress=" + remoteAddress + "]";
  }
}
