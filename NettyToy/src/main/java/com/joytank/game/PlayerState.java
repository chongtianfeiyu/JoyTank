package com.joytank.game;

import java.io.Serializable;

import com.google.common.base.Preconditions;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;

/**
 * This is a snapshot type of the state of a player
 */
public final class PlayerState implements Serializable {
  private static final long serialVersionUID = 1664840104706689117L;

	private Vector3f location;
	private Vector3f walkViewDirection;

	public Vector3f getLocation() {
		return location;
	}

	public void setLocation(Vector3f location) {
		Preconditions.checkState(location != null);
		this.location = location;
	}

	public Vector3f getWalkViewDirection() {
		return walkViewDirection;
	}

	public void setWalkViewDirection(Vector3f walkViewDirection) {
		Preconditions.checkState(walkViewDirection != null);
		this.walkViewDirection = walkViewDirection;
	}

	public PlayerState(Spatial player) {
		CharacterControl characterControl = player.getControl(CharacterControl.class);
		setWalkViewDirection(characterControl.getWalkDirection());
		setLocation(characterControl.getPhysicsLocation());
	}
}
