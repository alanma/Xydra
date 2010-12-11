package org.xydra.core.model.sync;

import org.xydra.core.model.XSynchronizationCallback;


public interface XCommandCallback extends XSynchronizationCallback {
	
	/**
	 * Like {@link #failed()}, but called after all other commands in the queue
	 * have been re-applied to the local model.
	 */
	public void failedPost();
	
	public void applied(long revision);
	
}
