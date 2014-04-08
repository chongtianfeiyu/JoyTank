package com.joytank.net;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

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
	private final ConcurrentLinkedQueue<Object> messageQueue = Queues.newConcurrentLinkedQueue();

	private UdpServerChannelHandler channelHandler;
	private ConnectionlessBootstrap bootstrap;

	private boolean isServerRunning;

	private ExecutorService gameTaskExec;

	/**
	 * Create a UDP server listening to {@code port}
	 * 
	 * @param port
	 *          the port this server will listen to
	 */
	public UdpServer(int port) {
		Preconditions.checkState(port >= Consts.PORT_MIN && port <= Consts.PORT_MAX,
		    "port is not in a valid range [1024, 65535].");
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
	 * @param msg
	 */
	private void broadcastMsg(Object msg) {
		Iterator<Entry<Integer, ClientInfo>> it = clientsMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, ClientInfo> entry = it.next();
			SocketAddress address = entry.getValue().getAddress();
			if (!sendMsg(msg, address)) {
				it.remove();
				LOGGER.info(String.format("Removed client %s since connection cannot be established in %d seconds.",
				    address.toString(), Consts.CONN_TIME_LMT_SEC));
			}
		}
	}

	/**
	 * Send a message to the given address through UDP channel then close the
	 * channel immediately
	 * 
	 * @param msg
	 * @param address
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
	 * Retrieve and deal with messages from message queue
	 */
	private void handleMessages() {
		Object msg = null;
		// TODO if messages come faster than we can consume them, this loop will never end
		while ((msg = messageQueue.poll()) != null) {
			if (msg instanceof JoinRequest) {
				handleJoinRequest((JoinRequest) msg);
			} else if (msg instanceof PingMsg) {
				handlePingMsg((PingMsg) msg);
			} else if (msg instanceof PlayerMotionMsg) {
				handlePlayerMotion((PlayerMotionMsg) msg);
			}
		}
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

	/**
	 * For now just simply broadcast the message 
	 * 
	 * @param playerMotion
	 */
	private void handlePlayerMotion(PlayerMotionMsg playerMotion) {
		broadcastMsg(playerMotion);
	}

	/**
	 * Handle when server receives a join request from a new client: Assign a new
	 * ID to the client and send the message back
	 * 
	 * @param joinRequest
	 */
	private void handleJoinRequest(JoinRequest joinRequest) {
		int newClientId = clientsMap.size();
		LOGGER.info(String.format("Got hello from %s, accpet it and assign ID: %d", joinRequest.getAddress(), newClientId));
		ClientInfo info = new ClientInfo.Builder().withAddress(joinRequest.getAddress()).build();
		clientsMap.putIfAbsent(newClientId, info);
		JoinResponse msgBack = new JoinResponse(newClientId, true, Lists.newArrayList(clientsMap.keySet()));
		broadcastMsg(msgBack);
	}

	/**
	 * A task that updates game state and broadcasts to clients on a timely base
	 */
	private class GameTask implements Runnable {
		
		@Override
		public void run() {
			try {
				isServerRunning = true;
				while (isServerRunning) {
					handleMessages();
					Thread.sleep(TIME_SLICE_MILLIS);
				}
			} catch (Exception e) {
				LOGGER.info("Exception: ", e);
			}
		}
	}

	/**
	 * 
	 */
	private class UdpServerChannelHandler extends SimpleChannelHandler {

		/**
		 * Receive and enqueue new messages
		 */
		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
			Object msg = e.getMessage();
			messageQueue.offer(msg);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
			LOGGER.warn("exceptionCaught: ", e.getCause());
		}
	}
}
