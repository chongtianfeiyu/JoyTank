package com.joytank.game;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
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
import com.google.common.collect.Queues;
import com.jme3.app.SimpleApplication;
import com.joytank.net.ClientInfo;
import com.joytank.net.Consts;
import com.joytank.net.NetUtils;

/**
 *  
 */
public abstract class AbstractApplication extends SimpleApplication {

  protected final SocketAddress localAddress;
  protected final UdpComponent udpComponent;
  
  protected final ConcurrentLinkedQueue<Object> messageQueue = Queues.newConcurrentLinkedQueue();

  /**
   * 
   * 
   * @param localPort
   */
  public AbstractApplication(int localPort) {
    super();
    Preconditions.checkState(localPort >= Consts.PORT_MIN && localPort <= Consts.PORT_MAX,
        "port is not in a valid range [1024, 65535].");
    this.localAddress = new InetSocketAddress(NetUtils.getLocalAddress(), localPort);
    this.udpComponent = new UdpComponent();
  }
  
  /**
   * 
   */
  public AbstractApplication() {
    super();
    this.localAddress = new InetSocketAddress(NetUtils.getLocalAddress(), NetUtils.generateRandomPort());
    this.udpComponent = new UdpComponent();
  }
  
  @Override
  public void simpleInitApp() {
    udpComponent.run();
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
   * Initialize everything here
   */
  abstract protected void initAll();

  /**
   * Handle a particular message here
   * 
   * @param msg
   */
  abstract protected void handleMessage(Object msg);

  /**
   * 
   * @author lizhaoliu
   * 
   */
  protected class UdpComponent {

    private final Logger logger = Logger.getLogger(UdpComponent.class);

    private UdpComponentHandler channelHandler;
    private ConnectionlessBootstrap bootstrap;

    /**
		 * 
		 */
    public void run() {
      ChannelFactory channelFactory = new NioDatagramChannelFactory(Executors.newCachedThreadPool());
      bootstrap = new ConnectionlessBootstrap(channelFactory);
      bootstrap.setOption("receiveBufferSizePredictorFactory", new FixedReceiveBufferSizePredictorFactory(
          Consts.UDP_PACKET_SIZE_MAX));
      channelHandler = new UdpComponentHandler();
      bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
        @Override
        public ChannelPipeline getPipeline() throws Exception {
          return Channels.pipeline(new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)),
              new ObjectEncoder(), channelHandler);
        }
      });
      bootstrap.bind(localAddress);
      logger.info("Bound to : " + localAddress.toString());
    }

    /**
     * 
     * @param msg
     */
    public void broadcastMsg(Object msg, ConcurrentMap<Integer, ClientInfo> clientsMap) {
      Iterator<Entry<Integer, ClientInfo>> it = clientsMap.entrySet().iterator();
      while (it.hasNext()) {
        Entry<Integer, ClientInfo> entry = it.next();
        SocketAddress address = entry.getValue().getAddress();
        if (!sendMsg(msg, address)) {
          it.remove();
          logger.info(String.format("Removed client %s since connection cannot be established in %d seconds.",
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
    public boolean sendMsg(Object msg, SocketAddress address) {
      Preconditions.checkState(msg != null);
      Preconditions.checkState(address != null);

      ChannelFuture channelFuture = bootstrap.connect(address);
      if (channelFuture.awaitUninterruptibly(Consts.CONN_TIME_LMT_SEC, TimeUnit.SECONDS)) {
        Channel channel = channelFuture.getChannel();
        channel.write(msg).addListener(ChannelFutureListener.CLOSE);
        return true;
      } else {
        logger.info(String.format("Cannot connect to %s within %d second(s).", address, Consts.CONN_TIME_LMT_SEC));
      }
      return false;
    }

    /**
		 * 
		 */
    private class UdpComponentHandler extends SimpleChannelHandler {

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
        logger.warn("exceptionCaught: ", e.getCause());
      }
    }
  }
}
