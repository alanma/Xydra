package org.xydra.core.change;

import org.xydra.core.model.XAddress;


/**
 * An {@link XEvent} that is not made up of other {@link XEvent XEvents}.
 * 
 */
public interface XAtomicEvent extends XEvent {
	
	/**
	 * WHAT is being changed?
	 * 
	 * @return the model, object or field that was added or removed or the field
	 *         whose value changed; null for transactions.
	 */
	XAddress getChangedEntity();
	
}
