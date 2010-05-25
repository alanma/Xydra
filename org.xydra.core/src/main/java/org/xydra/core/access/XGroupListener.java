package org.xydra.core.access;

/**
 * An object that is interested in changes to a group database.
 * 
 * @author dscharrer
 * 
 */
public interface XGroupListener {
	
	void onGroupEvent(XGroupEvent event);
	
}
