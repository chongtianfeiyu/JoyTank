package com.joytank.game;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;

import com.google.common.collect.Maps;
import com.joytank.net.game.ClientInfo;
import com.joytank.net.game.Consts;
import com.joytank.net.game.HeartBeat;
import com.joytank.net.game.JoinRequest;
import com.joytank.net.game.JoinResponse;
import com.joytank.net.game.PingMsg;
import com.joytank.net.game.PlayerMotionMsg;

/**
 * 
 */
public class DefaultServerApplication extends AbstractApplication {

	private static final Logger logger = Logger.getLogger(DefaultServerApplication.class);

	private static final int GAME_STATE_BROADCAST_INTERVAL_MILLIS = 300;

	protected final ConcurrentMap<Integer, ClientInfo> clientInfoMap = Maps.newConcurrentMap();

	protected float timerMillis;

	protected volatile boolean isRunningHeartBeatTask;

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
		if (timerMillis > GAME_STATE_BROADCAST_INTERVAL_MILLIS) {
			udpComponent.broadcastMsg(createGameState(), clientInfoMap);
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
			udpComponent.sendMsg(msg, remoteAddress);
		}
	}

	protected void handleJoinRequest(JoinRequest msg) {
		int newClientId = clientInfoMap.size();
		logger.info(String.format("Got join request from '%s', accpeted and assign ID: %d", msg.getAddress(), newClientId));

		// Add new client info
		ClientInfo info = new ClientInfo(msg.getAddress(), System.nanoTime());
		clientInfoMap.putIfAbsent(newClientId, info);

		// Add a new player entry
		Player newPlayer = Player.loadWithCapsuleCollisionShape("models/Oto/Oto.mesh.xml", assetManager);
		addToGame(newPlayer);
		playerMap.putIfAbsent(newClientId, newPlayer);

		// Broadcast a join response
		JoinResponse msgBack = new JoinResponse(newClientId, createGameState());
		udpComponent.broadcastMsg(msgBack, clientInfoMap);

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
		udpComponent.broadcastMsg(msg, clientInfoMap);
	}

	private void startHeartBeatTask() {
		if (isRunningHeartBeatTask) {
			return;
		}

		ExecutorService exec = Executors.newFixedThreadPool(1, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		});
		exec.execute(new HeartBeatTask());
	}

	/**
	 * 
	 */
	private class HeartBeatTask implements Runnable {

		@Override
		public void run() {
			try {
				isRunningHeartBeatTask = true;
				while (isRunningHeartBeatTask) {
					removeInactiveClients();
					udpComponent.broadcastMsg(new HeartBeat(), clientInfoMap);
					Thread.sleep(Consts.HEART_BEAT_INTERVAL_MILLIS);
				}
			} catch (Exception e) {
				logger.info("Exception: ", e);
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
