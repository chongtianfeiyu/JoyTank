package com.joytank.game;

import java.awt.Point;
import java.io.Serializable;
import com.google.common.base.Preconditions;

public class PlayerMotion implements Serializable {
  private static final long serialVersionUID = 2196791557505482846L;

  private final int clientId;
  private final Point src;
  private final Point dst;

  public int getClientId() {
    return clientId;
  }

  public Point getSrc() {
    return src;
  }

  public Point getDst() {
    return dst;
  }

  private PlayerMotion(Builder builder) {
    this.clientId = builder.clientId;
    this.src = builder.src;
    this.dst = builder.dst;
  }

  public static class Builder {

    private int clientId;
    private Point src;
    private Point dst;

    public Builder withClientId(int clientId) {
      this.clientId = clientId;
      return this;
    }

    public Builder withSrc(Point src) {
      this.src = src;
      return this;
    }

    public Builder withDst(Point dst) {
      this.dst = dst;
      return this;
    }

    public PlayerMotion build() {
      validate();
      return new PlayerMotion(this);
    }

    private void validate() {
      Preconditions.checkNotNull(src, "src may not be null");
      Preconditions.checkNotNull(dst, "dst may not be null");
    }
  }
}
