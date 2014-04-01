package com.joytank.game;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.google.common.base.Preconditions;
import com.joytank.net.PlayerStatusMap;
import com.joytank.net.UdpClient;

public class ClientUi extends JFrame {
  private static final long serialVersionUID = 3056308717356176233L;

  private final UdpClient client;
  private final UiPanel uiPanel;

  private Player actor;
  private PlayerStatusMap playerStatusMap;

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

  public void update(PlayerStatusMap playerStatusMap) {
    this.playerStatusMap = playerStatusMap;
    for (Entry<Integer, PlayerStatus> entry : playerStatusMap.getInternalMap().entrySet()) {
      if (entry.getKey() == this.client.getId()) {
        actor = entry.getValue().createPlayer();
      }
    }
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
        public void mouseReleased(MouseEvent e) {}

        @Override
        public void mousePressed(MouseEvent e) {
          if (client == null || actor == null) {
            return;
          }
          PlayerMotion motionMsg = new PlayerMotion.Builder()
              .withClientId(client.getId()).withSrc(actor.getLocation()).withDst(e.getPoint())
              .build();
          client.sendMsg(motionMsg);
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
      if (playerStatusMap != null) {
        for (Entry<Integer, PlayerStatus> entry : playerStatusMap.getInternalMap().entrySet()) {
        	Player actor = entry.getValue().createPlayer();
        	if (entry.getKey() == client.getId()) {
        		actor.setName("You");
        	} else {
        		actor.setName(entry.getKey() + "");
        	}
          actor.draw((Graphics2D) g);
        }
      }
    }
  }
}
