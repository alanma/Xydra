package org.xydra.core.model.session;

import org.xydra.core.change.XEvent;
import org.xydra.core.model.IHasXAddress;
import org.xydra.core.model.XChangeLog;


public interface XProtectedSynchronizesChanges extends IHasXAddress, XProtectedExecutesCommands,
        XProtectedExecutesTransactions {
	
	/**
	 * @return the {@link XChangeLog} which is logging the {@link XEvent
	 *         XEvents} which happen on this model or object.
	 */
	XChangeLog getChangeLog();
	
}
