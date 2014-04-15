package com.joytank.game;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executors;

import javax.annotation.Nonnull;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import com.google.common.base.Preconditions;

public class LobbyClient {

  private JFrame frmLobby;
  private final JPanel panel = new JPanel();
  private final JPanel panel_1 = new JPanel();
  private final JList list = new JList();
  private final JList list_1 = new JList();
  private final JPanel panel_2 = new JPanel();
  private final JButton btnNewButton = new JButton("Refresh Server");
  private final JButton btnNewButton_1 = new JButton("Join & Play");

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          LobbyClient window = new LobbyClient();
          window.frmLobby.setVisible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Create the application.
   */
  public LobbyClient() {
    initialize();
  }

  /**
   * Initialize the contents of the frame.
   */
  private void initialize() {
    frmLobby = new JFrame();
    frmLobby.setTitle("Lobby");
    frmLobby.setBounds(100, 100, 640, 480);
    frmLobby.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frmLobby.getContentPane().setLayout(new BoxLayout(frmLobby.getContentPane(), BoxLayout.X_AXIS));
    this.panel.setBorder(new TitledBorder(null, "Servers", TitledBorder.LEADING, TitledBorder.TOP, null, null));

    frmLobby.getContentPane().add(this.panel);
    this.panel.setLayout(new BorderLayout(0, 0));
    this.list.setBackground(new Color(224, 255, 255));

    this.panel.add(this.list, BorderLayout.CENTER);
    this.panel_1.setBorder(new TitledBorder(null, "Players", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    frmLobby.getContentPane().add(this.panel_1);
    this.panel_1.setLayout(new BorderLayout(0, 0));
    this.list_1.setBackground(new Color(250, 250, 210));

    this.panel_1.add(this.list_1, BorderLayout.CENTER);
    this.panel_2.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));

    this.panel_1.add(this.panel_2, BorderLayout.SOUTH);

    this.panel_2.add(this.btnNewButton);

    this.panel_2.add(this.btnNewButton_1);
  }

  private class TcpComponent {
    private ClientBootstrap bootstrap;

    private final SocketAddress lobbyAddress;

    public TcpComponent(@Nonnull GameConfig config) {
      Preconditions.checkState(config != null);

      lobbyAddress = new InetSocketAddress(config.getLobbyHost(), config.getLobbyPort());
    }

    public void setup() {
      ChannelFactory channelFactory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
          Executors.newCachedThreadPool());
      bootstrap = new ClientBootstrap(channelFactory);
      final ChannelHandler channelHandler = new MessageHandler();
      final ObjectDecoder objDecoder = new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null));
      final ObjectEncoder objEncoder = new ObjectEncoder();
      bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
        @Override
        public ChannelPipeline getPipeline() throws Exception {
          return Channels.pipeline(objDecoder, objEncoder, channelHandler);
        }
      });
      bootstrap.connect(lobbyAddress);
    }

    private class MessageHandler extends SimpleChannelHandler {

      @Override
      public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {}

      @Override
      public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        JOptionPane.showMessageDialog(frmLobby, e.getCause(), "Error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
      }
    }
  }
}
