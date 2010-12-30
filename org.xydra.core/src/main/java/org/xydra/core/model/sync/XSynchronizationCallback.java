package org.xydra.core.model.sync;

/**
 * TODO document when stable
 * 
 * @author dscharrer
 * 
 */
public interface XSynchronizationCallback {
	
	void onRequestError(Throwable t);
	
	void onCommandErrror(Throwable t);
	
	void onEventsError(Throwable t);
	
	void onSuccess();
	
}
