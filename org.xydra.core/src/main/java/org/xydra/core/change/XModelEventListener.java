package org.xydra.core.change;

/**
 * The basic interface for ChangeListeneres that listens for changes in an
 * XModel.
 * 
 * @author Kaidel
 */

public interface XModelEventListener {
	
	/**
	 * A single event
	 * 
	 * @param event
	 */
	void onChangeEvent(XModelEvent event);
}
