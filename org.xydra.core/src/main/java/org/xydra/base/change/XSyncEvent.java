package org.xydra.base.change;

import org.xydra.base.XAddress;


/**
 * A Xydra event which is fired when an entity has been synced (with success or
 * failure).
 *
 * @author xamde
 */
public class XSyncEvent {

    private final XAddress changedEntity;

    private final boolean syncResult;

    public boolean isSyncSuccess() {
        return this.syncResult;
    }

    /**
     * @param changedEntity MOF address
     * @param syncResult true iff sync was successful
     */
    public XSyncEvent(final XAddress changedEntity, final boolean syncResult) {
        super();
        this.changedEntity = changedEntity;
        this.syncResult = syncResult;
    }

    /**
     * @return WHAT has been changed? The {@link XAddress} of the model, object
     *         or field that was synced.
     */
    public XAddress getChangedEntity() {
        return this.changedEntity;
    }

}
