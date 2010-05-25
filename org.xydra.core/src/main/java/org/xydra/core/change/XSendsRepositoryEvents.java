package org.xydra.core.change;

/**
 * This interface indicated that you can register or remove listeners to listen
 * for {@link XRepositoryEvent}s
 * 
 * @author voelkel
 * @author Kaidel
 */
public interface XSendsRepositoryEvents {
	
	/**
	 * Adds an XRepositoryEventListener.
	 * 
	 * @param changeListener The listener which is to be added.
	 * @return true, if adding the given listener was successful, false
	 *         otherwise.
	 */
	
	public boolean addListenerForRepositoryEvents(XRepositoryEventListener changeListener);
	
	/**
	 * Removes the specified XRepositoryEventListener.
	 * 
	 * @param changeListener The listener which is to be removed
	 * @return true, if removing the given listener was successful, false
	 *         otherwise.
	 */
	
	public boolean removeListenerForRepositoryEvents(XRepositoryEventListener changeListener);
}
