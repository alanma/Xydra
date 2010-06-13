package org.xydra.core.change;

/**
 * This interface indicates that it is possible to register
 * {@link XModelEventListener XModelEventListeners} to listen for
 * {@link XModelEvent XModelEvents}.
 * 
 * @author voelkel
 * @author Kaidel
 */
public interface XSendsModelEvent {
	
	/**
	 * Adds an {@link XModelEventListener}.
	 * 
	 * @param changeListener The listener which is to be added.
	 * @return true, if adding the given listener was successful, false
	 *         otherwise.
	 */
	
	public boolean addListenerForModelEvents(XModelEventListener changeListener);
	
	/**
	 * Removes the specified {@link XModelEventListener}.
	 * 
	 * @param changeListener The listener which is to be removed.
	 * @return true, if remove the given listener was successful, false
	 *         otherwise.
	 */
	
	public boolean removeListenerForModelEvents(XModelEventListener changeListener);
}
