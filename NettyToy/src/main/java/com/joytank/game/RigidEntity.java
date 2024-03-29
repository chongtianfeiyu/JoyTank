package com.joytank.game;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.UrlLocator;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.scene.Spatial;
import com.joytank.net.game.Consts;

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

  /**
   * 
   * @param modelPath
   * @param mass
   * @param assetManager
   * @return
   */
  public static RigidEntity loadWithMeshCollisionShape(@Nonnull String modelPath, float mass,
      @Nonnull AssetManager assetManager) {
    Preconditions.checkState(!StringUtils.isBlank(modelPath));
    Preconditions.checkState(assetManager != null);
    
    assetManager.registerLocator(Consts.ASSETS_BASE_URL, UrlLocator.class);
    Spatial spatial = assetManager.loadModel(modelPath);
    CollisionShape collisionShape = CollisionShapeFactory.createMeshShape(spatial);
    RigidEntity re = new RigidEntity(spatial, collisionShape, mass);
    return re;
  }

  @Override
  public PhysicsControl getPhysicsControl() {
    return rigidBodyControl;
  }
}
