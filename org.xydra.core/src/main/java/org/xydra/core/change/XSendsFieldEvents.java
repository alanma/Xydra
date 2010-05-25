package org.xydra.core.change;

/**
 * This interface indicated that you can register or remove listeners to listen
 * for {@link XFieldEvent}s
 * 
 * @author voelkel
 * @author Kaidel
 */
public interface XSendsFieldEvents {
	
	/**
	 * Adds an XFieldEventListener.
	 * 
	 * @param changeListener The listener which is to be added.
	 * @return true, if adding the given listener was successful, false
	 *         otherwise.
	 */
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener);
	
	/**
	 * Removes the specified XFieldEventListener.
	 * 
	 * @param changeListener The listener which is to be removed.
	 * @return true, if removing the given listener was successful, false
	 *         otherwise.
	 */
	
	public boolean removeListenerForFieldEvents(XFieldEventListener changeListener);
}
