package com.joytank.net.lobby;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
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
import com.joytank.game.AbstractApplication;
import com.joytank.game.GameConfig;
import com.joytank.net.game.Consts;
import com.joytank.net.game.Utils;

import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * Client GUI of lobby
 */
public class LobbyClient {

	private JFrame frmLobby;
	private final JPanel panel = new JPanel();
	private final JList<ServerDesc> serversList = new JList<ServerDesc>();
	private final JPanel panel_2 = new JPanel();
	private final JButton btnRefreshServers = new JButton("Refresh Servers");
	private final JButton btnJoinPlay = new JButton("Join & Play");

	private TcpComponent tcpComponent;
	private final JPanel panel_1 = new JPanel();
	private final JLabel lblNewLabel = new JLabel("Name");
	private final JPanel panel_3 = new JPanel();
	private final JTextField textField = new JTextField();

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
		this.textField.setColumns(10);
		frmLobby = new JFrame();
		frmLobby.setTitle("Lobby");
		frmLobby.setBounds(100, 100, 572, 480);
		frmLobby.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frmLobby.getContentPane().setLayout(new BoxLayout(frmLobby.getContentPane(), BoxLayout.X_AXIS));
		this.panel.setBorder(new TitledBorder(null, "Servers", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		frmLobby.getContentPane().add(this.panel);
		this.panel.setLayout(new BorderLayout(0, 0));
		this.serversList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.serversList.setBackground(new Color(224, 255, 255));

		this.panel.add(this.serversList, BorderLayout.CENTER);
		this.panel.add(this.panel_2, BorderLayout.SOUTH);
		this.panel_2.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));

		this.panel_2.add(this.btnRefreshServers);

		this.panel_2.add(this.btnJoinPlay);
		this.panel_1.setBorder(new TitledBorder(null, "Player Info", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		frmLobby.getContentPane().add(this.panel_1);
		
		this.panel_1.add(this.panel_3);
		this.panel_3.add(this.lblNewLabel);
		
		this.panel_3.add(this.textField);

		// Initialize the network component
		tcpComponent = new TcpComponent(Utils.getGameConfig());
		tcpComponent.setup();

		// Initialize callbacks
		this.btnRefreshServers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				tcpComponent.sendMsg(new RefreshRequest());
			}
		});

		this.btnJoinPlay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ServerDesc serverDesc = serversList.getSelectedValue();
				if (serverDesc != null) {
					// TODO fire out a joinplayrequest
					JoinPlayRequest request = new JoinPlayRequest(serverDesc);
					tcpComponent.sendMsg(request);
				} else {
					JOptionPane.showMessageDialog(frmLobby, "Please select a server to join.", "Kind reminder",
					    JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		frmLobby.setVisible(true);
	}

	/**
	 * 
	 */
	private class TcpComponent {
		private ClientBootstrap bootstrap;
		private Channel channel;

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
			ChannelFuture channelFuture = bootstrap.connect(lobbyAddress);
			try {
				if (channelFuture.await(Consts.CONN_TIME_LMT_SEC, TimeUnit.SECONDS)) {
					channel = channelFuture.getChannel();
					sendMsg(new RefreshRequest());
				}
			} catch (InterruptedException e) {
				JOptionPane.showMessageDialog(frmLobby, e.getCause(), "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		}

		public void sendMsg(Serializable msg) {
			if (channel != null) {
				channel.write(msg);
			}
		}

		private class MessageHandler extends SimpleChannelHandler {

			@Override
			public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
				Object msg = e.getMessage();

				if (msg instanceof RefreshResponse) {
					final RefreshResponse response = (RefreshResponse) msg;
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							DefaultListModel<ServerDesc> listModel = new DefaultListModel<ServerDesc>();
							for (ServerDesc desc : response.getServerDescs()) {
								listModel.addElement(desc);
							}
							serversList.setModel(listModel);
						}
					});
				}
				if (msg instanceof JoinPlayResponse) {
					JoinPlayResponse response = (JoinPlayResponse) msg;
					if (response.isAccepted()) {
						// TODO start new client application here
						ServerDesc serverDesc = response.getServerDesc(); 
						AbstractApplication.startClientApplication(serverDesc.getServerHost(), serverDesc.getServerPort());
					} else {
						JOptionPane.showMessageDialog(frmLobby, "Server rejected, LOL...", "Meh", JOptionPane.WARNING_MESSAGE);
					}
				}
			}

			@Override
			public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
				JOptionPane.showMessageDialog(frmLobby, e.getCause(), "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		}
	}
}
