package com.joytank.game;

import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.scene.Spatial;

public class RigidEntity extends AbstractEntity {

	private float mass;
	private RigidBodyControl rigidBodyControl;
	
	public RigidEntity(Spatial spatial, CollisionShape collisionShape, float mass) {
		super(spatial);
		this.mass = mass;
		this.rigidBodyControl = new RigidBodyControl(collisionShape, mass);
		this.spatial.addControl(rigidBodyControl);
	}

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

	public static RigidEntity loadWithMeshCollisionShape(String zipPath, String modelFile, float mass, AssetManager assetManager) {
		assetManager.registerLocator(zipPath, ZipLocator.class);
		Spatial spatial = assetManager.loadModel(modelFile);
		CollisionShape collisionShape = CollisionShapeFactory.createMeshShape(spatial);
		RigidEntity re = new RigidEntity(spatial, collisionShape, mass);
		return re;
	}

  @Override
  public PhysicsControl getPhysicsControl() {
    return rigidBodyControl;
  }
}
