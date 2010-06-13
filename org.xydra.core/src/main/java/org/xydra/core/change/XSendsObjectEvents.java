package org.xydra.core.change;

/**
 * This interface indicates that it is possible register
 * {@link XObjectEventListener XObjectEventListeners} to listen for
 * {@link XObjectEvent XObjectEvents}.
 * 
 * @author voelkel
 * @author Kaidel
 */
public interface XSendsObjectEvents {
	
	/**
	 * Adds an {@link XObjectEventListener}.
	 * 
	 * @param changeListener The listener which is to be added.
	 * @return true, if adding the given listener was successful, false
	 *         otherwise.
	 */
	
	public boolean addListenerForObjectEvents(XObjectEventListener changeListener);
	
	/**
	 * Removes the specified {@link XObjectEventListener}.
	 * 
	 * @param changeListener The listener which is to be removed.
	 * @return true, if removing the given listener was successful, false
	 *         otherwise.
	 */
	
	public boolean removeListenerForObjectEvents(XObjectEventListener changeListener);
}
