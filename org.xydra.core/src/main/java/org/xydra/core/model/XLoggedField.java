package org.xydra.core.model;

import org.xydra.base.XHalfWritableField;
import org.xydra.core.change.XSendsFieldEvents;


/**
 * A logged field that supports write operations and can send events.
 * 
 * <h4>Design note</h4> Support for read operations would be enough, but this
 * leads to a problem with polymorphic methods in Java. Given interface B1 with
 * method B2 foo() and interface C1 with method C2 foo() can not be implemented
 * in Java with class A1 and method A2 foo() where A2 implements B2 and C2.
 * 
 * @author dscharrer
 * 
 */
public interface XLoggedField extends XHalfWritableField, XSendsFieldEvents {
	
}
