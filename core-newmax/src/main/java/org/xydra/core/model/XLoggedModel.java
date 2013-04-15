package org.xydra.core.model;

import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.XId;
import org.xydra.base.change.XEvent;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.core.change.XSendsFieldEvents;
import org.xydra.core.change.XSendsModelEvents;
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
public interface XLoggedModel extends XWritableModel, XSendsModelEvents, XSendsObjectEvents,
        XSendsFieldEvents, XSendsTransactionEvents, IHasChangeLog {
	
	/**
	 * Returns the {@link XLoggedObject} contained in this model with the given
	 * {@link XId}
	 * 
	 * @param id The {@link XId} of the {@link XLoggedObject} which is to be
	 *            returned
	 * @return The {@link XLoggedObject} with the given {@link XId} or null, if
	 *         no corresponding {@link XLoggedObject} exists
	 */
	@Override
    @ReadOperation
	XLoggedObject getObject(@NeverNull XId objectId);
	
}
