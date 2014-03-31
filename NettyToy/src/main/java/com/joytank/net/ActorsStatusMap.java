package com.joytank.net;

import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.Maps;
import com.joytank.game.ActorStatus;

public class ActorsStatusMap implements Serializable {
  private static final long serialVersionUID = -4877223774411448006L;

  private final ConcurrentMap<Integer, ActorStatus> actorsStatus = Maps.newConcurrentMap();

  public void put(int clientId, ActorStatus actorStatus) {
    actorsStatus.putIfAbsent(clientId, actorStatus);
  }
  
  public ActorStatus get(int clientId) {
    return actorsStatus.get(clientId);
  }
  
  public ConcurrentMap<Integer, ActorStatus> getInternalMap() {
    return actorsStatus;
  }
  
  public void remove(int clientId) {
    actorsStatus.remove(clientId);
  }
}
