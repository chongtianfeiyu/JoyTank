package com.joytank.game;

import com.google.common.base.Preconditions;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;

/**
 * 
 */
public abstract class AbstractEntity {
	
	protected Spatial spatial;
	
	public AbstractEntity(Spatial spatial) {
	  setSpatial(spatial);
  }

	/**
	 * 
	 * 
	 * @return
	 */
	public Spatial getSpatial() {
		return spatial;
	}

	/**
	 * 
	 * 
	 * @param spatial
	 */
	public void setSpatial(Spatial spatial) {
		Preconditions.checkState(spatial != null);
		this.spatial = spatial;
	}
	
	/**
	 * 
	 * 
	 * @param controlClass
	 * @return
	 */
	public <T extends Control> Control getControl(Class<T> controlClass) {
		return spatial.getControl(controlClass);
	}
}
