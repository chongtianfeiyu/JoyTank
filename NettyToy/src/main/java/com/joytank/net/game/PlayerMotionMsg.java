package com.joytank.net.game;

import java.io.Serializable;

import com.jme3.math.Vector3f;

public class PlayerMotionMsg implements Serializable {
	private static final long serialVersionUID = 2196791557505482846L;

	private final int clientId;
	private final Vector3f dst;

	public int getClientId() {
		return clientId;
	}

	public Vector3f getDst() {
		return dst;
	}

	public PlayerMotionMsg(int clientId, Vector3f dst) {
		super();
		this.clientId = clientId;
		this.dst = dst;
	}
}
