package org.xydra.core.change;

/**
 * This interface indicated that you can register or remove listeners to listen
 * for {@link XModelEvent}s
 * 
 * @author voelkel
 * @author Kaidel
 */
public interface XSendsModelEvent {
	
	/**
	 * Adds an XModelEventListener.
	 * 
	 * @param changeListener The listener which is to be added.
	 * @return true, if adding the given listener was successful, false
	 *         otherwise.
	 */
	
	public boolean addListenerForModelEvents(XModelEventListener changeListener);
	
	/**
	 * Removes the specified XModelEventListener.
	 * 
	 * @param changeListener The listener which is to be removed.
	 * @return true, if remove the given listener was successful, false
	 *         otherwise.
	 */
	
	public boolean removeListenerForModelEvents(XModelEventListener changeListener);
}
