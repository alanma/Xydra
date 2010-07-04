package org.xydra.client.sync;

import org.xydra.core.model.XSynchronizationCallback;


public interface XCommandCallback extends XSynchronizationCallback {
	
	/**
	 * TODO clarify
	 */
	public void failedPost();
	
	public void applied(long revision);
	
}
