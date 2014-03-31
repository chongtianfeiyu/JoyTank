package com.joytank.net;

import java.awt.Point;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ConcurrentMap;
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
import com.google.common.collect.Maps;
import com.joytank.game.ActorStatus;
import com.joytank.game.PlayerMotionToServer;

/**
 * 
 * @author lizhaoliu
 * 
 */
public class UdpServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(UdpServer.class);

  private static final int BORADCAST_INTERVAL_MILLIS = 30;

  private final int port;
  private final ConcurrentMap<Integer, ClientInfo> clientsMap = Maps.newConcurrentMap();
  private final ActorsStatusMap actorsStatusMap = new ActorsStatusMap();

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

  private void broadcastActorsStatus() {
    broadcastMsg(actorsStatusMap);
  }

  private void broadcastMsg(Object msg) {
    Iterator<Entry<Integer, ClientInfo>> it = clientsMap.entrySet().iterator();
    while (it.hasNext()) {
      Entry<Integer, ClientInfo> entry = it.next();
      SocketAddress address = entry.getValue().getAddress();
      if (!sendMsg(msg, address)) {
        it.remove();
        actorsStatusMap.remove(entry.getKey());
        LOGGER.info(String.format(
            "Removed client %s since connection cannot be established in %d seconds.",
            address.toString(), Consts.CONN_TIME_LMT_SEC));
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
          // TODO broadcast all player info to all players
          broadcastActorsStatus();
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
      Object msgObj = e.getMessage();
      if (msgObj instanceof HelloMsg) {
        handleHelloMsg((HelloMsg) msgObj);
      } else if (msgObj instanceof PingMsg) {
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
      ClientInfo info = clientsMap.get(pingMsg.getClientId());
      if (info != null) {
        SocketAddress remoteAddress = info.getAddress();
        sendMsg(pingMsg, remoteAddress);
      }
    }

    private void handlePlayerMotion(PlayerMotionToServer playerMotionDto) {
      ActorStatus actorStatus = actorsStatusMap.get(playerMotionDto.getClientId());
      if (actorStatus != null) {
        actorStatus.setLocation(playerMotionDto.getDst());
      }
    }

    /**
     * 
     * @param helloMsg
     */
    private void handleHelloMsg(HelloMsg helloMsg) {
      ActorStatus actorStatus = new ActorStatus.Builder().withAngle(0)
          .withColor(new Random().nextInt(0xffffff)).withLocation(new Point(0, 0))
          .withSpeed(new Point()).build();
      ClientInfo info = new ClientInfo.Builder().withAddress(helloMsg.getAddress()).build();
      clientsMap.putIfAbsent(helloMsg.getClientId(), info);
      actorsStatusMap.put(helloMsg.getClientId(), actorStatus);
    }
  }
}
