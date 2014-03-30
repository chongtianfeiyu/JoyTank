package com.joytank.net;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.joytank.game.PlayerMotionToServer;

/**
 * 
 * @author lizhaoliu
 * 
 */
public class UdpServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(UdpServer.class);

  private static final int BORADCAST_INTERVAL_MILLIS = 10;

  private final int port;
  private final Set<SocketAddress> clients = Collections
      .synchronizedSet(new HashSet<SocketAddress>());

  private UdpServerChannelHandler channelHandler;
  private ConnectionlessBootstrap bootstrap;

  private boolean isServerRunning;

  public UdpServer(int port) {
    Preconditions.checkState(port >= Consts.PORT_MIN && port <= Consts.PORT_MAX,
        "port is not in range (1023, 65536)");
    this.port = port;
  }

  public void run() {
    ChannelFactory channelFactory = new NioDatagramChannelFactory(Executors.newCachedThreadPool());
    bootstrap = new ConnectionlessBootstrap(channelFactory);
    channelHandler = new UdpServerChannelHandler();
    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline() throws Exception {
        return Channels.pipeline(
            new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)),
            new ObjectEncoder(), channelHandler);
      }
    });
    bootstrap.bind(new InetSocketAddress(port));
    LOGGER.info("Server bound to " + port);
    Executors.newCachedThreadPool().execute(new ServerTask());
  }

  private void broadcastMsg(Object msg) {
    synchronized (clients) {
      Iterator<SocketAddress> it = clients.iterator();
      while (it.hasNext()) {
        SocketAddress address = it.next();
        if (!sendMsg(msg, address)) {
          it.remove();
          LOGGER.info(String.format(
              "Removed client %s since connection cannot be established in %d seconds.",
              address.toString(), Consts.CONN_TIME_LMT_SEC));
        }
      }
    }
  }

  private boolean sendMsg(Object msg, SocketAddress address) {
    ChannelFuture channelFuture = bootstrap.connect(address);
    if (channelFuture.awaitUninterruptibly(Consts.CONN_TIME_LMT_SEC, TimeUnit.SECONDS)) {
      Channel channel = channelFuture.getChannel();
      channel.write(msg);
      return true;
    } else {
      LOGGER.info(String.format("Cannot connect to %s within %d second(s).", address,
          Consts.CONN_TIME_LMT_SEC));
    }
    return false;
  }

  private class ServerTask implements Runnable {
    @Override
    public void run() {
      try {
        isServerRunning = true;
        while (isServerRunning) {
          Thread.sleep(BORADCAST_INTERVAL_MILLIS);
        }
      } catch (Exception e) {
        LOGGER.info("Exception: ", e);
      }
    }
  }

  private class UdpServerChannelHandler extends SimpleChannelHandler {

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
      LOGGER.info("***messageReceived***");
      Object msgObj = e.getMessage();
      if (msgObj instanceof PingMsg) {
        handlePingMsg((PingMsg) msgObj);
      } else if (msgObj instanceof PlayerMotionToServer) {
        handlePlayerMotion((PlayerMotionToServer) msgObj);
      }
      super.messageReceived(ctx, e);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
      LOGGER.warn("exceptionCaught: ", e.getCause());
      super.exceptionCaught(ctx, e);
    }

    private void handlePingMsg(PingMsg pingMsg) {
      SocketAddress remoteAddress = pingMsg.getRemoteAddress();
      clients.add(remoteAddress);
      ChannelFuture channelFuture = bootstrap.connect(remoteAddress);
      if (channelFuture.awaitUninterruptibly(Consts.CONN_TIME_LMT_SEC, TimeUnit.SECONDS)) {
        Channel channel = channelFuture.getChannel();
        channel.write(pingMsg);
      } else {
        clients.remove(remoteAddress);
        LOGGER.info(String.format("Cannot connect to %s within %d second(s), drop %s.",
            remoteAddress, Consts.CONN_TIME_LMT_SEC, pingMsg.toString()));
      }
    }

    private void handlePlayerMotion(PlayerMotionToServer playerMotionDto) {

    }

  }
}
