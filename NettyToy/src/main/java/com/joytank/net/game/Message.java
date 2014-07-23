package com.joytank.net.game;

import java.net.SocketAddress;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.Preconditions;

/**
 * A wrapper of a general message
 */
@Immutable
public class Message {
  private final Object messageObject;

  private final SocketAddress remoteAddress;

  /**
   * Constructs a {@link Message}
   * 
   * @param messageObject
   *          {@link Nonnull} the raw message object
   * @param remoteAddress
   *          {@link Nonnull} the remote address which sent this message
   */
  public Message(@Nonnull Object messageObject, @Nonnull SocketAddress remoteAddress) {
    Preconditions.checkNotNull(messageObject);
    Preconditions.checkNotNull(remoteAddress);

    this.messageObject = messageObject;
    this.remoteAddress = remoteAddress;
  }

  public Object getMessageObject() {
    return messageObject;
  }

  public SocketAddress getRemoteAddress() {
    return remoteAddress;
  }
}
