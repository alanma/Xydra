package org.xydra.core.change;

import org.xydra.base.change.XFieldEvent;


/**
 * This interface indicates that it is possible to register
 * {@link XFieldEventListener XFieldEventListeners} to listen for
 * {@link XFieldEvent XFieldEvents}.
 *
 * @author xamde
 * @author kaidel
 */
public interface XSendsFieldEvents {

	/**
	 * Adds an {@link XFieldEventListener}.
	 *
	 * @param changeListener The listener which is to be added.
	 * @return true, if adding the given listener was successful, false
	 *         otherwise.
	 */
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener);

	/**
	 * Removes the specified {@link XFieldEventListener}.
	 *
	 * @param changeListener The listener which is to be removed.
	 * @return true, if removing the given listener was successful, false
	 *         otherwise.
	 */

	public boolean removeListenerForFieldEvents(XFieldEventListener changeListener);
}
