package com.joytank.game;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.apache.commons.lang.SerializationUtils;
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
import com.google.common.collect.Queues;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.scene.Spatial;
import com.joytank.net.ClientInfo;
import com.joytank.net.Consts;
import com.joytank.net.NetUtils;

/**
 * 
 */
public abstract class AbstractApplication extends SimpleApplication {

	private static final Logger logger = Logger.getLogger(AbstractApplication.class);

	// Physics state
	protected BulletAppState bulletAppState;

	// Fields related to networking
	protected final SocketAddress localAddress;
	protected final UdpComponent udpComponent;
	protected final Queue<Object> messageQueue = Queues.newConcurrentLinkedQueue();

	// Fields of game logic
	protected final ConcurrentMap<Integer, Player> playerMap = Maps.newConcurrentMap();
	protected RigidEntity stage;

	/**
	 * Construct with a given local port to bind to
	 * 
	 * @param localPort
	 *          the local port to bind to
	 */
	public AbstractApplication(int localPort) {
		super();
		Preconditions.checkState(localPort >= Consts.PORT_MIN && localPort <= Consts.PORT_MAX,
		    "port is not in a valid range [1024, 65535].");
		this.localAddress = new InetSocketAddress(NetUtils.getLocalAddress(), localPort);
		this.udpComponent = new UdpComponent();
	}

	/**
	 * Construct with a randomly generated local port
	 */
	public AbstractApplication() {
		this(NetUtils.generateRandomPort());
	}

	@Override
	public void simpleInitApp() {
		bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);

		udpComponent.setup();
		setupStage();
		initAll();
	}

	@Override
	public void simpleUpdate(float tpf) {
		super.simpleUpdate(tpf);
		handleMessages();
	}

	/**
	 * Poll all messages in message queue and handle them
	 */
	protected void handleMessages() {
		Object msg = null;
		while ((msg = messageQueue.poll()) != null) {
			handleMessage(msg);
		}
	}

	/**
	 * Take a snapshot of the game
	 * 
	 * @return
	 */
	protected GameState createGameState() {
		GameState gameState = new GameState(System.nanoTime());

		ConcurrentMap<Integer, PlayerState> playerStateMap = gameState.getPlayerStateMap();
		for (Entry<Integer, Player> entry : playerMap.entrySet()) {
			int id = entry.getKey();
			Player player = entry.getValue();
			playerStateMap.putIfAbsent(id, new PlayerState(player));
		}

		return gameState;
	}

	/**
	 * Add an entity to the game space
	 * 
	 * @param entity
	 *          entity to be added
	 */
	protected void addToGame(@Nonnull AbstractEntity entity) {
		Preconditions.checkState(entity != null, "entity is unexpectedly null.");

		Spatial spatial = entity.getSpatial();

		// Attempt to add physics control
		PhysicsControl physicsControl = entity.getPhysicsControl();
		if (physicsControl != null) {
			bulletAppState.getPhysicsSpace().add(physicsControl);
		}

		rootNode.attachChild(spatial);
	}

	/**
	 * Setup the game stage/terrain/scene
	 */
	private void setupStage() {
		String modelPath = "models/town/main.scene";
		stage = RigidEntity.loadWithMeshCollisionShape(modelPath, 0, assetManager);
		addToGame(stage);
	}

	/**
	 * Initialize everything here
	 */
	abstract protected void initAll();

	/**
	 * Handle a particular message here
	 * 
	 * @param msg
	 *          message object
	 */
	abstract protected void handleMessage(Object msg);

	/**
	 * The network component using UDP protocol
	 */
	protected class UdpComponent {

		private final Logger logger = Logger.getLogger(UdpComponent.class);

		private ConnectionlessBootstrap bootstrap;

		/**
		 * Setup the network and bind to local {@link SocketAddress}
		 */
		public void setup() {
			ChannelFactory channelFactory = new NioDatagramChannelFactory(Executors.newCachedThreadPool());
			bootstrap = new ConnectionlessBootstrap(channelFactory);
			bootstrap.setOption("receiveBufferSizePredictorFactory", new FixedReceiveBufferSizePredictorFactory(
			    Consts.UDP_PACKET_SIZE_MAX));

			final UdpComponentHandler channelHandler = new UdpComponentHandler();
			final ObjectDecoder objDecoder = new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null));
			final ObjectEncoder objEncoder = new ObjectEncoder();
			bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
				@Override
				public ChannelPipeline getPipeline() throws Exception {
					return Channels.pipeline(objDecoder, objEncoder, channelHandler);
				}
			});

			bootstrap.bind(localAddress);
			logger.info("Bound to local address: " + localAddress.toString());
		}

		/**
		 * Broadcast a message to clients
		 * 
		 * @param msg
		 *          message object
		 * @param clientMap
		 *          map of clients information
		 */
		public void broadcastMsg(@Nonnull Serializable msg, @Nonnull ConcurrentMap<Integer, ClientInfo> clientMap) {
			Preconditions.checkState(msg != null);
			Preconditions.checkState(clientMap != null);

			Iterator<Entry<Integer, ClientInfo>> it = clientMap.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Integer, ClientInfo> entry = it.next();
				SocketAddress address = entry.getValue().getClientAddress();
				if (!sendMsg(msg, address)) {
					it.remove();
					logger.info(String.format("Removed client %s since connection cannot be established in %d seconds.",
					    address.toString(), Consts.CONN_TIME_LMT_SEC));
				}
			}
		}

		/**
		 * Send a message to the given address through UDP channel then close the channel immediately
		 * 
		 * @param msg
		 *          message object
		 * @param remoteAddress
		 *          the remote address to send to
		 * @return whether the message has been sent successfully
		 */
		public boolean sendMsg(@Nonnull Serializable msg, @Nonnull SocketAddress remoteAddress) {
			Preconditions.checkState(msg != null);
			Preconditions.checkState(remoteAddress != null);

			logger.info(String.format("Message pre-sent => {to: %s, size: %d, class: %s}.", remoteAddress,
			    SerializationUtils.serialize(msg).length, msg.getClass().getName()));
			ChannelFuture channelFuture = bootstrap.connect(remoteAddress);
			if (channelFuture.awaitUninterruptibly(Consts.CONN_TIME_LMT_SEC, TimeUnit.SECONDS)) {
				Channel channel = channelFuture.getChannel();
				channel.write(msg).addListener(ChannelFutureListener.CLOSE);
				return true;
			} else {
				logger
				    .info(String.format("Cannot connect to %s within %d second(s).", remoteAddress, Consts.CONN_TIME_LMT_SEC));
			}
			return false;
		}

		/**
		 * Channel handler of this network component
		 */
		private class UdpComponentHandler extends SimpleChannelHandler {

			@Override
			public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
				Object msg = e.getMessage();
				messageQueue.offer(msg);
			}

			@Override
			public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
				logger.warn("exceptionCaught: ", e.getCause());
			}
		}
	}
}
