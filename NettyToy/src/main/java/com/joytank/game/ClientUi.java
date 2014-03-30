package com.joytank.game;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.SocketAddress;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ClientUi extends JFrame {
  private static final long serialVersionUID = 3056308717356176233L;

  private final SocketAddress serverAddress;
  
  private Player player;

  public ClientUi(SocketAddress serverAddress) {
    this.serverAddress = serverAddress;
    
    setLayout(new BorderLayout());
    add(new UiPanel());
  }

  private class UiPanel extends JPanel {
    
    private static final long serialVersionUID = -1914273488587960000L;
    private static final int WIDTH = 640;
    private static final int HEIGHT = 480;

    public UiPanel() {
      setPreferredSize(new Dimension(WIDTH, HEIGHT));
      
      this.addMouseListener(new MouseListener() {
        @Override
        public void mouseReleased(MouseEvent e) {}

        @Override
        public void mousePressed(MouseEvent e) {
          Point src = player.getLocation();
          Point dst = e.getPoint();
        }

        @Override
        public void mouseExited(MouseEvent e) {}

        @Override
        public void mouseEntered(MouseEvent e) {}

        @Override
        public void mouseClicked(MouseEvent e) {}
      });
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
    }

  }
}
