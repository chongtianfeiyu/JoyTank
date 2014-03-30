package com.joytank.net;

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
public class UdpClientChannelHandler extends SimpleChannelHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(UdpClientChannelHandler.class);

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    LOGGER.info("***messageReceived***");
    super.messageReceived(ctx, e);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    LOGGER.warn("exceptionCaught: ", e.getCause());
    super.exceptionCaught(ctx, e);
  }
}
