package com.joytank.game;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Random;
import java.util.concurrent.ConcurrentMap;
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
import com.google.common.collect.Maps;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.joytank.net.Consts;
import com.joytank.net.JoinRequest;
import com.joytank.net.JoinResponse;
import com.joytank.net.NetUtils;
import com.joytank.net.PingMsg;
import com.joytank.net.PlayerMotionMsg;
import com.joytank.net.UpdateRequest;

/**
 * Main GUI of the client side, this needs to replace the simple
 * {@link ClientUi}
 * 
 * @author lizhaoliu
 */
public class ClientGui extends SimpleApplication {

	private static final Logger LOGGER = Logger.getLogger(ClientGui.class);

	private Spatial sceneModel;
	private Node actor;

	private RigidBodyControl sceneBodyControl;
	private CharacterControl charControl;

	private AnimChannel animChannel;
	private AnimControl animControl;

	private BulletAppState bulletAppState;

	private CameraNode camNode;

	private Vector3f direction = new Vector3f();
	private Vector3f destination = new Vector3f();

	private final UdpClient udpClient;
	
	private final ConcurrentMap<Integer, Player> playerMap = Maps.newConcurrentMap();

	/**
	 * 
	 * @param serverHost
	 * @param serverPort
	 */
	public ClientGui(String serverHost, int serverPort) {
		this.udpClient = new UdpClient(serverHost, serverPort);
  }
	
	@Override
	public void simpleInitApp() {
		bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);

		assetManager.registerLocator("assets/models/Oto.zip", ZipLocator.class);
		actor = (Node) assetManager.loadModel("Oto.mesh.xml");
		actor.move(0, 4.5f, 0);
		actor.setLocalScale(0.75f);
		rootNode.attachChild(actor);

		animControl = actor.getControl(AnimControl.class);
		animControl.addListener(new AnimEvtListenerImpl());
		animChannel = animControl.createChannel();
		animChannel.setAnim("stand");

		assetManager.registerLocator("assets/models/town.zip", ZipLocator.class);
		sceneModel = assetManager.loadModel("main.scene");
		sceneModel.setLocalScale(2.0f);
		rootNode.attachChild(sceneModel);

		flyCam.setEnabled(false);

