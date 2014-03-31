package com.joytank.game;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.google.common.base.Preconditions;
import com.joytank.net.AllPlayerInfo;
import com.joytank.net.UdpClient;

public class ClientUi extends JFrame {
  private static final long serialVersionUID = 3056308717356176233L;

  private final UdpClient client;
  private final UiPanel uiPanel;

  private Actor player;
  private AllPlayerInfo allPlayerInfo;

  public ClientUi(UdpClient client) {
    Preconditions.checkState(client != null);

    this.uiPanel = new UiPanel();
    this.client = client;
    setLayout(new BorderLayout());
    add(uiPanel);
    
    client.setUi(this);
    client.run();
    
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    pack();
    setVisible(true);
  }

  public void update(AllPlayerInfo info) {
    this.allPlayerInfo = info;
    this.uiPanel.repaint();
  }

  private class UiPanel extends JPanel {

    private static final long serialVersionUID = -1914273488587960000L;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    public UiPanel() {
      setPreferredSize(new Dimension(WIDTH, HEIGHT));

      this.addMouseListener(new MouseListener() {
        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
          Preconditions.checkState(e != null, "MouseEvent cannot be null.");
          Preconditions.checkState(client != null, "client cannot be null.");
          
          PlayerMotionToServer motionMsg = new PlayerMotionToServer.Builder().withClientId(client.getId())
              .withSrc(player.getLocation()).withDst(e.getPoint()).build();
          client.sendMsg(motionMsg);
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }
      });
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (allPlayerInfo != null) {
        for (ActorStatus e : allPlayerInfo.getPlayerEssences()) {
          e.createPlayer().draw((Graphics2D) g);
        }
      }
    }
  }
}
