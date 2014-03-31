package com.joytank.net;

import java.io.Serializable;
import java.net.SocketAddress;

import com.google.common.base.Preconditions;

public final class HelloMsg implements Serializable {
  private static final long serialVersionUID = -6503804384688527303L;
  
  private final int clientId;
  private final SocketAddress address;

  public SocketAddress getAddress() {
    return address;
  }

  public int getClientId() {
    return clientId;
  }

  public HelloMsg(int clientId, SocketAddress address) {
    Preconditions.checkState(address != null);

    this.clientId = clientId;
    this.address = address;
  }

}
