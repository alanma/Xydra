package org.xydra.core.change;

/**
 * This interface indicates that that it is possible to register
 * {@link XRepositoryEvent XRepositoryEventListeners} to listen for
 * {@link XRepositoryEvent XRepositoryEvents}.
 * 
 * @author voelkel
 * @author Kaidel
 */
public interface XSendsRepositoryEvents {
	
	/**
	 * Adds an {@link XRepositoryEventListener}.
	 * 
	 * @param changeListener The listener which is to be added.
	 * @return true, if adding the given listener was successful, false
	 *         otherwise.
	 */
	
	public boolean addListenerForRepositoryEvents(XRepositoryEventListener changeListener);
	
	/**
	 * Removes the specified {@link XRepositoryEventListener}.
	 * 
	 * @param changeListener The listener which is to be removed
	 * @return true, if removing the given listener was successful, false
	 *         otherwise.
	 */
	
	public boolean removeListenerForRepositoryEvents(XRepositoryEventListener changeListener);
}
