package com.joytank.net;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author lizhaoliu
 * 
 */
public class UdpClientChannelHandler extends SimpleChannelHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(UdpClientChannelHandler.class);

  private int expectedPingId;
  private long nanoTimePingFired;

  public void setPingInfo(int expectedPingId, long nanoTimePingFired) {
    this.expectedPingId = expectedPingId;
    this.nanoTimePingFired = nanoTimePingFired;
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    LOGGER.info("***messageReceived***");
    Object msgObj = e.getMessage();
    if (msgObj instanceof PingMsg) {
      PingMsg pingMsg = (PingMsg) msgObj;
      if (pingMsg.getId() == expectedPingId) {
        long dTime = System.nanoTime() - nanoTimePingFired;
        LOGGER.info("Ping: " + dTime / 1000000);
      }
    }
    super.messageReceived(ctx, e);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    LOGGER.warn("exceptionCaught: ", e.getCause());
    super.exceptionCaught(ctx, e);
  }

  @Override
  public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e) throws Exception {
    LOGGER.info("***writeComplete***");
    super.writeComplete(ctx, e);
  }
}
