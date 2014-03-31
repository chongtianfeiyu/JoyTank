package com.joytank.net;

import java.net.SocketAddress;

import com.google.common.base.Preconditions;

public final class ClientInfo {

  private final SocketAddress address;

  public SocketAddress getAddress() {
    return address;
  }

  private ClientInfo(Builder builder) {
    this.address = builder.address;
  }

  public static class Builder {

    private SocketAddress address;

    public Builder withAddress(SocketAddress address) {
      this.address = address;
      return this;
    }

    public ClientInfo build() {
      validate();
      return new ClientInfo(this);
    }

    private void validate() {
      Preconditions.checkNotNull(address, "address may not be null");
    }
  }
}
