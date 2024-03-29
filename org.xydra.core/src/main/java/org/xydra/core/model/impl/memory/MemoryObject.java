package org.xydra.core.model.impl.memory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.annotations.CanBeNull;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.Base;
import org.xydra.base.IHasXAddress;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.change.impl.memory.MemoryModelCommand;
import org.xydra.base.change.impl.memory.MemoryObjectCommand;
import org.xydra.base.change.impl.memory.RevisionConstants;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.impl.XExistsRevWritableModel;
import org.xydra.base.rmof.impl.XExistsRevWritableObject;
import org.xydra.base.rmof.impl.XExistsRevWritableRepository;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.base.value.XValue;
import org.xydra.core.XCopyUtils;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XObjectEventListener;
import org.xydra.core.change.XRMOFChangeListener;
import org.xydra.core.change.XSendsFieldEvents;
import org.xydra.core.change.XSendsObjectEvents;
import org.xydra.core.change.XSendsTransactionEvents;
import org.xydra.core.change.XTransactionEventListener;
import org.xydra.core.model.IHasChangeLog;
import org.xydra.core.model.XChangeLog;
import org.xydra.core.model.XChangeLogState;
import org.xydra.core.model.XExecutesCommands;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XSynchronizesChanges;
import org.xydra.core.model.impl.memory.sync.Root;
import org.xydra.sharedutils.XyAssert;

/**
 * An implementation of {@link XObject}.
 *
 * @author xamde
 * @author kaidel
 *
 */
