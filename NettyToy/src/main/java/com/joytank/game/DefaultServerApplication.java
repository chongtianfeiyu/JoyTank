package com.joytank.game;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.common.collect.Maps;
import com.joytank.net.game.ClientInfo;
import com.joytank.net.game.Consts;
import com.joytank.net.game.HeartBeat;
import com.joytank.net.game.JoinRequest;
import com.joytank.net.game.JoinResponse;
import com.joytank.net.game.Message;
import com.joytank.net.game.PingMsg;
import com.joytank.net.game.PlayerMotionMsg;

/**
 * 
 */
public class DefaultServerApplication extends AbstractApplication {

	private static final Logger logger = Logger.getLogger(DefaultServerApplication.class);

	protected final ConcurrentMap<Integer, ClientInfo> clientInfoMap = Maps.newConcurrentMap();

	protected float timerMillis;

	protected volatile boolean isRunningHeartBeatTask;

	public DefaultServerApplication(int localPort) {
		super(localPort);
	}

	@Override
	protected void initAll() {
		udpComponent.bind();
	}

	@Override
	protected void handleMessage(Message message) {
		Object msg = message.getMessageObject();
		if (msg instanceof JoinRequest) {
			handleJoinRequest((JoinRequest) msg, message.getRemoteAddress());
		}
		if (msg instanceof PingMsg) {
			handlePingMsg((PingMsg) msg);
		}
		if (msg instanceof PlayerMotionMsg) {
			handlePlayerMotionMsg((PlayerMotionMsg) msg);
		}
		if (msg instanceof HeartBeat) {
			handleHeartBeat((HeartBeat) msg);
		}
	}

	private void handleHeartBeat(HeartBeat msg) {
		int clientId = msg.getClientId();
		ClientInfo info = clientInfoMap.get(clientId);
		if (info != null) {
			info.setTimeStamp(System.nanoTime());
		}
	}

	@Override
	public void simpleUpdate(float tpf) {
		super.simpleUpdate(tpf);
		timerMillis += tpf * 1000;
		if (timerMillis > Consts.GAME_STATE_BROADCAST_INTERVAL_MILLIS) {
			udpComponent.broadcastMessage(createGameState(), clientInfoMap);
			timerMillis = 0;
		}
		for (Entry<Integer, Player> entry : playerMap.entrySet()) {
			Player player = entry.getValue();
			player.checkPosStop(5f);
		}
	}

	protected void handlePingMsg(PingMsg msg) {
		ClientInfo info = clientInfoMap.get(msg.getClientId());
		if (info != null) {
			SocketAddress remoteAddress = info.getClientAddress();
			udpComponent.sendMessage(msg, remoteAddress);
		}
	}

	protected void handleJoinRequest(JoinRequest msg, SocketAddress remoteAddress) {
		int newClientId = remoteAddress.hashCode();
		logger.info(String.format("Got join request from '%s', accpeted and assign ID: %d", remoteAddress, newClientId));

		// Add new client info
		ClientInfo info = new ClientInfo(remoteAddress, System.nanoTime());
		clientInfoMap.putIfAbsent(newClientId, info);

		// Add a new player entry
		Player newPlayer = Player.loadWithCapsuleCollisionShape("models/Oto/Oto.mesh.xml", assetManager);
		addToGame(newPlayer);
		playerMap.putIfAbsent(newClientId, newPlayer);

		// Broadcast a join response
		JoinResponse msgBack = new JoinResponse(newClientId, createGameState());
		udpComponent.broadcastMessage(msgBack, clientInfoMap);

		// Start heart beat task
		startHeartBeatTask();
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
		udpComponent.broadcastMessage(msg, clientInfoMap);
	}

	private void startHeartBeatTask() {
		if (isRunningHeartBeatTask) {
			return;
		}

		isRunningHeartBeatTask = true;
		ScheduledExecutorService exec = Executors.newScheduledThreadPool(1, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		});
		exec.scheduleAtFixedRate(new HeartBeatTask(), 0, Consts.HEART_BEAT_INTERVAL_SEC, TimeUnit.SECONDS);
	}

	/**
	 * 
	 */
	private class HeartBeatTask implements Runnable {

		@Override
		public void run() {
			if (isRunningHeartBeatTask) {
				removeInactiveClients();
				udpComponent.broadcastMessage(new HeartBeat(), clientInfoMap);
			}
		}

		private void removeInactiveClients() {
			long timeStamp = System.nanoTime();
			Iterator<Entry<Integer, ClientInfo>> it = clientInfoMap.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Integer, ClientInfo> entry = it.next();
				ClientInfo info = entry.getValue();
				int clientId = entry.getKey();
				if ((timeStamp - info.getTimeStamp()) / 1000000 > Consts.DISCONNECT_THRESHOLD_MILLIS) {
					it.remove();
					playerMap.remove(clientId);
					logger.info(String.format("Removed client with ID %d for being disconnected.", clientId));
				}
			}
		}
	}
}
