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
	 * 
	 * @param serverHostName
	 * @Nonnull
	 * @param serverPort
	 */
	public UdpClient(String serverHostName, int serverPort) {
		Preconditions.checkState(!StringUtils.isBlank(serverHostName), "serverHostName is unexpectedly null or blank.");

		this.serverAddress = new InetSocketAddress(serverHostName, serverPort);
		this.localAddress = getLocalAddress();
		this.id = localAddress.hashCode();
	}

	public void setUi(ClientUi clientUi) {
		this.clientUi = clientUi;
	}

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

	public int getId() {
		return id;
	}

	public boolean sendMsg(Object msg) {
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

	private int genRandomPort() {
		int port = 0;
		while (port < Consts.PORT_MIN || port > Consts.PORT_MAX) {
			port = new Random().nextInt(Consts.PORT_MAX);
		}
		return port;
	}

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

	private HelloMsg createHelloMsg() {
		return new HelloMsg(id, localAddress);
	}

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
			} else if (msgObj instanceof ActorsStatusMap) {
				handleAllPlayerInfo((ActorsStatusMap) msgObj);
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
			LOGGER.warn("exceptionCaught: ", e.getCause());
		}

		private void handleAllPlayerInfo(ActorsStatusMap actorsStatusMap) {
			clientUi.update(actorsStatusMap);
		}
	}

}
