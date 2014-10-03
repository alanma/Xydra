package com.sonicmetrics.core.shared.query;

import com.sonicmetrics.core.shared.ISonicEvent;

/**
 * Listens
 * 
 * @author xamde
 */
public interface ISonicListener {

	/**
	 * @param sonicEvent
	 *            to be received,
	 */
	void receiveEvent(ISonicEvent sonicEvent);

}
