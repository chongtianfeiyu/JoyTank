package com.joytank.game;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext.Type;
import com.joytank.net.game.ClientInfo;
import com.joytank.net.game.Consts;
import com.joytank.net.game.Message;
import com.joytank.net.game.Utils;

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
  protected final Queue<Message> messageQueue = Queues.newConcurrentLinkedQueue();

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
    this.localAddress = new InetSocketAddress(Utils.getLocalAddress(), localPort);
    this.udpComponent = new UdpComponent();
  }

  /**
   * Construct with a randomly generated local port
   */
  public AbstractApplication() {
    this(Utils.generateRandomPort());
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
    Message msg = null;
    while ((msg = messageQueue.poll()) != null) {
      handleMessage(msg);
    }
  }

  /**
   * Return an immutable copy of the player map
   * 
   * @return an immutable copy of the player map
   */
  public Map<Integer, Player> getPlayerMap() {
    return ImmutableMap.copyOf(playerMap);
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
    // String modelPath = "models/terrain/terrain.obj";
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
   */
  abstract protected void handleMessage(Message msg);

  /**
   * The network component using UDP protocol
   */
  protected class UdpComponent {

    private ConnectionlessBootstrap bootstrap;

    private final ChannelFutureListener CLOSE_CHANNEL_SYNC = new ChannelFutureListener() {
      @Override
      public void operationComplete(ChannelFuture future) throws Exception {
        future.getChannel().close().awaitUninterruptibly();
      }
    };

    /**
     * Instantiate the network components
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
    }

    /**
     * Bind to local address
     */
    public void bind() {
      bootstrap.bind(localAddress);
      logger.info("Bound to local address: " + localAddress.toString());
    }

    /**
     * Broadcast a message to clients
     * 
     * @param msg
     *          {@link Nonnull} message object
     * @param clientMap
     *          {@link Nonnull} map of clients information
     */
    public void broadcastMessage(@Nonnull Serializable msg, @Nonnull ConcurrentMap<Integer, ClientInfo> clientMap) {
      Preconditions.checkState(msg != null);
      Preconditions.checkState(clientMap != null);

      Iterator<Entry<Integer, ClientInfo>> it = clientMap.entrySet().iterator();
      while (it.hasNext()) {
        Entry<Integer, ClientInfo> entry = it.next();
        SocketAddress remoteAddress = entry.getValue().getClientAddress();
        SocketAddress localAddress = entry.getValue().getLocalAddressForClient();
        if (!sendMessage(msg, remoteAddress, localAddress)) {
          it.remove();
          logger.info(String.format("Removed client %s since connection cannot be established in %d seconds.",
              remoteAddress.toString(), Consts.CONN_TIME_LMT_SEC));
        }
      }
    }

    /**
     * Send a message to the given address through UDP channel then close the channel immediately
     * 
     * @param msg
     *          {@link Nonnull} message object
     * @param remoteAddress
     *          {@link Nonnull} remote address to which message will be sent
     * @return whether the message has been sent successfully
     */
    public boolean sendMessage(@Nonnull Serializable msg, @Nonnull SocketAddress remoteAddress) {
      return sendMessage(msg, remoteAddress, null, CLOSE_CHANNEL_SYNC);
    }

    /**
     * Send a message to the given address through UDP channel then close the channel immediately
     * 
     * @param msg
     *          {@link Nonnull} message object
     * @param remoteAddress
     *          {@link Nonnull} remote address to which message will be sent
     * @param localAddress
     *          {@link Nullable} the local address to use to send this message, if null this address is auto-determined
     * @return whether the message has been sent successfully
     */
    public boolean sendMessage(@Nonnull Serializable msg, @Nonnull SocketAddress remoteAddress,
        @Nonnull SocketAddress localAddress) {
      return sendMessage(msg, remoteAddress, localAddress, CLOSE_CHANNEL_SYNC);
    }

    /**
     * Send a {@link Message} to the remote address
     * 
     * @param msg
     *          {@link Nonnull} the message body
     * @param remoteAddress
     *          {@link Nonnull} the remote address to send message to
     * @param localAddress
     *          {@link Nullable} the local address to use to send this message, if null this address is auto-determined
     * @param onMessageSent
     *          {@link Nullable} a {@link ChannelFutureListener} which defines what to do after the message has been
     *          sent successfully, if null then do nothing
     * @return true if message has been sent successfully, otherwise false
     */
    protected boolean sendMessage(@Nonnull Serializable msg, @Nonnull SocketAddress remoteAddress,
        @Nonnull SocketAddress localAddress, @Nullable ChannelFutureListener onMessageSent) {
      Preconditions.checkNotNull(msg);
      Preconditions.checkNotNull(remoteAddress);

      ChannelFuture channelFuture = bootstrap.connect(remoteAddress, localAddress);
      if (channelFuture.awaitUninterruptibly(Consts.CONN_TIME_LMT_SEC, TimeUnit.SECONDS)) {
        Channel channel = channelFuture.getChannel();
        if (onMessageSent != null) {
          channel.write(msg).addListener(onMessageSent);
        }
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
        messageQueue.offer(new Message(e.getMessage(), e.getRemoteAddress()));
      }

      @Override
      public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        logger.warn("exceptionCaught: ", e.getCause());
      }
    }
  }

  /**
   * Create and start a server instance
   * 
   * @param port
   * @return
   */
  public static AbstractApplication startServerApplication(int port) {
    AbstractApplication app = new DefaultServerApplication(port);
    app.start(Type.Headless);
    return app;
  }

  /**
   * Create and start a client instance
   * 
   * @param serverHost
   *          {@link Nonnull}
   * @param serverPort
   * @return
   */
  public static AbstractApplication startClientApplication(@Nonnull String serverHost, int serverPort) {
    GameConfig config = Utils.getGameConfig();
    AbstractApplication app = new DefaultClientApplication(serverHost, serverPort);
    AppSettings settings = new AppSettings(true);
    settings.setResolution(config.getScreenWidth(), config.getScreenHeight());
    settings.setSamples(config.getSamples());
    settings.setFullscreen(config.isFullscreen());
    settings.setVSync(config.isVSync());
    settings.setTitle("ROFL");
    app.setSettings(settings);
    app.setShowSettings(false);
    app.setPauseOnLostFocus(false);
    app.start();

    return app;
  }
}
