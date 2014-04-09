package com.joytank.net;

import java.io.Serializable;

/**
 * 
 * @author lizhaoliu
 *
 */
public class AbstractMessage implements Serializable {
	private static final long serialVersionUID = 3920015618848407681L;

	protected final long timeStamp;

	/**
	 * Construct a message with a time stamp
	 * 
	 * @param timeStamp
	 */
	public AbstractMessage(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	/**
	 * Get the time stamp of this message
	 * 
	 * @return
	 */
	public long getTimeStamp() {
		return timeStamp;
	}
}
