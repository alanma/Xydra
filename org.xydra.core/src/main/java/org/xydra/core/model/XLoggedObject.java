package org.xydra.core.model;

import org.xydra.annotations.ReadOperation;
import org.xydra.base.XID;
import org.xydra.base.XHalfWritableObject;
import org.xydra.core.change.XEvent;
import org.xydra.core.change.XSendsFieldEvents;
import org.xydra.core.change.XSendsObjectEvents;
import org.xydra.core.change.XSendsTransactionEvents;


/**
 * A logged object that at least supports read operations and sends
 * {@link XEvent XEvents} if something is changed.
 * 
 * <h4>Design note</h4> Support for read operations would be enough, but this
 * leads to a problem with polymorphic methods in Java. Given interface B1 with
 * method B2 foo() and interface C1 with method C2 foo() can not be implemented
 * in Java with class A1 and method A2 foo() where A2 implements B2 and C2.
 * 
 * @author dscharrer
 */
public interface XLoggedObject extends XHalfWritableObject, XSendsObjectEvents, XSendsFieldEvents,
        XSendsTransactionEvents, IHasChangeLog {
	
	/**
	 * Returns the {@link XLoggedField} with the given {@link XID} contained in
	 * this object .
	 * 
	 * @param fieldID The {@link XID} of the {@link XLoggedField} which is to be
	 *            returned
	 * @return The {@link XLoggedField} with the given {@link XID} or null, if
	 *         no corresponding {@link XLoggedField} exists
	 */
	@ReadOperation
	XLoggedField getField(XID fieldId);
	
}
