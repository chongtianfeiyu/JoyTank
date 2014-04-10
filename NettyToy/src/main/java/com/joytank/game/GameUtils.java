package com.joytank.game;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.scene.Spatial;

/**
 * 
 */
public final class GameUtils {

  /**
   * Load a character type model and assign it with a {@link CharacterControl}
   * 
   * @param zipFilePath
   * @param modelName
   * @param assetManager
   * @return
   */
  public static Spatial loadPlayer(@Nonnull String zipFilePath, @Nonnull String modelName,
      @Nonnull AssetManager assetManager) {
    Preconditions.checkState(!StringUtils.isBlank(zipFilePath));
    Preconditions.checkState(!StringUtils.isBlank(modelName));
    Preconditions.checkState(assetManager != null);

    // Load model file
    assetManager.registerLocator(zipFilePath, ZipLocator.class);
    Spatial model = assetManager.loadModel(modelName);
    BoundingBox bb = (BoundingBox) model.getWorldBound();
    model.move(0, bb.getYExtent(), 0);

    // Assign the model with CharacterControl
    CollisionShape actorShape = new CapsuleCollisionShape(Math.max(bb.getXExtent(), bb.getZExtent()) * 0.5f,
        bb.getYExtent(), 1);
    CharacterControl characterControl = new CharacterControl(actorShape, 0.05f);
    characterControl.setJumpSpeed(10);
    characterControl.setFallSpeed(10);
    characterControl.setGravity(9.8f);
    characterControl.setPhysicsLocation(model.getLocalTranslation());
    model.addControl(characterControl);

    return model;
  }

  /**
   * Load a rigid body type model and assign it with a {@link RigidBodyControl}
   * 
   * @param zipFilePath
   * @param modelName
   * @param mass
   * @param assetManager
   * @return
   */
  public static Spatial loadRigidBody(String zipFilePath, String modelName, float mass, AssetManager assetManager) {
    Preconditions.checkState(!StringUtils.isBlank(zipFilePath));
    Preconditions.checkState(!StringUtils.isBlank(modelName));
    Preconditions.checkState(assetManager != null);

    assetManager.registerLocator(zipFilePath, ZipLocator.class);
    Spatial model = assetManager.loadModel(modelName);

    // Assign the model with RigidBodyControl
    CollisionShape collisionShape = CollisionShapeFactory.createMeshShape(model);
    RigidBodyControl rigidBodyControl = new RigidBodyControl(collisionShape, mass);
    model.addControl(rigidBodyControl);

    return model;
  }

  private GameUtils() {
  }
}
