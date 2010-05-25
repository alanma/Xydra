package org.xydra.core.change;

/**
 * This interface indicated that you can register or remove listeners to listen
 * for {@link XObjectEvent}s
 * 
 * @author voelkel
 * @author Kaidel
 */
public interface XSendsObjectEvents {
	
	/**
	 * Adds an XObjectEventListener.
	 * 
	 * @param changeListener The listener which is to be added.
	 * @return true, if adding the given listener was successful, false
	 *         otherwise.
	 */
	
	public boolean addListenerForObjectEvents(XObjectEventListener changeListener);
	
	/**
	 * Removes the specified XObjectEventListener.
	 * 
	 * @param changeListener The listener which is to be removed.
	 * @return true, if removing the given listener was successful, false
	 *         otherwise.
	 */
	
	public boolean removeListenerForObjectEvents(XObjectEventListener changeListener);
}
