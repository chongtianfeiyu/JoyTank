package com.joytank.game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.Point2D;

public class Player implements Drawable {

  private Point2D location;
  private Point2D speed;
  private float angle;
  private Shape shape;
  private Color color;
  
  public Player() {
    Polygon poly = new Polygon(new int[]{-1, 0, 1}, new int[]{0, 1, 0}, 3);
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
