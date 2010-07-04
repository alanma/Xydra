package org.xydra.core.model;

/**
 * An interface to notify of local commands that failed to re-apply when
 * synchronizing a model/object.
 * 
 * @author dscharrer
 * 
 */
public interface XSynchronizationCallback {
	
	void failed();
	
}
