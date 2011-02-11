package org.xydra.base.change;

import org.xydra.base.value.XValue;


/**
 * An {@link XFieldEvent} that supports additionally operations that make it
 * easy to un-do the event.
 * 
 * This kind of events is supported on the client side. For data storage and
 * network transmission the less space-consuming {@link XFieldEvent} should be
 * used.
 * 
 * @author xamde
 * 
 */
public interface XReversibleFieldEvent extends XFieldEvent {
	
	/**
	 * WHAT was changed?
	 * 
	 * @return the old {@link XValue} before the change happened. null indicates
	 *         an add event.
	 */
	XValue getOldValue();
	
}
