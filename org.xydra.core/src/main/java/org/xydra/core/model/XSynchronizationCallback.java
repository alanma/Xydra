package org.xydra.core.model;

import org.xydra.core.change.XCommand;


/**
 * An interface to notify if re-applying local {@link XCommand XCommands} failed
 * during synchronizing an {@link XModel}/{@link XObject}.
 * 
 * @author dscharrer
 * 
 */
public interface XSynchronizationCallback {
	
	void failed();
	
}
