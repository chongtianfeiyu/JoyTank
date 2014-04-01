package com.joytank.net;

import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;
import com.joytank.game.PlayerStatus;

public class PlayerStatusMap implements Serializable {
  private static final long serialVersionUID = -4877223774411448006L;

  private final ConcurrentMap<Integer, PlayerStatus> actorsStatus = Maps.newConcurrentMap();

  public void put(int clientId, PlayerStatus actorStatus) {
    actorsStatus.putIfAbsent(clientId, actorStatus);
  }
  
  public PlayerStatus get(int clientId) {
    return actorsStatus.get(clientId);
  }
  
  public ConcurrentMap<Integer, PlayerStatus> getInternalMap() {
    return actorsStatus;
  }
  
  public void remove(int clientId) {
    actorsStatus.remove(clientId);
  }
}
