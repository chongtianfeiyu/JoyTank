package com.joytank.net;

import java.awt.Point;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
import com.google.common.collect.Maps;
import com.joytank.game.PlayerMotion;
import com.joytank.game.PlayerStatus;

/**
 * 
 * @author lizhaoliu
 * 
 */
public class UdpServer {

	private static final Logger LOGGER = Logger.getLogger(UdpServer.class);

	private static final int TIME_SLICE_MILLIS = 30;

	private final int port;
	private final ConcurrentMap<Integer, ClientInfo> clientsMap = Maps.newConcurrentMap();
	private final PlayerStatusMap playerStatusMap = new PlayerStatusMap();

	private UdpServerChannelHandler channelHandler;
	private ConnectionlessBootstrap bootstrap;

	private boolean isServerRunning;
	
	private ExecutorService gameTaskExec;

	/**
	 * Create a UDP server listening to {@code port}
	 * 
	 * @param port the port this server will listen to
	 */
	public UdpServer(int port) {
		Preconditions.checkState(port >= Consts.PORT_MIN && port <= Consts.PORT_MAX, "port is not in range (1023, 65536)");

		this.port = port;
	}

	/**
	 * 
	 */
	public void run() {
		ChannelFactory channelFactory = new NioDatagramChannelFactory(Executors.newCachedThreadPool());
		bootstrap = new ConnectionlessBootstrap(channelFactory);
		bootstrap.setOption("receiveBufferSizePredictorFactory", new FixedReceiveBufferSizePredictorFactory(
		    Consts.UDP_PACKET_SIZE_MAX));
		channelHandler = new UdpServerChannelHandler();
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)),
				    new ObjectEncoder(), channelHandler);
			}
		});
		bootstrap.bind(new InetSocketAddress(port));
		LOGGER.info("Server listening to " + port);
		
		// TODO enable it to handle multiple games instances
		gameTaskExec = Executors.newCachedThreadPool();
		gameTaskExec.execute(new GameTask());
	}

	/**
	 * 
	 */
	private void broadcastPlayerStatus() {
		broadcastMsg(playerStatusMap);
	}

	/**
	 * 
	 * @param msg
	 */
	private void broadcastMsg(Object msg) {
		Iterator<Entry<Integer, ClientInfo>> it = clientsMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, ClientInfo> entry = it.next();
			SocketAddress address = entry.getValue().getAddress();
			if (!sendMsg(msg, address)) {
				it.remove();
				playerStatusMap.remove(entry.getKey());
				LOGGER.info(String.format("Removed client %s since connection cannot be established in %d seconds.",
				    address.toString(), Consts.CONN_TIME_LMT_SEC));
			}
		}
	}

	/**
	 * Send a message to the given address through UDP channel then close the channel
	 * 
	 * @param msg @Nonnull
	 * @param address @Nonnull
	 * @return true if message is sent successfully, otherwise false
	 */
	private boolean sendMsg(Object msg, SocketAddress address) {
	  Preconditions.checkState(msg != null);
	  Preconditions.checkState(address != null);
	  
		ChannelFuture channelFuture = bootstrap.connect(address);
		if (channelFuture.awaitUninterruptibly(Consts.CONN_TIME_LMT_SEC, TimeUnit.SECONDS)) {
			Channel channel = channelFuture.getChannel();
			channel.write(msg).addListener(ChannelFutureListener.CLOSE);
			return true;
		} else {
			LOGGER.info(String.format("Cannot connect to %s within %d second(s).", address, Consts.CONN_TIME_LMT_SEC));
		}
		return false;
	}

	/**
	 * A task that updates game state and broadcasts to clients on a timely base
	 */
	private class GameTask implements Runnable {
		private float time = 0.0f;
		
		@Override
		public void run() {
			try {
				isServerRunning = true;
				while (isServerRunning) {
				  // TODO update game status with the proper game logic here
					broadcastPlayerStatus();
					Thread.sleep(TIME_SLICE_MILLIS);
					time += TIME_SLICE_MILLIS;
				}
			} catch (Exception e) {
				LOGGER.info("Exception: ", e);
			}
		}
	}

	/**
	 * 
	 * @author lizhaoliu
	 *
	 */
	private class UdpServerChannelHandler extends SimpleChannelHandler {

		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			Object msg = e.getMessage();
			if (msg instanceof HelloMsg) {
				handleHelloMsg((HelloMsg) msg);
			} else if (msg instanceof PingMsg) {
				handlePingMsg((PingMsg) msg);
			} else if (msg instanceof PlayerMotion) {
				handlePlayerMotion((PlayerMotion) msg);
			}
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
			LOGGER.warn("exceptionCaught: ", e.getCause());
		}

		/**
		 * 
		 * @param msg
		 */
		private void handlePingMsg(PingMsg msg) {
			ClientInfo info = clientsMap.get(msg.getClientId());
			if (info != null) {
				SocketAddress remoteAddress = info.getAddress();
				sendMsg(msg, remoteAddress);
			}
		}

		private void handlePlayerMotion(PlayerMotion playerMotion) {
		  PlayerStatus playerStatus = playerStatusMap.get(playerMotion.getClientId());
			if (playerStatus != null) {
				playerStatus.setLocation(playerMotion.getDst());
			}
		}

		/**
		 * Handle when server receives a "Hello" from a new client: Assign a new ID to the client and send the message back
		 * 
		 * @param helloMsg
		 */
		private void handleHelloMsg(HelloMsg helloMsg) {
			int newClientId = clientsMap.size();
			HelloMsgBack msgBack = new HelloMsgBack(newClientId, true);
			LOGGER.info(String.format("Got hello from %s, accpet it and assign ID: %d", helloMsg.getAddress(), newClientId));
			PlayerStatus actorStatus = new PlayerStatus.Builder().withAngle(0).withColor(new Random().nextInt(0xffffff))
			    .withLocation(new Point(0, 0)).withSpeed(new Point()).build();
			ClientInfo info = new ClientInfo.Builder().withAddress(helloMsg.getAddress()).build();
			clientsMap.putIfAbsent(newClientId, info);
			playerStatusMap.put(newClientId, actorStatus);
			sendMsg(msgBack, helloMsg.getAddress());
		}
	}
}
