package com.joytank.game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;

public class Player implements Drawable {

  private Point location;
  private Point speed;
  private float angle;
  private Shape shape;
  private Color color;

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

  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  public Player() {
    Polygon poly = new Polygon(new int[] { -3, 0, 3 }, new int[] { 0, 5, 0 }, 3);
    shape = poly;
    color = Color.YELLOW;
  }

  @Override
  public void draw(Graphics2D g) {
    g.translate(location.getX(), location.getY());
    g.rotate(angle);

    g.setColor(color);
    g.fill(shape);

    g.rotate(-angle);
    g.translate(-location.getX(), -location.getY());
  }

}
