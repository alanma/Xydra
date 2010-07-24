package org.xydra.core.model.session;

import org.xydra.core.change.XEvent;
import org.xydra.core.change.XTransaction;
import org.xydra.core.model.IHasXAddress;
import org.xydra.core.model.XChangeLog;


/**
 * An interface that indicates that this entity is able to execute
 * {@link XTransaction XTransactions} and synchronize remote changes.
 * 
 * @author dscharrer
 * 
 */
public interface XProtectedSynchronizesChanges extends IHasXAddress, XProtectedExecutesCommands,
        XProtectedExecutesTransactions {
	
	/**
	 * @return the {@link XChangeLog} which is logging the {@link XEvent
	 *         XEvents} which happen on this model or object.
	 */
	XChangeLog getChangeLog();
	
	/*
	 * TODO Why does this interface miss almost all methods provided by the
	 * normal XSynchronizesChangesInterface?
	 */

}
