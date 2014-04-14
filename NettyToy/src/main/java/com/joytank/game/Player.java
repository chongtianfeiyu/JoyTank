package com.joytank.game;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;
import com.jme3.asset.AssetManager;
import com.jme3.asset.plugins.UrlLocator;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.joytank.net.Consts;

/**
 * 
 */
public class Player extends AbstractEntity {

  private CharacterControl characterControl;
  private Vector3f movementDestination;
  private float moveSpeed;

  public Player(Spatial spatial, CollisionShape collisionShape) {
    super(spatial);

    Preconditions.checkState(collisionShape != null);
    this.characterControl = new CharacterControl(collisionShape, 0.05f);
    this.characterControl.setGravity(9.8f);
    this.characterControl.setFallSpeed(9.8f);
    this.characterControl.setJumpSpeed(9.8f);
    this.characterControl.setPhysicsLocation(spatial.getLocalTranslation());
    this.spatial.addControl(characterControl);
    this.movementDestination = characterControl.getPhysicsLocation().clone();
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
   * @param modelPath
   *          {@link Nonnull} path of model file
   * @param assetManager
   *          {@link Nonnull} asset manager that loads the model
   * @return
   */
  public static Player loadWithCapsuleCollisionShape(@Nonnull String modelPath, @Nonnull AssetManager assetManager) {
    Preconditions.checkState(!StringUtils.isBlank(modelPath));
    Preconditions.checkState(assetManager != null);

    assetManager.registerLocator(Consts.ASSETS_BASE_URL, UrlLocator.class);
    Spatial spatial = assetManager.loadModel(modelPath);
    BoundingBox bb = (BoundingBox) spatial.getWorldBound();

    spatial.move(0, bb.getYExtent(), 0);

    CollisionShape collisionShape = new CapsuleCollisionShape(Math.max(bb.getXExtent(), bb.getZExtent()) * 0.5f,
        bb.getYExtent(), 1);

    return new Player(spatial, collisionShape);
  }

  @Override
  public PhysicsControl getPhysicsControl() {
    return characterControl;
  }
}
