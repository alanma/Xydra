package org.xydra.core.model;

import org.xydra.core.change.XSendsFieldEvents;


/**
 * A logged field that at least supports read operations and can send events.
 * 
 * @author dscharrer
 * 
 */
public interface XLoggedField extends XBaseField, XSendsFieldEvents {
	
}
