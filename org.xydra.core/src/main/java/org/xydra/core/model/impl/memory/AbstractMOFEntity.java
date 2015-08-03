package org.xydra.core.model.impl.memory;

import java.io.Serializable;

import org.xydra.annotations.ReadOperation;
import org.xydra.base.XId;
import org.xydra.base.change.XSyncEvent;
import org.xydra.core.change.XSyncEventListener;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XSynchronizesChanges;
import org.xydra.core.model.impl.memory.sync.Root;

public abstract class AbstractMOFEntity extends AbstractEntity implements IMemoryMOFEntity,
		XSynchronizesChanges, Serializable {

	public AbstractMOFEntity(final Root root) {
		assert root != null;
		this.root = root;
	}

	/**
	 * Handles all registered event listeners. Also the lock for synchronising
	 * change operations.
	 */
	private final Root root;

	@Override
	public Root getRoot() {
		return this.root;
	}

	/**
	 * @throws IllegalStateException if this method is called after this
	 *             MemoryModel was already removed
	 */
	@ReadOperation
	public void assertThisEntityExists() throws IllegalStateException {
		// TODO synchronize?
		if (!exists()) {
			throw new IllegalStateException("this entity has been removed: " + getAddress());
		}
	}

	@Override
	public boolean addListenerForSyncEvents(final XSyncEventListener syncListener) {
		return this.root.addListenerForSyncEvents(getAddress(), syncListener);
	}

	@Override
	public boolean removeListenerForSyncEvents(final XSyncEventListener syncListener) {
		return this.root.removeListenerForSyncEvents(getAddress(), syncListener);
	}

	@Override
	public void fireSyncEvent(final XSyncEvent event) {
		this.root.fireSyncEvent(getAddress(), event);
	}

	// implement XSynchronizesChanges
	@Override
	public int countUnappliedLocalChanges() {
		return getRoot().countUnappliedLocalChanges();
	}

	// implement XSynchronizesChanges
	@Override
	public XId getSessionActor() {
		return getRoot().getSessionActor();
	}

	// implement XSynchronizesChanges
	@Override
	public String getSessionPasswordHash() {
		return getRoot().getSessionPasswordHash();
	}

	// implement XSynchronizesChanges
	@Override
	public long getSynchronizedRevision() {
		return getRoot().getSyncLog().getSynchronizedRevision();
	}

	/**
	 * Set a new actor to be used when building commands for changes to this
	 * entity and its children.
	 *
	 * @param actorId for this entity and its children, if any.
	 * @param passwordHash the password for the given actor.
	 */
	@Override
	public void setSessionActor(final XId actorId, final String passwordHash) {
		this.root.setSessionActor(actorId);
		this.root.setSessionPasswordHash(passwordHash);
	}

	@Override
	public XChangeLog getChangeLog() {
		return this.root.getChangeLog();
	}

}
