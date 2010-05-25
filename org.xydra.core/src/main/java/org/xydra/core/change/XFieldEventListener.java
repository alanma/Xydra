package org.xydra.core.change;

/**
 * The basic interface for ChangeListeneres that listens for changes in an
 * XField.
 * 
 * @author Kaidel
 */

public interface XFieldEventListener {
	
	/**
	 * A single event
	 * 
	 * @param event
	 */
	void onChangeEvent(XFieldEvent event);
}
