package org.xydra.core.model;

import org.xydra.annotations.ReadOperation;
import org.xydra.base.XID;
import org.xydra.base.XHalfWritableModel;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XSendsFieldEvents;
import org.xydra.core.change.XSendsModelEvent;
import org.xydra.core.change.XSendsObjectEvents;
import org.xydra.core.change.XSendsTransactionEvents;


/**
 * A logged model that at least supports read operations and sends
 * {@link XEvent XEvents} if something is changed.
 * 
 * <h4>Design note</h4> Support for read operations would be enough, but this
 * leads to a problem with polymorphic methods in Java. Given interface B1 with
 * method B2 foo() and interface C1 with method C2 foo() can not be implemented
 * in Java with class A1 and method A2 foo() where A2 implements B2 and C2.
 * 
 * @author dscharrer
 */
public interface XLoggedModel extends XHalfWritableModel, XSendsModelEvent, XSendsObjectEvents,
        XSendsFieldEvents, XSendsTransactionEvents, IHasChangeLog {
	
	/**
	 * Returns the {@link XLoggedObject} contained in this model with the given
	 * {@link XID}
	 * 
	 * @param id The {@link XID} of the {@link XLoggedObject} which is to be
	 *            returned
	 * @return The {@link XLoggedObject} with the given {@link XID} or null, if
	 *         no corresponding {@link XLoggedObject} exists
	 */
	@ReadOperation
	XLoggedObject getObject(XID objectId);
	
}
