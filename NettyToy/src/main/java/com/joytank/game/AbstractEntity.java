package com.joytank.game;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.PhysicsControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;

/**
 * 
 */
public abstract class AbstractEntity {

	protected Spatial spatial;

	/**
	 * 
	 * @param spatial
	 *          {@link Nonnull}
	 */
	public AbstractEntity(@Nonnull Spatial spatial) {
		setSpatial(spatial);
	}

	/**
	 * 
	 * 
	 * @return
	 */
	@Nonnull
	public Spatial getSpatial() {
		return spatial;
	}

	/**
	 * 
	 * 
	 * @param spatial
	 *          {@link Nonnull}
	 */
	public void setSpatial(Spatial spatial) {
		Preconditions.checkNotNull(spatial);

		this.spatial = spatial;
	}

	/**
	 * 
	 * 
	 * @param controlClass
	 * @return
	 */
	@Nullable
	public <T extends Control> T getControl(Class<T> controlClass) {
		return spatial.getControl(controlClass);
	}

	/**
	 * Get the physics control of the entity, e.g {@link CharacterControl} from {@link Player} and
	 * {@link RigidBodyControl} from {@link RigidEntity}
	 * 
	 * @return
	 */
	public abstract PhysicsControl getPhysicsControl();
}
