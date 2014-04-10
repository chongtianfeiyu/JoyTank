package com.joytank.game;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.google.common.collect.Maps;
import com.joytank.net.ClientInfo;
import com.joytank.net.JoinRequest;
import com.joytank.net.JoinResponse;
import com.joytank.net.PingMsg;
import com.joytank.net.PlayerMotionMsg;

/**
 * 
 */
public class DefaultServerApplication extends AbstractApplication {
	
	private static final Logger logger = Logger.getLogger(DefaultServerApplication.class);
	
	private static final int BROADCAST_INTERVAL_MILLIS = 150;

	protected final ConcurrentMap<Integer, ClientInfo> clientMap = Maps.newConcurrentMap();
	
	protected float timerMillis;

	public DefaultServerApplication(int localPort) {
		super(localPort);
	}

	@Override
	protected void initAll() {}

	@Override
	protected void handleMessage(Object msg) {
		if (msg instanceof JoinRequest) {
			handleJoinRequest((JoinRequest) msg);
		}
		if (msg instanceof PingMsg) {
			handlePingMsg((PingMsg) msg);
		}
		if (msg instanceof PlayerMotionMsg) {
			handlePlayerMotionMsg((PlayerMotionMsg) msg);
		}
	}
	
	@Override
	public void simpleUpdate(float tpf) {
	  super.simpleUpdate(tpf);
	  timerMillis += tpf;
	  if (timerMillis > BROADCAST_INTERVAL_MILLIS) {
	  	udpComponent.broadcastMsg(createGameState(), clientMap);
	  	timerMillis = 0;
	  }
	}

	protected void handlePingMsg(PingMsg msg) {
		ClientInfo info = clientMap.get(msg.getClientId());
		if (info != null) {
			SocketAddress remoteAddress = info.getClientAddress();
			udpComponent.sendMsg(msg, remoteAddress);
		}
	}
	
	protected void handleJoinRequest(JoinRequest msg) {
		int newClientId = clientMap.size();
		logger.info(String.format("Got join request from '%s', accpeted and assign ID: %d", msg.getAddress(), newClientId));
		
		// Add new client info
		ClientInfo info = new ClientInfo(msg.getAddress());
		clientMap.putIfAbsent(newClientId, info);
		
		// Add a new player entry
		Player newPlayer = Player.loadWithCapsuleCollisionShape("assets/models/Oto.zip", "main.scene", assetManager);
		addToGame(newPlayer);
		playerMap.putIfAbsent(newClientId, newPlayer);
		
		//broadcast a join response
		JoinResponse msgBack = new JoinResponse(newClientId, createGameState());
		udpComponent.broadcastMsg(msgBack, clientMap);
	}
	
	protected void handlePlayerMotionMsg(PlayerMotionMsg msg) {
	  Player player = playerMap.get(msg.getClientId());
		if (player == null) {
			logger.info("Player does not exist, ID: " + msg.getClientId());
			return;
		}
		
		// do motion logic
		player.move(msg.getDst());
		
		// Broadcast the message
		udpComponent.broadcastMsg(msg, clientMap);
	}
}
