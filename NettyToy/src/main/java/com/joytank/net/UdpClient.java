package com.joytank.net;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Random;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;
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
public class UdpClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(UdpClient.class);

  private final SocketAddress serverAddress;
  private final SocketAddress localAddress;

  private UdpClientChannelHandler channelHandler;

  /**
   * 
   * @param serverHostName
   * @param serverPort
   */
  public UdpClient(String serverHostName, int serverPort) {
    Preconditions.checkState(!StringUtils.isBlank(serverHostName),
        "serverHostName cannot be blank.");

    this.serverAddress = new InetSocketAddress(serverHostName, serverPort);
    this.localAddress = getLocalAddress();
    System.out.println(localAddress.toString());
  }

  public void run() {
    ChannelFactory channelFactory = new NioDatagramChannelFactory(Executors.newCachedThreadPool());
    ConnectionlessBootstrap bootstrap = new ConnectionlessBootstrap(channelFactory);
    channelHandler = new UdpClientChannelHandler();
    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline() throws Exception {
        return Channels.pipeline(
            new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null)),
            new ObjectEncoder(), channelHandler);
      }
    });
    bootstrap.bind(localAddress);
  }

  private int genRandomPort() {
    int port = 0;
    while (port < 1023 || port > 65535) {
      port = new Random().nextInt(65535);
    }
    return port;
  }

  private SocketAddress getLocalAddress() {
    try {
      Socket socket = new Socket("www.google.com", 80);
      String hostName = socket.getLocalAddress().getHostAddress();
      int port = genRandomPort();
      socket.close();
      return new InetSocketAddress(hostName, port);
    } catch (Exception e) {
      LOGGER.warn("exception: ", e);
    }
    return null;
  }
}
