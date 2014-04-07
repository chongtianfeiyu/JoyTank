package com.joytank.game;

import java.io.Serializable;

import org.apache.log4j.Logger;

import com.jme3.animation.AnimControl;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 * 
 * @author lizhaoliu
 *
 */
public class Player implements Serializable {

	private static final long serialVersionUID = 1361848341433289227L;
	private static final Logger LOGGER = Logger.getLogger(Player.class);

	private final Node node;
	private final CharacterControl characterControl;
	private final AnimControl animControl;

	private transient Vector3f movementDestination;

	/**
	 * 
	 * @return
	 */
	public Vector3f getMovementDestination() {
		return movementDestination;
	}

	/**
	 * 
	 * @param movementDestination
	 */
	public void setMovementDestination(Vector3f movementDestination) {
		this.movementDestination = movementDestination;
	}

	/**
	 * Stop movement
	 */
	public void stop() {
		characterControl.setWalkDirection(Vector3f.ZERO);
	}

	/**
	 * Move the character on plane y = 0
	 * 
	 * @param movementDestination
	 */
	public void move(Vector3f movementDestination) {
		Vector3f loc = characterControl.getPhysicsLocation();
		Vector3f dir = movementDestination.subtract(loc);
		dir.y = 0;
		dir.normalizeLocal();
		characterControl.setWalkDirection(dir);
		characterControl.setViewDirection(dir);
		this.movementDestination = movementDestination.clone();
	}

	/**
	 * 
	 * 
	 * @param node
	 * @param characterControl
	 * @param animControl
	 */
	public Player(Node node, CharacterControl characterControl, AnimControl animControl) {
		super();
		this.node = node;
		this.characterControl = characterControl;
		this.animControl = animControl;
	}
}
