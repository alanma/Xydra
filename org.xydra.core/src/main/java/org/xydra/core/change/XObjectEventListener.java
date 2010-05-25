package org.xydra.core.change;

/**
 * The basic interface for ChangeListeneres that listens for changes in an
 * XObject.
 * 
 * @author Kaidel
 */

public interface XObjectEventListener {
	
	/**
	 * A single event
	 * 
	 * @param event
	 */
	void onChangeEvent(XObjectEvent event);
}
