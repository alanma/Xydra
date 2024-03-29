package org.xydra.core.model.impl.memory.sync;

import java.io.Serializable;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XSyncEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XRepositoryEventListener;
import org.xydra.core.change.XSyncEventListener;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XChangeLogState;
import org.xydra.core.model.XRepository;
import org.xydra.core.model.impl.memory.MemoryChangeLogState;
import org.xydra.core.model.impl.memory.MemoryEventBus;
import org.xydra.core.model.impl.memory.MemoryEventBus.EventType;
import org.xydra.core.model.impl.memory.sync.ISyncLog.ChangeRecordMode;

/**
 * A xydra data tree can have one of these forms: R-M-O-F, M-O-F, O-F, or F. Of
 * course, R-M-O-F can also be just R, i.e. a {@link XRepository} is not
 * required to have children. All such trees use a single {@link Root} instance.
 * This simplified implementing sync algorithms.
 *
 * The root manages event sending.
 *
 * @author xamde
 */
public class Root implements XRoot, Serializable {

	/**
	 * @param eventBus
	 * @param syncLog
	 * @param sessionActor
	 */
	public Root(final MemoryEventBus eventBus, final ISyncLog syncLog, final XId sessionActor) {
		this.eventBus = eventBus;
		this.syncLog = syncLog;
		this.sessionActor = sessionActor;
		this.isTransactionInProgress = false;
	}

	/** for registering listeners and firing events */
	private transient MemoryEventBus eventBus;

	private transient MemoryEventBus repositoryEventBus;

	private XId sessionActor;

	private boolean isTransactionInProgress;

	private String sessionPasswordHash;

	private final ISyncLog syncLog;

	@Override
	public String getSessionPasswordHash() {
		return this.sessionPasswordHash;
	}

	public void setSessionPasswordHash(final String sessionPasswordHash) {
		this.sessionPasswordHash = sessionPasswordHash;
	}

	public boolean addListenerForFieldEvents(final XAddress entityAddress,
			final XFieldEventListener changeListener) {
		synchronized (this.eventBus) {
			return this.eventBus.addListener(EventType.FieldChange, entityAddress, changeListener);
		}
	}

	public boolean addListenerForModelEvents(final XAddress entityAddress,
			final XModelEventListener changeListener) {
		synchronized (this.eventBus) {
			return this.eventBus.addListener(EventType.ModelChange, entityAddress, changeListener);
		}
	}

	public boolean addListenerForObjectEvents(final XAddress entityAddress,
			final XObjectEventListener changeListener) {
		synchronized (this.eventBus) {
			return this.eventBus.addListener(EventType.ObjectChange, entityAddress, changeListener);
		}
	}

	public boolean addListenerForRepositoryEvents(final XAddress entityAddress,
			final XRepositoryEventListener changeListener) {
		synchronized (this.eventBus) {
			return this.eventBus.addListener(EventType.RepositoryChange, entityAddress,
					changeListener);
		}
	}

	public boolean addListenerForSyncEvents(final XAddress entityAddress, final XSyncEventListener syncListener) {
		synchronized (this.eventBus) {
			return this.eventBus.addListener(EventType.Sync, entityAddress, syncListener);
		}
	}

	public boolean addListenerForTransactionEvents(final XAddress entityAddress,
			final XTransactionEventListener changeListener) {
		synchronized (this.eventBus) {
			return this.eventBus.addListener(EventType.TransactionChange, entityAddress,
					changeListener);
		}
	}

	public void fireFieldEvent(final XAddress entityAddress, final XFieldEvent event) {
		synchronized (this.eventBus) {
			assert entityAddress.getAddressedType() == XType.XFIELD;
			this.eventBus.fireEvent(EventType.FieldChange, entityAddress, event);
			// object
			XAddress parent = entityAddress.getParent();
			this.eventBus.fireEvent(EventType.FieldChange, parent, event);
			// model
			parent = parent.getParent();
			this.eventBus.fireEvent(EventType.FieldChange, parent, event);
			// repository
			parent = parent.getParent();
			if (this.repositoryEventBus != null) {
				this.repositoryEventBus.fireEvent(EventType.FieldChange, parent, event);
			}
		}
	}

	public void fireModelEvent(final XAddress entityAddress, final XModelEvent event) {
		synchronized (this.eventBus) {
			assert entityAddress.getAddressedType() == XType.XMODEL;
			this.eventBus.fireEvent(EventType.ModelChange, entityAddress, event);
			// repository
			final XAddress parent = entityAddress.getParent();

			if (this.repositoryEventBus != null) {
				this.repositoryEventBus.fireEvent(EventType.ModelChange, parent, event);
			}
		}
	}

	public void fireObjectEvent(final XAddress entityAddress, final XObjectEvent event) {
		synchronized (this.eventBus) {
			assert entityAddress.getAddressedType() == XType.XOBJECT : "type is "
					+ entityAddress.getAddressedType();
			this.eventBus.fireEvent(EventType.ObjectChange, entityAddress, event);
			// model
			XAddress parent = entityAddress.getParent();
			this.eventBus.fireEvent(EventType.ObjectChange, parent, event);
			// repository
			parent = parent.getParent();

			if (this.repositoryEventBus != null) {
				this.repositoryEventBus.fireEvent(EventType.ObjectChange, parent, event);
			}
		}
	}

