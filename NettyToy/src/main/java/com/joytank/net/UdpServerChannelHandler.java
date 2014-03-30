package com.joytank.net;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author lizhaoliu
 * 
 */
public class UdpServerChannelHandler extends SimpleChannelHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(UdpServerChannelHandler.class);

  private final ConnectionlessBootstrap bootstrap;

  private final Set<SocketAddress> remoteAddresses = Collections
      .synchronizedSet(new HashSet<SocketAddress>());

  public UdpServerChannelHandler(ConnectionlessBootstrap bootstrap) {
    this.bootstrap = bootstrap;
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    LOGGER.info("***messageReceived***");
    Object msgObj = e.getMessage();
    if (msgObj instanceof PingMsg) {
      PingMsg pingMsg = (PingMsg) msgObj;
      SocketAddress remoteAddress = pingMsg.getRemoteAddress();
      ChannelFuture channelFuture = bootstrap.connect(remoteAddress);
      if (channelFuture.awaitUninterruptibly(Consts.CONN_TIME_LMT_SEC, TimeUnit.SECONDS)) {
        Channel channel = channelFuture.getChannel();
        channel.write(pingMsg);
      } else {
        LOGGER.info(String.format("Cannot connect to %s within %d second(s), drop %s.",
            remoteAddress, Consts.CONN_TIME_LMT_SEC, pingMsg.toString()));
      }
    }
    super.messageReceived(ctx, e);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    LOGGER.warn("exceptionCaught: ", e.getCause());
    super.exceptionCaught(ctx, e);
  }
}
