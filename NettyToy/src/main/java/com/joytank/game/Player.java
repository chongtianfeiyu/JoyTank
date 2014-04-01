package com.joytank.game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.io.Serializable;

public class Player implements Drawable, Serializable {

  private static final long serialVersionUID = 1361848341433289227L;

  private Point location;
  private Point speed;
  private float angle;

  private Polygon shape;
  private Color color;
  
  private String name;

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
  
  public String getName() {
  	return name;
  }
  
  public void setName(String name) {
  	this.name = name;
  }

  public Player() {
    shape = new Polygon(new int[] { -10, 0, 10 }, new int[] { 0, 17, 0 }, 3);
  }

  @Override
  public void draw(Graphics2D g) {

    g.translate(location.getX(), location.getY());
    g.rotate(angle);

    g.setColor(color);
    g.drawString(name, 0, 0);
    g.fillPolygon(shape);

    g.rotate(-angle);
    g.translate(-location.getX(), -location.getY());
  }

}