public class MemoryObject extends AbstractMOFEntity implements IMemoryObject, XObject,
		IHasXAddress, IHasChangeLog, XSynchronizesChanges, XExecutesCommands, XSendsObjectEvents,
		XSendsFieldEvents, XSendsTransactionEvents, Serializable {

	private static final long serialVersionUID = -808702139986657842L;

	/**
	 * delegates change events to local cache only, state change is handled by
	 * {@link Executor} already
	 */
	private final XRMOFChangeListener changeListener = new XRMOFChangeListener() {

		@Override
		public void onChangeEvent(final XFieldEvent event) {
		}

		@Override
		public void onChangeEvent(final XModelEvent event) {
		}

		@Override
		public void onChangeEvent(final XObjectEvent event) {
			if (event.getChangeType() == ChangeType.REMOVE) {
				MemoryObject.this.loadedFields.remove(event.getFieldId());
			}
		}

		@Override
		public void onChangeEvent(final XRepositoryEvent event) {
		}
	};

	/** The father-model of this MemoryObject */
	@CanBeNull
	private final IMemoryModel father;

	private final Map<XId, IMemoryField> loadedFields = new HashMap<XId, IMemoryField>();

	/** The snapshot-like runtime state */
	private final XExistsRevWritableObject objectState;

	/**
	 * Wrap an existing objectState.
	 *
	 * @param father @NeverNull
	 * @param objectState @NeverNull
	 */
	public MemoryObject(final IMemoryModel father, final XRevWritableObject objectState) {
		super(father.getRoot());
		assert objectState != null;
		assert objectState.getAddress().getRepository() != null;
		assert objectState.getAddress().getModel() != null;

		this.father = father;
		if (objectState instanceof XExistsRevWritableObject) {
			this.objectState = (XExistsRevWritableObject) objectState;
		} else {
			this.objectState = XCopyUtils.createSnapshot(objectState);
		}
	}

	/**
	 * @param root @CanBeNull if father is defined
	 * @param father @CanBeNull if root is defined
	 * @param actorId
	 * @param passwordHash
	 * @param objectAddress
	 * @param objectState @CanBeNull
	 * @param log
	 * @param createObject Can only be true if state & log are null. If true, an
	 *            initial create-this-object-event is added.
	 */
	@SuppressWarnings("null")
	private MemoryObject(final Root root, final IMemoryModel father, final XId actorId, final String passwordHash,
			final XAddress objectAddress, final XRevWritableObject objectState, final XChangeLogState log,
			final boolean createObject) {
		super(father == null ? root : father.getRoot());
		assert father != null || root != null;

		if (objectState == null) {
			final XExistsRevWritableObject newObjectState = new SimpleObject(objectAddress);
			if (father != null) {
				newObjectState.setRevisionNumber(father.getRevisionNumber());
			} else {
				newObjectState.setRevisionNumber(XCommand.NONEXISTANT);
				newObjectState.setExists(false);
			}
			this.objectState = newObjectState;
		} else {
			if (objectState instanceof XExistsRevWritableObject) {
				this.objectState = (XExistsRevWritableObject) objectState;
			} else {
				this.objectState = XCopyUtils.createSnapshot(objectState);
			}
		}
		assert this.objectState != null;
		this.objectState.setExists(createObject || objectState != null);
		assert this.objectState.getAddress().getRepository() != null;
		assert this.objectState.getAddress().getModel() != null;
		assert this.objectState.getAddress().equals(objectAddress);

		if (createObject) {
			if (father != null) {
				final XModelCommand createObjectCommand = MemoryModelCommand.createAddCommand(
						father.getAddress(), true, getId());
				Executor.executeCommandOnModel(actorId, createObjectCommand, null,
						father.getState(), root, null);
			}
			setExists(true);
			if (this.objectState.getRevisionNumber() != RevisionConstants.NO_REVISION) {
				this.objectState.setRevisionNumber(root.getSyncLog().getCurrentRevisionNumber());
			}
		}

		this.father = father;
	}

	/**
	 * Create a new stand-alone object (without father), which can be synced IFF
	 * objectAddress is a full address and has an initial create-object command
	 * in its changeLog
	 *
	 * @param actorId
	 * @param passwordHash
	 * @param objectAddress @NeverNull
	 */
	public MemoryObject(final XId actorId, final String passwordHash, final XAddress objectAddress) {
		this(Root.createWithActor(actorId, objectAddress, XCommand.NEW), null, actorId,
				passwordHash, objectAddress, null, null, true);

		assert objectAddress.getRepository() != null;
		assert objectAddress.getModel() != null;

		assert objectAddress != null;
	}

	/**
	 * Create a new stand-alone object (without father), which can be synced IFF
	 * objectAddress is a full address and has an initial create-object command
	 * in its changeLog
	 *
	 * @param actorId
	 * @param passwordHash
	 * @param objectId @NeverNull
	 */
	public MemoryObject(final XId actorId, final String passwordHash, final XId objectId) {
		this(actorId, passwordHash, Base.toAddress(XId.DEFAULT, XId.DEFAULT, objectId, null));

		assert objectId != null;
	}

	/**
	 * Create a stand-alone object (without father) wrapping an existing state.
	 * Can be synced IFF objectAddress is a full address and has an initial
	 * create-object command in its changeLog.
	 *
	 * Used for de-serialisation.
	 *
	 * @param actorId
	 * @param passwordHash
	 * @param objectState @NeverNull
	 * @param log @CanBeNull
	 */
	public MemoryObject(final XId actorId, final String passwordHash, final XRevWritableObject objectState,
			final XChangeLogState log) {
		this(Root.createWithActorAndChangeLogState(actorId, objectState.getAddress(), log), null,
				actorId, passwordHash, objectState.getAddress(), objectState, log, true);

		assert objectState != null;
		assert objectState.getAddress().getRepository() != null;
		assert objectState.getAddress().getModel() != null;
	}

	@Override
	public boolean addListenerForFieldEvents(final XFieldEventListener changeListener) {
		synchronized (getRoot()) {
			return getRoot().addListenerForFieldEvents(getAddress(), changeListener);
		}
	}

	@Override
	public boolean addListenerForObjectEvents(final XObjectEventListener changeListener) {
		synchronized (getRoot()) {
			return getRoot().addListenerForObjectEvents(getAddress(), changeListener);
		}
	}

	@Override
	public boolean addListenerForTransactionEvents(final XTransactionEventListener changeListener) {
		synchronized (getRoot()) {
			return getRoot().addListenerForTransactionEvents(getAddress(), changeListener);
		}
	}

	@Override
	public int countUnappliedLocalChanges() {
		return getRoot().countUnappliedLocalChanges();
	}

	@Override
	public IMemoryField createField(final XId fieldId) {
		if (getRevisionNumber() < 0) {
			throw new IllegalStateException("cannot add field to not existing object!");
		}

		final XObjectCommand command = MemoryObjectCommand.createAddCommand(getAddress(), true, fieldId);

		// synchronize so that return is never null if command succeeded
		synchronized (getRoot()) {
			final long result = executeObjectCommand(command);
			final IMemoryField field = getField(fieldId);
			XyAssert.xyAssert(result == XCommand.FAILED || field != null, "result=" + result
					+ " field=" + field);
			return field;
		}
	}

	@Override
	public XRevWritableObject createSnapshot() {
		synchronized (getRoot()) {
			if (exists()) {
				return XCopyUtils.createSnapshot(this);
			} else {
				return null;
			}
		}
	}

	@ReadOperation
	@Override
	public boolean equals(final Object object) {
		synchronized (getRoot()) {
			return super.equals(object);
		}
	}

	@Override
	public long executeCommand(final XCommand command) {
		assert command != null;
		assert command instanceof XTransaction || command instanceof XModelCommand
				|| command instanceof XObjectCommand || command instanceof XFieldCommand;

		synchronized (getRoot()) {
			final XId actorId = getRoot().getSessionActor();
			XExistsRevWritableModel modelState = null;
			IMemoryRepository repository = null;
			XExistsRevWritableRepository repositoryState = null;
			if (getFather() != null) {
				modelState = getFather().getState();
				repository = getFather().getFather();
				if (repository != null) {
					repositoryState = repository.getState();
				}
			}
			return Executor.executeCommandOnObject(actorId, command, repositoryState, modelState,
					getState(), getRoot(), this.changeListener);
		}
	}

	@Override
	public long executeObjectCommand(final XObjectCommand command) {
		return executeCommand(command);
	}

	// implement IMemoryObject
	@Override
	public void fireFieldEvent(final XFieldEvent event) {
		synchronized (getRoot()) {
			getRoot().fireFieldEvent(getAddress(), event);
		}
	}

	// implement IMemoryObject
	@Override
	public void fireObjectEvent(final XObjectEvent event) {
		synchronized (getRoot()) {
			getRoot().fireObjectEvent(getAddress(), event);
		}
	}

	// implement IMemoryObject
	@Override
	public void fireTransactionEvent(final XTransactionEvent event) {
		synchronized (getRoot()) {
			getRoot().fireTransactionEvent(getAddress(), event);
		}
	}

	@Override
	public XAddress getAddress() {
		synchronized (getRoot()) {
			return this.objectState.getAddress();
		}
	}

	@Override
	public XChangeLog getChangeLog() {
		return getRoot().getSyncLog();
	}

	@Override
	public IMemoryModel getFather() {
		return this.father;
	}

	@Override
	public IMemoryField getField(final XId fieldId) {
		synchronized (getRoot()) {
			assertThisEntityExists();

			IMemoryField field = this.loadedFields.get(fieldId);
			if (field != null) {
				return field;
			}

			final XRevWritableField fieldState = this.objectState.getField(fieldId);
			if (fieldState == null) {
				return null;
			}

			field = new MemoryField(this, fieldState);
			this.loadedFields.put(fieldId, field);

			return field;
		}
	}

	@Override
	public XId getId() {
		synchronized (getRoot()) {
			return this.objectState.getId();
		}
	}

	/**
	 * @return the {@link XId} of the father-{@link XModel} of this MemoryObject
	 *         or null, if this object has no father.
	 */
	@SuppressWarnings("unused")
	private XId getModelId() {
		return this.father == null ? null : this.father.getId();
	}

	@Override
	public long getRevisionNumber() {
		synchronized (getRoot()) {
			return exists() ? this.objectState.getRevisionNumber() : XCommand.NONEXISTANT;
		}
	}

	@Override
	public XId getSessionActor() {
		return getRoot().getSessionActor();
	}

	@Override
	public String getSessionPasswordHash() {
		return getRoot().getSessionPasswordHash();
	}

	@Override
	public long getSynchronizedRevision() {
		return getRoot().getSynchronizedRevision();
	}

	// implement IMemoryObject
	@Override
	public XExistsRevWritableObject getState() {
		return this.objectState;
	}

	@Override
	public XType getType() {
		return XType.XOBJECT;
	}

	@Override
	public boolean hasField(final XId id) {
		synchronized (getRoot()) {
			assertThisEntityExists();
			return this.objectState.hasField(id);
		}
	}

	@ReadOperation
	@Override
	public int hashCode() {
		synchronized (getRoot()) {
			return super.hashCode();
		}
	}

	@Override
	public boolean isEmpty() {
		synchronized (getRoot()) {
			assertThisEntityExists();
			return this.objectState.isEmpty();
		}
	}

	@Override
	public boolean isSynchronized() {
		return getRevisionNumber() <= getSynchronizedRevision();
	}

	@Override
	public Iterator<XId> iterator() {
		synchronized (getRoot()) {
			assertThisEntityExists();
			return this.objectState.iterator();
		}
	}

	@Override
	public boolean removeField(final XId fieldId) {

		/*
		 * no synchronization necessary here (except that in
		 * executeObjectCommand())
		 */

		final XObjectCommand command = MemoryObjectCommand.createRemoveCommand(getAddress(),
				XCommand.FORCED, fieldId);

		final long result = executeObjectCommand(command);
		XyAssert.xyAssert(result >= 0 || result == XCommand.NOCHANGE);
		return result != XCommand.NOCHANGE;
	}

	@Override
	public boolean removeListenerForFieldEvents(final XFieldEventListener changeListener) {
		synchronized (getRoot()) {
			return getRoot().removeListenerForFieldEvents(getAddress(), changeListener);
		}
	}

	@Override
	public boolean removeListenerForObjectEvents(final XObjectEventListener changeListener) {
		synchronized (getRoot()) {
			return getRoot().removeListenerForObjectEvents(getAddress(), changeListener);
		}
	}

	@Override
	public boolean removeListenerForTransactionEvents(final XTransactionEventListener changeListener) {
		synchronized (getRoot()) {
			return getRoot().removeListenerForTransactionEvents(getAddress(), changeListener);
		}
	}

	@Override
	public void setSessionActor(final XId actorId, final String passwordHash) {
		getRoot().setSessionActor(actorId);
		getRoot().setSessionPasswordHash(passwordHash);
	}

	@ReadOperation
	@Override
	public String toString() {
		return getId() + " rev[" + getRevisionNumber() + "]" + " "
				+ this.objectState.toString();
	}

	@Override
	public boolean exists() {
		return this.objectState.exists();
	}

	@Override
	public void setExists(final boolean entityExists) {
		this.objectState.setExists(entityExists);
	}

	@Override
	public boolean setFieldValue(final XId fieldId, final XValue value) {
		if (value == null) {
			return removeField(fieldId);
		}
		return createField(fieldId).setValue(value);
	}

	@Override
	public XValue getFieldValue(final XId fieldId) {
		final IMemoryField field = getField(fieldId);
		if (field == null) {
			return null;
		}
		return field.getValue();
	}

}
