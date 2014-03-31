package com.joytank.net;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.Lists;
import com.joytank.game.ActorStatus;

public class AllPlayerInfo implements Serializable {
  private static final long serialVersionUID = -4877223774411448006L;

  private final List<ActorStatus> playerEssences = Lists.newArrayList();

  public List<ActorStatus> getPlayerEssences() {
    return playerEssences;
  }
}
