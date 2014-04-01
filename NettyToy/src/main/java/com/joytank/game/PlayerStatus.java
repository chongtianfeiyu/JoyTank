package com.joytank.game;

import java.awt.Color;
import java.awt.Point;
import java.io.Serializable;

public class PlayerStatus implements Serializable {
  private static final long serialVersionUID = 6426947069660230551L;

  private Point location;
  private Point speed;
  private float angle;
  private int color;

  public Point getLocation() {
    return location;
  }

  public void setLocation(Point location) {
    this.location = location;
  }

  public Point getSpeed() {
    return speed;
  }

  public void setSpeed(Point speed) {
    this.speed = speed;
  }

  public float getAngle() {
    return angle;
  }

  public void setAngle(float angle) {
    this.angle = angle;
  }

  public int getColor() {
    return color;
  }

  public void setColor(int color) {
    this.color = color;
  }
  
  public Player createPlayer() {
    Player player = new Player();
    player.setAngle(angle);
    player.setColor(new Color(color));
    player.setLocation(location);
    player.setSpeed(speed);
    return player;
  }

  private PlayerStatus(Builder builder) {
    this.location = builder.location;
    this.speed = builder.speed;
    this.angle = builder.angle;
    this.color = builder.color;
  }

  public static class Builder {

    private Point location;
    private Point speed;
    private float angle;
    private int color;

    public Builder withLocation(Point location) {
      this.location = location;
      return this;
    }

    public Builder withSpeed(Point speed) {
      this.speed = speed;
      return this;
    }

    public Builder withAngle(float angle) {
      this.angle = angle;
      return this;
    }

    public Builder withColor(int color) {
      this.color = color;
      return this;
    }

    public PlayerStatus build() {
      validate();
      return new PlayerStatus(this);
    }

    private void validate() {
    }
  }
}
