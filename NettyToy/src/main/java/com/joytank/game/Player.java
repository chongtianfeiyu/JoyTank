package com.joytank.game;

import org.apache.log4j.Logger;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 * 
 */
public class Player {

	private static final Logger logger = Logger.getLogger(Player.class);

	private final Node node;
	private final CharacterControl characterControl;
	private final AnimControl animControl;
	private final AnimChannel animChannel;

	private Vector3f movementDestination;

	public Node getNode() {
		return node;
	}

	public CharacterControl getCharacterControl() {
		return characterControl;
	}

	public AnimControl getAnimControl() {
		return animControl;
	}

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
	 * @param threshold
	 */
	public void checkPosStop(float threshold) {
		Vector3f pos = characterControl.getPhysicsLocation();
		if (pos.distance(movementDestination) < threshold) {
			stop();
		}
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
		this.animControl.addListener(new AnimEventListener() {
			@Override
			public void onAnimCycleDone(AnimControl arg0, AnimChannel arg1, String arg2) {}

			@Override
			public void onAnimChange(AnimControl arg0, AnimChannel arg1, String arg2) {}
		});
		this.animChannel = animControl.createChannel();
		movementDestination = characterControl.getPhysicsLocation().clone();
	}

	/**
	 * 
	 * @param zipPath
	 * @param modelFile
	 * @param assetManager
	 * @return
	 */
	public static Player makePlayer(String zipPath, String modelFile, AssetManager assetManager) {
		assetManager.registerLocator(zipPath, ZipLocator.class);
		Node node = (Node) assetManager.loadModel(modelFile);
		BoundingBox bv = (BoundingBox) node.getWorldBound();

		node.move(0, bv.getYExtent(), 0);

		CollisionShape actorShape = new CapsuleCollisionShape(Math.max(bv.getXExtent(), bv.getZExtent()) * 0.5f,
		    bv.getYExtent(), 1);
		CharacterControl characterControl = new CharacterControl(actorShape, 0.05f);
		characterControl.setJumpSpeed(10);
		characterControl.setFallSpeed(10);
		characterControl.setGravity(9.8f);
		characterControl.setPhysicsLocation(node.getLocalTranslation());
		node.addControl(characterControl);

		AnimControl animControl = node.getControl(AnimControl.class);
		return new Player(node, characterControl, animControl);
	}
}
