package com.joytank.game;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;

import com.google.common.collect.Maps;
import com.joytank.net.game.ClientInfo;
import com.joytank.net.game.Consts;
import com.joytank.net.game.JoinRequest;
import com.joytank.net.game.JoinResponse;
import com.joytank.net.game.Message;
import com.joytank.net.game.Ping;
import com.joytank.net.game.PlayerMotionMsg;

/**
 * 
 */
public class DefaultServerApplication extends AbstractApplication {

  private static final Logger logger = Logger.getLogger(DefaultServerApplication.class);

  protected final ConcurrentMap<Integer, ClientInfo> clientInfoMap = Maps.newConcurrentMap();

  protected float timerMillis;

  protected volatile boolean isDcDetectorRunning;

  public DefaultServerApplication(int localPort) {
    super(localPort);
  }

  @Override
  protected void initAll() {
    udpComponent.bind();

    // Start disconnection detector
    startDisconnectionDetector();
  }

  @Override
  protected void handleMessage(Message message) {
    Object msg = message.getMessageObject();
    if (msg instanceof JoinRequest) {
      handleJoinRequest((JoinRequest) msg, message.getRemoteAddress());
    }
    if (msg instanceof Ping) {
      handlePingMsg((Ping) msg);
    }
    if (msg instanceof PlayerMotionMsg) {
      handlePlayerMotionMsg((PlayerMotionMsg) msg);
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

  protected void handlePingMsg(Ping msg) {
    ClientInfo info = clientInfoMap.get(msg.getClientId());
    if (info != null) {
      SocketAddress remoteAddress = info.getClientAddress();
      info.setTimeStamp(System.currentTimeMillis());
      udpComponent.sendMessage(msg, remoteAddress);
    }
  }

  /**
   * This is the first message from a client
   * 
   * @param msg
   * @param remoteAddress
   */
  protected void handleJoinRequest(JoinRequest msg, SocketAddress remoteAddress) {
    int newClientId = createUniqueId(remoteAddress);
    logger.info(String.format("Got join request from '%s', accpeted and assign ID: %d", remoteAddress, newClientId));

    // Add new client info
    ClientInfo info = new ClientInfo(remoteAddress, System.currentTimeMillis());
    clientInfoMap.putIfAbsent(newClientId, info);

    // Add a new player entry
    Player newPlayer = Player.loadWithCapsuleCollisionShape("models/Oto/Oto.mesh.xml", assetManager);
    addToGame(newPlayer);
    playerMap.putIfAbsent(newClientId, newPlayer);

    // Broadcast a join response
    JoinResponse msgBack = new JoinResponse(newClientId, createGameState());
    udpComponent.broadcastMessage(msgBack, clientInfoMap);
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

  private int createUniqueId(SocketAddress clientAddress) {
    int newId = 0;
    while (newId == 0) {
      newId = (RandomUtils.nextInt() * 31 + clientAddress.hashCode()) & 0x7fffffff;
    }
    return newId;
  }

  private void startDisconnectionDetector() {
    if (isDcDetectorRunning) {
      return;
    }

    isDcDetectorRunning = true;
    ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
      @Override
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("DisconnectionDetector");
        return t;
      }
    });
    exec.scheduleAtFixedRate(new DisconnectionDetector(), 0, Consts.DC_DETECTION_INTERVAL_SEC, TimeUnit.SECONDS);
  }

  /**
   * Task that detects client disconnection
   */
  private class DisconnectionDetector implements Runnable {

    @Override
    public void run() {
      removeDisconnectedClients();
    }

    private void removeDisconnectedClients() {
      long now = System.currentTimeMillis();
      Iterator<Entry<Integer, ClientInfo>> it = clientInfoMap.entrySet().iterator();
      while (it.hasNext()) {
        Entry<Integer, ClientInfo> entry = it.next();
        ClientInfo info = entry.getValue();
        int clientId = entry.getKey();
        if ((now - info.getTimeStamp()) > Consts.DISCONNECT_THRESHOLD_MILLIS) {
          it.remove();
          playerMap.remove(clientId);
          logger.info(String.format("Removed disconnected client, ID = %d.", clientId));
        }
      }
    }
  }
}