		setupCollision();
		registerInput();
		setUpLight();
		setCam();
	}

	public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
		if (animName.equals("Walk")) {
			channel.setAnim("stand", 0.50f);
			channel.setLoopMode(LoopMode.DontLoop);
			channel.setSpeed(1f);
		}
	}

	public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
		// unused
	}

	private void setupCollision() {
		CollisionShape sceneShape = CollisionShapeFactory.createMeshShape(sceneModel);
		sceneBodyControl = new RigidBodyControl(sceneShape, 0);
		sceneModel.addControl(sceneBodyControl);
		bulletAppState.getPhysicsSpace().add(sceneBodyControl);

		BoundingBox bv = (BoundingBox) actor.getWorldBound();
		CollisionShape actorShape = new CapsuleCollisionShape(bv.getXExtent(), bv.getYExtent(), 1);
		charControl = new CharacterControl(actorShape, 0.05f);
		charControl.setJumpSpeed(20);
		charControl.setFallSpeed(30);
		charControl.setGravity(30);
		charControl.setPhysicsLocation(actor.getLocalTranslation());
		actor.addControl(charControl);
		bulletAppState.getPhysicsSpace().add(charControl);
	}

	private void setUpLight() {
		viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));

		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(1.3f));
		rootNode.addLight(al);

		DirectionalLight dl = new DirectionalLight();
		dl.setColor(ColorRGBA.White);
		dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
		rootNode.addLight(dl);
	}

	private void setCam() {
		int camDist = 80;
		camNode = new CameraNode("Camera Node", cam);
		Vector3f actorPos = actor.getLocalTranslation();
		camNode.setLocalTranslation(actorPos.x, actorPos.y + camDist, actorPos.z - camDist);
		camNode.lookAt(actorPos, new Vector3f(0, 0.707f, 0.707f));
		rootNode.attachChild(camNode);
	}

	private void registerInput() {
		inputManager.addMapping("Walk", new KeyTrigger(KeyInput.KEY_UP), new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping("move", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
		inputManager.addListener(new MyActionListener(), "Walk");
		inputManager.addListener(new MyActionListener(), "move");
	}

	class MyActionListener implements ActionListener {
		@Override
		public void onAction(String arg0, boolean arg1, float arg2) {
			direction.set(cam.getDirection()).normalizeLocal();
			if (arg0.equals("Walk")) {
				if (!animChannel.getAnimationName().equals("Walk") && !arg1) {
					animChannel.setAnim("Walk", 0.50f);
					animChannel.setLoopMode(LoopMode.Loop);
				}
			}
			if (arg0.equals("move") && arg1) {
				if (!animChannel.getAnimationName().equals("Walk") && !arg1) {
					animChannel.setAnim("Walk", 0.50f);
					animChannel.setLoopMode(LoopMode.Loop);
				}
				CollisionResults results = new CollisionResults();
				Vector2f click2d = inputManager.getCursorPosition();
				Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
				Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), cam.getFrustumNear())
				    .subtractLocal(click3d).normalizeLocal();
				Ray ray = new Ray(click3d, dir);
				sceneModel.collideWith(ray, results);
				if (results.size() > 0) {
					CollisionResult cr = results.getClosestCollision();
//					moveCharacter(charControl, cr.getContactPoint());
					udpClient.sendMsg(new PlayerMotionMsg(udpClient.getId(), cr.getContactPoint()));
				}
			}
		}
	}

	/**
	 * Move a character 
	 * 
	 * @param characterControl
	 * @param dst
	 */
	public void moveCharacter(CharacterControl characterControl, Vector3f dst) {
		Vector3f loc = characterControl.getPhysicsLocation();
		Vector3f dir = dst.subtract(loc);
		dir.y = 0;
		dir.multLocal(0.5f).normalizeLocal();
		characterControl.setWalkDirection(dir);
		characterControl.setViewDirection(dir);
		destination = dst.clone();
	}

	@Override
	public void simpleUpdate(float tpf) {
		super.simpleUpdate(tpf);
		if (destination.distance(charControl.getPhysicsLocation()) < 5f) {
			charControl.setWalkDirection(Vector3f.ZERO);
		}
	}

	private class AnimEvtListenerImpl implements AnimEventListener {
		@Override
		public void onAnimChange(AnimControl arg0, AnimChannel arg1, String arg2) {}

		@Override
		public void onAnimCycleDone(AnimControl arg0, AnimChannel arg1, String arg2) {}
	}
	
	public class UdpClient {

		private static final int INVALID_ID = -1;

		private final Logger logger = Logger.getLogger(UdpClient.class);
		private final SocketAddress serverAddress;
		private final SocketAddress localAddress;

		private int id = INVALID_ID;
		private ClientGui clientGui;

		private UdpClientChannelHandler channelHandler;
		private ConnectionlessBootstrap bootstrap;

		private boolean isPinging;

		/**
		 * Create a UDP client
		 * 
		 * @param serverHost
		 * @Nonnull the IPv4 address of the server
		 * @param serverPort
		 *          port number of the server
		 */
		public UdpClient(String serverHost, int serverPort) {
			Preconditions.checkState(!StringUtils.isBlank(serverHost), "serverHostName is unexpectedly null or blank.");

			this.serverAddress = new InetSocketAddress(serverHost, serverPort);
			this.localAddress = new InetSocketAddress(NetUtils.getLocalAddress(), genRandomPort());
		}

		/**
		 * 
		 * @return
		 */
		public ClientGui getClientGui() {
			return clientGui;
		}
		
		/**
		 * 
		 * @param clientGui
		 */
		public void setClientGui(ClientGui clientGui) {
			Preconditions.checkState(clientGui != null, "clientGui is unexpectedly null.");
			this.clientGui = clientGui;
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

			// Initiate connection to server
			if (!sendMsg(createHelloMsg())) {
				logger.info("Cannot connect to server, now exit.");
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
		 * @param msg
		 * @Nonnull message to be sent
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
				logger.info(String.format("Cannot connect to %s within %d second(s).", serverAddress, Consts.CONN_TIME_LMT_SEC));
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
								logger.info(String.format("Cannot connect to %s within %d second(s).", serverAddress,
								    Consts.CONN_TIME_LMT_SEC));
							}
						}
					} catch (InterruptedException e) {
						logger.warn("InterruptedException", e);
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
		 * Create an initiation message
		 * 
		 * @return
		 */
		private JoinRequest createHelloMsg() {
			return new JoinRequest(localAddress);
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
				Object msg = e.getMessage();
				if (msg instanceof PingMsg) {
					PingMsg pingMsg = (PingMsg) msg;
					if (pingMsg.getPingId() == expectedPingId) {
						long dTime = System.nanoTime() - nanoTimePingFired;
						logger.info(String.format("Ping: %d ms", dTime / 1000000));
					}
				} else if (msg instanceof JoinResponse) {
					handleJoinResponse((JoinResponse) msg);
				}
			}

			@Override
			public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
				logger.warn("exceptionCaught: ", e.getCause());
			}

			private void handleJoinResponse(JoinResponse msg) {
				if (msg.isAccepted()) {
					id = msg.getCliendId();
					logger.info("Accepted by server, assigned ID: " + id);
					doPingServer();
				} else {
					logger.info("Server rejected connection, now exit.");
					System.exit(0);
				}
			}
		}

	}
}
