package org.xydra.core.access;

/**
 * A listener that listens for {@link XGroupEvents}
 * 
 * @author dscharrer
 * 
 */
public interface XGroupListener {
	
	void onGroupEvent(XGroupEvent event);
	
}
