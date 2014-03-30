package com.joytank.game;

import java.awt.Point;
import java.io.Serializable;

public class PlayerMotionToServer implements Serializable {
  private static final long serialVersionUID = 2196791557505482846L;
  private final Point src;
  private final Point dst;

  public Point getSrc() {
    return src;
  }

  public Point getDst() {
    return dst;
  }

  public PlayerMotionToServer(Point src, Point dst) {
    this.src = src;
    this.dst = dst;
  }
}
