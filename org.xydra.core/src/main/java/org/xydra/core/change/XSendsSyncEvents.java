package org.xydra.core.change;

/**
 * This interface indicates that it is possible to register
 * {@link XSyncEventListener}
 *
 * @author alpha
 */
public interface XSendsSyncEvents {

    /**
     * Adds an {@link XSyncEventListener}.
     *
     * @param syncListener The listener which is to be added.
     * @return true, if adding the given listener was successful, false
     *         otherwise.
     */
    public boolean addListenerForSyncEvents(XSyncEventListener syncListener);

    /**
     * Removes the specified {@link XSyncEventListener}.
     *
     * @param syncListener The listener which is to be removed.
     * @return true, if removing the given listener was successful, false
     *         otherwise.
     */

    public boolean removeListenerForSyncEvents(XSyncEventListener syncListener);

}
