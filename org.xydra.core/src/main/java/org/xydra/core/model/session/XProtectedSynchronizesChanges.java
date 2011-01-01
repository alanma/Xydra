package org.xydra.core.model.session;

import org.xydra.core.change.XEvent;
import org.xydra.core.change.XTransaction;
import org.xydra.core.model.IHasChangeLog;
import org.xydra.core.model.IHasXAddress;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XID;


/**
 * An interface that indicates that this entity is able to execute
 * {@link XTransaction XTransactions} and synchronize remote changes.
 * 
 * @author dscharrer
 * 
 */
public interface XProtectedSynchronizesChanges extends IHasXAddress, IHasChangeLog,
        XProtectedExecutesCommands {
	
	/**
	 * @return the {@link XChangeLog} which is logging the {@link XEvent
	 *         XEvents} which happen on this model or object.
	 */
	XChangeLog getChangeLog();
	
	/*
	 * TODO Why does this interface miss almost all methods provided by the
	 * normal XSynchronizesChangesInterface?
	 */

	/**
	 * @return the actor that is represented by this interface. This is the
	 *         actor that is recorded for change operations. Operations will
	 *         only succeed if this actor has access.
	 */
	XID getActor();
	
}
