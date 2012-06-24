package com.sonicmetrics.core.shared.query;

import com.sonicmetrics.core.shared.ISonicEvent;


/**
 * Listens
 * 
 * @author xamde
 */
public interface ISonicListener {
	
	/**
	 * @param sonicEvent to be received, may have a key defined which was
	 *            obtained via {@link #setKey(ISonicEvent)} in the same
	 *            implementation
	 */
	void receiveEvent(ISonicEvent sonicEvent);
	
	/**
	 * Set a key string as it is used in the backend.
	 * 
	 * @param sonicEvent without a key
	 */
	void setKey(ISonicEvent sonicEvent);
	
}
