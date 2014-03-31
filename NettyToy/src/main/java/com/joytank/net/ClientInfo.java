package com.joytank.net;

import java.net.SocketAddress;

import com.joytank.game.ActorStatus;
import com.google.common.base.Preconditions;

public final class ClientInfo {

  private final SocketAddress address;
  private final ActorStatus playerEssence;

  public SocketAddress getAddress() {
    return address;
  }

  public ActorStatus getPlayerEssence() {
    return playerEssence;
  }

  private ClientInfo(Builder builder) {
    this.address = builder.address;
    this.playerEssence = builder.playerEssence;
  }

  public static class Builder {

    private SocketAddress address;
    private ActorStatus playerEssence;

    public Builder withAddress(SocketAddress address) {
      this.address = address;
      return this;
    }

    public Builder withPlayerEssence(ActorStatus playerEssence) {
      this.playerEssence = playerEssence;
      return this;
    }

    public ClientInfo build() {
      validate();
      return new ClientInfo(this);
    }

    private void validate() {
      Preconditions.checkNotNull(address, "address may not be null");
      Preconditions.checkNotNull(playerEssence, "playerEssence may not be null");
    }
  }
}
