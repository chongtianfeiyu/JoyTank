package com.joytank.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.FixedReceiveBufferSizePredictorFactory;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import com.google.common.base.Preconditions;
import com.joytank.game.ClientUi;

/**
 * 
 * @author lizhaoliu
 * 
 */
public class UdpClient {

	private static final Logger LOGGER = Logger.getLogger(UdpClient.class);

	private final SocketAddress serverAddress;
	private final SocketAddress localAddress;
	private final int id;

	private ClientUi clientUi;
	private UdpClientChannelHandler channelHandler;
	private ConnectionlessBootstrap bootstrap;

	private boolean isPinging;

	/**
	 * Create a UDP client
	 * 
	 * @param serverHost @Nonnull the IPv4 address of the server
	 * @param serverPort port number of the server
	 */
	public UdpClient(String serverHost, int serverPort) {
		Preconditions.checkState(!StringUtils.isBlank(serverHost), "serverHostName is unexpectedly null or blank.");

		this.serverAddress = new InetSocketAddress(serverHost, serverPort);
		this.localAddress = getLocalAddress();
		this.id = localAddress.hashCode();
	}

	/**
	 * Set the associated UI of this client 
	 * 
	 * @param clientUi
	 */
	public void setUi(ClientUi clientUi) {
		this.clientUi = clientUi;
	}

	/**
	 * Set up and start this client
	 */
	public void run() {
		ChannelFactory channelFactory = new NioDatagramChannelFactory(Executors.newCachedThreadPool());
		bootstrap = new ConnectionlessBootstrap(channelFactory);
		bootstrap.setOption("receiveBufferSizePredictorFactory", new FixedReceiveBufferSizePredictorFactory(
		    Consts.UDP_PACKET_SIZE_MAX));
		channelHandler = new UdpClientChannelHandler();
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)),
				    new ObjectEncoder(), channelHandler);
			}
		});
		bootstrap.bind(localAddress);
		if (sendMsg(createHelloMsg())) {
			doPingServer();
		} else {
			LOGGER.info("Cannot connect to server, now exit.");
			System.exit(0);
		}
	}

	/**
	 * Return the unique ID of this client
	 * 
	 * @return unique ID
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Request updated state from server
	 * 
	 * @return
	 */
	public boolean requestUpdate() {
	  UpdateRequest request = new UpdateRequest.Builder().withClientId(id).build();
	  return sendMsg(request);
	}
	
	/**
	 * Request to stop pinging the server
	 */
	public void stopPingingServer() {
	  isPinging = false;
	}

	/**
	 * Send a message to server through UDP channel
	 * 
	 * @param msg @Nonnull message to be sent
	 * @return
	 */
	public boolean sendMsg(Object msg) {
	  Preconditions.checkState(msg != null);
	  
		ChannelFuture channelFuture = bootstrap.connect(serverAddress);
		if (channelFuture.awaitUninterruptibly(Consts.CONN_TIME_LMT_SEC, TimeUnit.SECONDS)) {
			Channel channel = channelFuture.getChannel();
			channel.write(msg).addListener(ChannelFutureListener.CLOSE);
			return true;
		} else {
			LOGGER.info(String.format("Cannot connect to %s within %d second(s).", serverAddress, Consts.CONN_TIME_LMT_SEC));
		}
		return false;
	}

	/**
	 * Start a daemon task for pinging the server
	 */
	private void doPingServer() {
		ExecutorService exec = Executors.newFixedThreadPool(1, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		});

		exec.execute(new Runnable() {
			@Override
			public void run() {
				try {
					int pingId = 0;
					isPinging = true;
					while (isPinging) {
						ChannelFuture channelFuture = bootstrap.connect(serverAddress);
						if (channelFuture.awaitUninterruptibly(Consts.CONN_TIME_LMT_SEC, TimeUnit.SECONDS)) {
							Channel channel = channelFuture.getChannel();
							++pingId;
							PingMsg pingMsg = new PingMsg(pingId, id);
							channel.write(pingMsg);
							channelHandler.setPingInfo(pingId, System.nanoTime());
							Thread.sleep(Consts.PING_INTERVAL_MILLISEC);
						} else {
							LOGGER.info(String.format("Cannot connect to %s within %d second(s).", serverAddress,
							    Consts.CONN_TIME_LMT_SEC));
						}
					}
				} catch (InterruptedException e) {
					LOGGER.warn("InterruptedException", e);
				}
			}
		});
	}

	/**
	 * Generate a random valid port
	 * 
	 * @return
	 */
	private int genRandomPort() {
		int port = 0;
		while (port < Consts.PORT_MIN || port > Consts.PORT_MAX) {
			port = new Random().nextInt(Consts.PORT_MAX);
		}
		return port;
	}

	/**
	 * Find the non-loopback local {@link SocketAddress}
	 * 
	 * @return
	 */
	private SocketAddress getLocalAddress() {
		int port = genRandomPort();
		try {
			Socket socket = new Socket("www.google.com", 80);
			String hostName = socket.getLocalAddress().getHostAddress();
			socket.close();
			return new InetSocketAddress(hostName, port);
		} catch (Exception e) {
			LOGGER.warn("Exception: ", e);
		}
		try {
			return new InetSocketAddress(InetAddress.getLocalHost().getHostName(), port);
		} catch (UnknownHostException e) {
			LOGGER.warn("UnknownHostException: ", e);
		}
		return null;
	}

	/**
	 * Create an initiation message
	 * 
	 * @return
	 */
	private HelloMsg createHelloMsg() {
		return new HelloMsg(id, localAddress);
	}

	/**
	 * 
	 */
	private class UdpClientChannelHandler extends SimpleChannelHandler {

		private int expectedPingId;
		private long nanoTimePingFired;

		public void setPingInfo(int expectedPingId, long nanoTimePingFired) {
			this.expectedPingId = expectedPingId;
			this.nanoTimePingFired = nanoTimePingFired;
		}

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			Object msgObj = e.getMessage();
			if (msgObj instanceof PingMsg) {
				PingMsg pingMsg = (PingMsg) msgObj;
				if (pingMsg.getPingId() == expectedPingId) {
					long dTime = System.nanoTime() - nanoTimePingFired;
					LOGGER.info(String.format("Ping: %d ms", dTime / 1000000));
				}
			} else if (msgObj instanceof PlayerStatusMap) {
				handleAllPlayerInfo((PlayerStatusMap) msgObj);
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
			LOGGER.warn("exceptionCaught: ", e.getCause());
		}

		private void handleAllPlayerInfo(PlayerStatusMap actorsStatusMap) {
			clientUi.update(actorsStatusMap);
		}
	}

}
