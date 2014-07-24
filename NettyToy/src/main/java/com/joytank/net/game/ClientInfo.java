package com.joytank.net.game;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 */
public final class ClientInfo {

  private static final Set<Integer> portSet = Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());

  private final SocketAddress clientAddress;

  private final SocketAddress localAddressForClient;

  private long timeStamp;

  public SocketAddress getClientAddress() {
    return clientAddress;
  }

  public ClientInfo(SocketAddress clientAddress, long timeStamp) {
    super();
    this.clientAddress = clientAddress;
    int port = Utils.generateRandomPort();
    while (portSet.contains(port)) {
      port = Utils.generateRandomPort();
    }
    portSet.add(port);
    this.localAddressForClient = new InetSocketAddress(port);
    this.timeStamp = timeStamp;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
  }

  /**
   * Get the dedicated local address to communicate with this particular client
   * 
   * @return
   */
  public SocketAddress getLocalAddressForClient() {
    return localAddressForClient;
  }
}
