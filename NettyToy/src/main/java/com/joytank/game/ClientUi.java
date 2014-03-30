package com.joytank.game;

import java.awt.Graphics;
import java.net.SocketAddress;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ClientUi extends JFrame {
  private static final long serialVersionUID = 3056308717356176233L;

  private final SocketAddress serverAddress;

  public ClientUi(SocketAddress serverAddress) {
    this.serverAddress = serverAddress;
  }

  private class UiPanel extends JPanel {

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
    }
  }
}
