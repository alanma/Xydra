package org.xydra.core.change;

/**
 * The basic interface for ChangeListeneres that listens for changes in an
 * XRepository.
 * 
 * @author Kaidel
 */

public interface XRepositoryEventListener {
	
	/**
	 * A single event
	 * 
	 * @param event
	 */
	void onChangeEvent(XRepositoryEvent event);
}
