package com.joytank.game;

import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.scene.Node;

public class RigidEntity {

	private final Node node;

	private float mass;
	private RigidBodyControl rigidBodyControl;

	public float getMass() {
		return mass;
	}

	public void setMass(float mass) {
		this.mass = mass;
	}

	public RigidBodyControl getRigidBodyControl() {
		return rigidBodyControl;
	}

	public void setRigidBodyControl(RigidBodyControl rigidBodyControl) {
		this.rigidBodyControl = rigidBodyControl;
	}

	public Node getNode() {
		return node;
	}

	public RigidEntity(Node node) {
		super();
		this.node = node;
		setPhysics();
	}

	private void setPhysics() {
		CollisionShape collisionShape = CollisionShapeFactory.createMeshShape(node);
		rigidBodyControl = new RigidBodyControl(collisionShape, mass);
		node.addControl(rigidBodyControl);
	}

	public static RigidEntity make(String zipPath, String modelFile, float mass, AssetManager assetManager) {
		assetManager.registerLocator(zipPath, ZipLocator.class);
		Node node = (Node) assetManager.loadModel(modelFile);
		RigidEntity re = new RigidEntity(node);
		re.setMass(mass);
		return re;
	}
}
