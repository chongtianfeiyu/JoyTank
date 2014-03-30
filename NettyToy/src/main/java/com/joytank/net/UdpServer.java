package com.joytank.net;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * 
 * @author lizhaoliu
 * 
 */
public class UdpServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(UdpServer.class);

  private final int port;

  private UdpServerChannelHandler channelHandler;
  private ConnectionlessBootstrap bootstrap;

  public UdpServer(int port) {
    Preconditions.checkState(port >= Consts.PORT_MIN && port <= Consts.PORT_MAX, "port is not in range (1023, 65536)");
    this.port = port;
  }

  public void run() {
    ChannelFactory channelFactory = new NioDatagramChannelFactory(Executors.newCachedThreadPool());
    bootstrap = new ConnectionlessBootstrap(channelFactory);
    channelHandler = new UdpServerChannelHandler(bootstrap);
    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline() throws Exception {
        return Channels.pipeline(new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)),
            new ObjectEncoder(), channelHandler);
      }
    });
    bootstrap.bind(new InetSocketAddress(port));
    LOGGER.info("Server bound to " + port);
  }
}
