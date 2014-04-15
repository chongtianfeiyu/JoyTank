package com.joytank.net.lobby;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executors;

import javax.annotation.Nonnull;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.ClassResolvers;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

import com.google.common.base.Preconditions;
import com.joytank.game.GameConfig;

/**
 * 
 */
public class LobbyServer {
	private static final Logger logger = Logger.getLogger(LobbyServer.class);

	private TcpComponent tcpComponent;
	private ServerDesc[] serverDescs;

	public LobbyServer(@Nonnull GameConfig config) {
		Preconditions.checkState(config != null);

		tcpComponent = new TcpComponent(config.getLobbyPort());
		tcpComponent.setup();
	}

	public void start() {
		try {
			serverDescs = new ObjectMapper().readValue(ClassLoader.getSystemResourceAsStream("servers.json"),
			    ServerDesc[].class);
		} catch (IOException e) {
			logger.warn("IOException: ", e);
		}
	}

	/**
   * The TCP networking component
   */
	private class TcpComponent {
		private ServerBootstrap bootstrap;

		private final SocketAddress lobbyAddress;

		public TcpComponent(int port) {
			lobbyAddress = new InetSocketAddress(port);
		}

		public void setup() {
			ChannelFactory channelFactory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
			    Executors.newCachedThreadPool());
			bootstrap = new ServerBootstrap(channelFactory);
			final ChannelHandler channelHandler = new MessageHandler();
			final ObjectDecoder objDecoder = new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(null));
			final ObjectEncoder objEncoder = new ObjectEncoder();
			bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
				@Override
				public ChannelPipeline getPipeline() throws Exception {
					return Channels.pipeline(objDecoder, objEncoder, channelHandler);
				}
			});
			bootstrap.bind(lobbyAddress);
		}

		private class MessageHandler extends SimpleChannelHandler {

			@Override
			public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
				Object msg = e.getMessage();
				if (msg instanceof RefreshRequest) {
					RefreshResponse response = new RefreshResponse(serverDescs);
					e.getChannel().write(response);
				}
				if (msg instanceof JoinPlayRequest) {
					SocketAddress remoteAddress = e.getRemoteAddress();
					JoinPlayResponse response = new JoinPlayResponse(remoteAddress, true);
					e.getChannel().write(response);
				}
			}

			@Override
			public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
				logger.warn("exceptionCaught: " + e.getCause());
			}
		}
	}
}