	public void fireRepositoryEvent(final XAddress entityAddress, final XRepositoryEvent event) {
		synchronized (this.eventBus) {
			this.eventBus.fireEvent(EventType.RepositoryChange, entityAddress, event);
		}
	}

	public void fireSyncEvent(final XAddress entityAddress, final XSyncEvent event) {
		synchronized (this.eventBus) {
			this.eventBus.fireEvent(EventType.Sync, entityAddress, event);
		}
	}

	public void fireTransactionEvent(final XAddress entityAddress, final XTransactionEvent event) {
		this.eventBus.fireEvent(EventType.TransactionChange, entityAddress, event);
	}

	public boolean removeListenerForFieldEvents(final XAddress entityAddress,
			final XFieldEventListener changeListener) {
		synchronized (this.eventBus) {
			return this.eventBus.removeListener(EventType.FieldChange, entityAddress,
					changeListener);
		}
	}

	public boolean removeListenerForModelEvents(final XAddress entityAddress,
			final XModelEventListener changeListener) {
		synchronized (this.eventBus) {
			return this.eventBus.removeListener(EventType.ModelChange, entityAddress,
					changeListener);
		}
	}

	public boolean removeListenerForObjectEvents(final XAddress entityAddress,
			final XObjectEventListener changeListener) {
		synchronized (this.eventBus) {
			return this.eventBus.removeListener(EventType.ObjectChange, entityAddress,
					changeListener);
		}
	}

	public boolean removeListenerForRepositoryEvents(final XAddress entityAddress,
			final XRepositoryEventListener changeListener) {
		synchronized (this.eventBus) {
			return this.eventBus.removeListener(EventType.RepositoryChange, entityAddress,
					changeListener);
		}
	}

	public boolean removeListenerForSyncEvents(final XAddress entityAddress,
			final XSyncEventListener syncListener) {
		synchronized (this.eventBus) {
			return this.eventBus.removeListener(EventType.Sync, entityAddress, syncListener);
		}
	}

	public boolean removeListenerForTransactionEvents(final XAddress entityAddress,
			final XTransactionEventListener changeListener) {
		synchronized (this.eventBus) {
			return this.eventBus.removeListener(EventType.TransactionChange, entityAddress,
					changeListener);
		}
	}

	public boolean isTransactionInProgess() {
		return this.isTransactionInProgress;
	}

	public void setTransactionInProgress(final boolean b) {
		this.isTransactionInProgress = b;
	}

	@Override
	public XId getSessionActor() {
		return this.sessionActor;
	}

	/**
	 * Set a new actor to be used when building commands for changes.
	 *
	 * @param actor for this field.
	 */
	public void setSessionActor(final XId actor) {
		this.sessionActor = actor;
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
		setSessionActor(actorId);
		setSessionPasswordHash(passwordHash);
	}

	/**
	 * @param actorId @NeverNull
	 * @param baseAddress for sync log @NeverNull
	 * @param changeLogBaseRevision
	 * @return ...
	 */
	public static Root createWithActor(final XId actorId, final XAddress baseAddress, final long changeLogBaseRevision) {
		return new Root(new MemoryEventBus(), MemorySyncLog.create(baseAddress,
				changeLogBaseRevision), actorId);
	}

	/**
	 * @param actorId @NeverNull
	 * @param baseAddress for sync log @NeverNull
	 * @param changeLogState @CanBeNull
	 * @return ...
	 */
	public static Root createWithActorAndChangeLogState(final XId actorId, final XAddress baseAddress,
			final XChangeLogState changeLogState) {
		final XChangeLogState usedChangeLogState = changeLogState == null ? new MemoryChangeLogState(
				baseAddress) : changeLogState;

		return new Root(

		new MemoryEventBus(),

		new MemorySyncLog(usedChangeLogState),

		actorId);
	}

	private Object readResolve() {
		this.eventBus = new MemoryEventBus();
		return this;
	}

	@Override
	public int countUnappliedLocalChanges() {
		return this.syncLog.countUnappliedLocalChanges();
	}

	public void startExecutingTransaction() {
		this.isTransactionInProgress = true;
	}

	public void stopExecutingTransaction() {
		this.isTransactionInProgress = false;
	}

	private boolean locked = false;

	public void lock() {
		this.locked = true;
	}

	public void unlock() {
		this.locked = false;
	}

	public boolean isLocked() {
		return this.locked;
	}

	public ISyncLog getSyncLog() {
		return this.syncLog;
	}

	@Override
	public long getSynchronizedRevision() {
		return this.syncLog.getSynchronizedRevision();
	}

	public void registerRepositoryEventBus(final MemoryEventBus repoBus) {
		this.repositoryEventBus = repoBus;
	}

	@Override
	public String toString() {
		return "inTrans?" + this.isTransactionInProgress + " locked?" + this.locked + " "
				+ this.syncLog.toString();
	}

	@Override
	public XChangeLog getChangeLog() {
		return getSyncLog();
	}

	@Override
	public boolean addListenerForSyncEvents(final XSyncEventListener syncListener) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeListenerForSyncEvents(final XSyncEventListener syncListener) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * See doc of {@link ChangeRecordMode}.
	 *
	 * @param changeRecordMode
	 */
	public void setChangeRecordMode(final ChangeRecordMode changeRecordMode) {
		this.syncLog.setChangeRecordMode(changeRecordMode);
	}

}
