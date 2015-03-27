package org.xydra.core.model.impl.memory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xydra.annotations.ModificationOperation;
import org.xydra.annotations.NeverNull;
import org.xydra.annotations.ReadOperation;
import org.xydra.base.IHasXAddress;
import org.xydra.base.XAddress;
import org.xydra.base.XCompareUtils;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XCommandUtils;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XModelEvent;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XObjectEvent;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XRepositoryEvent;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.change.impl.memory.MemoryModelCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.base.change.impl.memory.RevisionConstants;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.impl.XExistsRevWritableModel;
import org.xydra.base.rmof.impl.XExistsRevWritableRepository;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.base.rmof.impl.memory.SimpleObject;
import org.xydra.core.XCopyUtils;
import org.xydra.core.XX;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.XModelEventListener;
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
import org.xydra.core.model.XRepository;
import org.xydra.core.model.XSynchronizesChanges;
import org.xydra.core.model.impl.memory.sync.ISyncLog;
import org.xydra.core.model.impl.memory.sync.ISyncLogState;
import org.xydra.core.model.impl.memory.sync.Root;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.sharedutils.XyAssert;

/**
 * The core state information is represented in two ways, which must be kept in
 * sync: (a) a {@link XRevWritableModel} representing the current snapshot, and
 * (b) a {@link ISyncLog} which represents the change history.
 * 
 * 
 * Update strategy:
 * 
 * State reads are handled by (a); change operations are checked on (a),
 * executed on (b) and finally materialised again in (a).
 * 
 * @author xamde
 * @author kaidel
 */
public class MemoryModel extends AbstractMOFEntity implements IMemoryModel, XModel,

IHasXAddress, IHasChangeLog, XSynchronizesChanges, XExecutesCommands,

XSendsObjectEvents, XSendsFieldEvents, XSendsTransactionEvents,

Serializable {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(MemoryModel.class);

	private static final long serialVersionUID = -2969189978307340483L;

	/**
	 * @param actorId @NeverNull to create a root
	 * @param father @NeverNull
	 * @param modelId @NeverNull
	 * @return a MemoryModel that represents a model that does not exist and
	 *         never existed before
	 */
	public static MemoryModel createNonExistantModel(XId actorId, IMemoryRepository father,
			XId modelId) {
		assert father != null;
		assert modelId != null;

		XAddress modelAddress = XX.resolveModel(father.getAddress(), modelId);

		MemoryModel nonExistingModel = new MemoryModel(modelAddress, Root.createWithActor(actorId,
				modelAddress, XCommand.NONEXISTANT), father);
		return nonExistingModel;
	}

	/**
	 * delegates change events to local cache only, state change is handled by
	 * {@link Executor} already
	 */
	private transient final XRMOFChangeListener changeListener = new XRMOFChangeListener() {

		@Override
		public void onChangeEvent(XFieldEvent event) {
		}

		@Override
		public void onChangeEvent(XModelEvent event) {
			if (event.getChangeType() == ChangeType.REMOVE) {
				MemoryModel.this.loadedObjects.remove(event.getObjectId());
			}
		}

		@Override
		public void onChangeEvent(XObjectEvent event) {
		}

		@Override
		public void onChangeEvent(XRepositoryEvent event) {
			switch (event.getChangeType()) {
			case ADD:
				MemoryModel.this.setExists(true);
				break;
			case REMOVE:
				MemoryModel.this.setExists(false);
				break;
			default:
				assert false;
			}
		}
	};

	/**
	 * The father-repository of this MemoryModel
	 * 
	 * @CanBeNull for stand-alone-models without father; if present, it's used
	 *            to ensure uniqueness of modelIds
	 */
	private final transient IMemoryRepository father;

	/**
	 * Current state as a snapshot in augmented form.
	 */
	private final transient Map<XId, IMemoryObject> loadedObjects = new HashMap<XId, IMemoryObject>();

	/**
	 * Represents the current state as a snapshot.
	 * 
	 * A model with revision numbers is required to let e.g. each object know
	 * its current revision number.
	 */
	final XExistsRevWritableModel modelState;

	/**
	 * A model with the given initial state
	 * 
	 * @param father @CanBeNull
	 * @param actorId @NeverNull
	 * @param passwordHash
	 * @param modelState @NeverNull
	 * @param changeLogState @CanBeNull
	 */
	public MemoryModel(IMemoryRepository father, XId actorId, String passwordHash,
			XExistsRevWritableModel modelState, XChangeLogState changeLogState) {
		this(
				Root.createWithActorAndChangeLogState(actorId, modelState.getAddress(),
						changeLogState), father, actorId, passwordHash, modelState.getAddress(),
				modelState, true);

		assert modelState != null;
		assert changeLogState != null;
	}

	/**
	 * A model that can be synced if it has a father. Internal state is re-used
	 * or a new internal state is created if none is given.
	 * 
	 * @param father @CanBeNull
	 * @param actorId @NeverNull
	 * @param passwordHash
	 * @param modelAddress @NeverNull
	 * @param modelState @CanBeNull If present, must have same address as
	 *            modelAddress
	 * @param changeLogState @CanBeNull
	 * @param createModel If no modelState was given and if true, an initial
	 *            create-this-model command is added
	 */
	private MemoryModel(Root root, IMemoryRepository father, XId actorId, String passwordHash,
			XAddress modelAddress, XExistsRevWritableModel modelState, boolean createModel) {
		super(root);

		this.father = father;
		if (father != null) {
			assert father.getAddress().equals(modelAddress.getParent());
		}

		assert actorId != null;
		assert modelAddress != null;
		assert modelAddress.getModel() != null;
		assert modelState == null || modelState.getAddress().equals(modelAddress);

		if (modelState == null) {
			this.modelState = new SimpleModel(modelAddress);

			long currentModelRev = root.getSyncLog().getCurrentRevisionNumber();
			this.modelState.setRevisionNumber(currentModelRev);
			XEvent event = root.getSyncLog().getLastEvent();
			boolean modelExists = false;
			if (event != null) {
				if (event instanceof XModelEvent) {
					XModelEvent modelEvent = (XModelEvent) event;
					modelExists = modelEvent.getChangeType() != ChangeType.REMOVE;
				} else
					// another event happened, so model must be there
					modelExists = true;
			}
			this.modelState.setExists(modelExists);
		} else {
			this.modelState = modelState;
			this.modelState.setExists(modelState.exists());
		}

		if (createModel) {
			createThisModel();
			assert getRevisionNumber() >= 0;
			assert getChangeLog().getCurrentRevisionNumber() >= 0;
			assert this.modelState.exists();
		} else {
			if (this.modelState.getRevisionNumber() != RevisionConstants.NO_REVISION) {
				this.modelState.setRevisionNumber(getChangeLog().getCurrentRevisionNumber());
			}
		}

		assert getRevisionNumber() == getChangeLog().getCurrentRevisionNumber() : "rev="
				+ getRevisionNumber() + " change.rev=" + getChangeLog().getCurrentRevisionNumber();
	}

	/**
	 * Non-existing model
	 * 
	 * @param root @NeverNull
	 * @param father @CanBenull
	 */
	private MemoryModel(XAddress modelAddress, Root root, IMemoryRepository father) {
		super(root);
		this.father = father;
		this.modelState = createModelState(modelAddress, father);
		this.modelState.setExists(false);
		this.modelState.setRevisionNumber(XCommand.NONEXISTANT);
	}

	private static XExistsRevWritableModel createModelState(XAddress modelAddress,
			IMemoryRepository father) {
		if (father == null) {
			return new SimpleModel(modelAddress);
		} else {
			XId modelId = modelAddress.getModel();
			assert father.getState().getModel(modelId) == null;
			return father.getState().createModel(modelId);
		}
	}

	public MemoryModel(XId actorId, IMemoryRepository father, XRevWritableModel modelState) {
		super(Root
				.createWithActor(actorId, modelState.getAddress(), modelState.getRevisionNumber()));

		assert modelState != null;

		this.father = father;
		if (modelState instanceof XExistsRevWritableModel) {
			this.modelState = (XExistsRevWritableModel) modelState;
		} else {
			SimpleModel simpleModel = new SimpleModel(modelState.getAddress());
			XCopyUtils.copyDataAndRevisions(modelState, simpleModel);
			this.modelState = simpleModel;
			this.modelState.setExists(true);
		}
	}

	/**
	 * Creates a stand-alone model.
	 * 
	 * Used in tests.
	 * 
	 * @param actorId
	 * @param passwordHash
	 * @param modelAddress
	 */
	public MemoryModel(XId actorId, String passwordHash, XAddress modelAddress) {
		this(Root.createWithActor(actorId, modelAddress, XCommand.NONEXISTANT), null, actorId,
				passwordHash, modelAddress, null, true);
	}

	/**
	 * Creates a stand-alone model.
	 * 
	 * Used in tests.
	 * 
	 * @param actorId
	 * @param passwordHash
	 * @param modelId
	 */
	public MemoryModel(XId actorId, String passwordHash, XId modelId) {
		this(actorId, passwordHash, XX.resolveModel(XId.DEFAULT, modelId));
	}

	/**
	 * Used for copy.
	 * 
	 * @param actorId
	 * @param passwordHash
	 * @param modelState @NeverNull
	 */
	public MemoryModel(XId actorId, String passwordHash, XExistsRevWritableModel modelState) {
		this(
				Root.createWithActor(actorId, modelState.getAddress(),
						modelState.getRevisionNumber()), null, actorId, passwordHash, modelState
						.getAddress(), modelState, false);
	}

	/**
	 * Used for de-serialisation.
	 * 
	 * @param actorId
	 * @param passwordHash
	 * @param modelState @NeverNull
	 * @param syncLogState @CanBeNull
	 */
	public MemoryModel(XId actorId, String passwordHash, XExistsRevWritableModel modelState,
			ISyncLogState syncLogState) {
		this(Root.createWithActorAndChangeLogState(actorId, modelState.getAddress(), syncLogState),
				null, actorId, passwordHash, modelState.getAddress(), modelState, false);
	}

	@Override
	public boolean addListenerForFieldEvents(XFieldEventListener changeListener) {
		synchronized (getRoot()) {
			return getRoot().addListenerForFieldEvents(getAddress(), changeListener);
		}
	}

	@Override
	public boolean addListenerForModelEvents(XModelEventListener changeListener) {
		synchronized (getRoot()) {
			return getRoot().addListenerForModelEvents(getAddress(), changeListener);
		}
	}

	@Override
	public boolean addListenerForObjectEvents(XObjectEventListener changeListener) {
		synchronized (getRoot()) {
			return getRoot().addListenerForObjectEvents(getAddress(), changeListener);
		}
	}

	@Override
	public boolean addListenerForTransactionEvents(XTransactionEventListener changeListener) {
		synchronized (getRoot()) {
			return getRoot().addListenerForTransactionEvents(getAddress(), changeListener);
		}
	}

	@Override
	@ModificationOperation
	public IMemoryObject createObject(@NeverNull XId objectId) {
		XModelCommand command = MemoryModelCommand.createAddCommand(getAddress(), true, objectId);

		// Synchronised so that return is never null if command succeeded
		synchronized (getRoot()) {
			long result = executeModelCommand(command);
			IMemoryObject object = getObject(objectId);
			XyAssert.xyAssert(result == XCommand.FAILED || object != null);
			return object;
		}
	}

	@Override
	@ReadOperation
	public XRevWritableModel createSnapshot() {
		synchronized (getRoot()) {
			if (exists()) {
				return XCopyUtils.createSnapshot(this.getState());
			} else {
				return null;
			}
		}
	}

	private void createThisModel() {

		XAddress repositoryAddress = getAddress().getParent();
		if (repositoryAddress == null) {
			repositoryAddress = XX.resolveRepository(XId.DEFAULT);
		}

		XRepositoryCommand command = MemoryRepositoryCommand.createAddCommand(repositoryAddress,
				true, getId());

		// Synchronised so that return is never null if command succeeded
		synchronized (getRoot()) {
			long result = Executor.executeCommandOnModel(getSessionActor(), command,
					getFatherState(), getState(), getRoot(), null);
			assert XCommandUtils.success(result);
			assert XCommandUtils.changedSomething(result);
			assert getState().exists();
			assert exists();
		}
	}

	@Override
	@ReadOperation
	public boolean equals(Object o) {

		// FIXME use readablemodel here?

		if (!(o instanceof MemoryModel)) {
			return false;
		}

		MemoryModel other = (MemoryModel) o;
		synchronized (getRoot()) {
			// compare revision number, repositoryId & modelId
			return XCompareUtils.equalId(this.father, other.getFather())
					&& XCompareUtils.equalState(this.getState(), other.getState());
		}
	}

	@Override
	public long executeCommand(XCommand command) {
		assert command != null;
		assert command instanceof XTransaction || command instanceof XRepositoryCommand
				|| command instanceof XModelCommand || command instanceof XObjectCommand
				|| command instanceof XFieldCommand;

		synchronized (getRoot()) {
			XId actorId = getRoot().getSessionActor();
			XExistsRevWritableRepository repositoryState = getFatherState();
			return Executor.executeCommandOnModel(actorId, command, repositoryState, getState(),
					getRoot(), this.changeListener);
		}
	}

	/**
	 * method is responsible for updating the snapshot-like state
	 * 
	 * @param command @NeverNull
	 * @return ...
	 */
	@Override
	@ModificationOperation
	public long executeModelCommand(XModelCommand command) {
		assert command != null;
		return Executor.executeCommandOnModel(getSessionActor(), command, getFatherState(),
				getState(), getRoot(), this.changeListener);
	}

	// implement IMemoryModel
	@Override
	public void fireFieldEvent(XFieldEvent event) {
		synchronized (getRoot()) {
			getRoot().fireFieldEvent(getAddress(), event);
		}
	}

	/**
	 * Notifies all listeners that have registered interest for notification on
	 * {@link XModelEvent XModelEvents} happening on this MemoryModel.
	 * 
	 * @param event The {@link XModelEvent} which will be propagated to the
	 *            registered listeners.
	 */
	// implement IMemoryModel
	@Override
	public void fireModelEvent(XModelEvent event) {
		getRoot().fireModelEvent(getAddress(), event);
	}

	// implement IMemoryModel
	@Override
	public void fireObjectEvent(XObjectEvent event) {
		synchronized (getRoot()) {
			getRoot().fireObjectEvent(getAddress(), event);
		}
	}

	// implement IMemoryModel
	@Override
	public void fireTransactionEvent(XTransactionEvent event) {
		synchronized (getRoot()) {
			getRoot().fireTransactionEvent(getAddress(), event);
		}
	}

	@Override
	@ReadOperation
	public XAddress getAddress() {
		return this.modelState.getAddress();
	}

	@Override
	public XChangeLog getChangeLog() {
		return this.getRoot().getSyncLog();
	}

	/**
	 * Returns the father-{@link MemoryRepository} of this MemoryModel.
	 * 
	 * @return The father of this MemoryModel (may be null).
	 */
	@Override
	@ReadOperation
	public IMemoryRepository getFather() {
		return this.father;
	}

	private XExistsRevWritableRepository getFatherState() {
		IMemoryRepository father = getFather();
		return father == null ? null : father.getState();
	}

	@Override
	@ReadOperation
	public XId getId() {
		return this.modelState.getId();
	}

	@Override
	@ReadOperation
	public IMemoryObject getObject(@NeverNull XId objectId) {
		synchronized (getRoot()) {
			assertThisEntityExists();

			// lazy loading of MemoryObjects
			IMemoryObject object = this.loadedObjects.get(objectId);
			if (object != null) {
				return object;
			}

			XRevWritableObject objectState = this.modelState.getObject(objectId);
			if (objectState == null) {
				return null;
			}

			object = new MemoryObject(this, objectState);

			if (object instanceof SimpleObject) {
				// dont cache, its very quick to load these
			} else {
				this.loadedObjects.put(objectId, object);
			}

			return object;
		}
	}

	/**
	 * @return the {@link XId} of the father-{@link XRepository} of this
	 *         MemoryModel or null, if this object has none. An object can have
	 *         a repositoryId even if it has no father - {@link XRepository}.
	 */
	@ReadOperation
	protected XId getRepositoryId() {
		return getAddress().getRepository();
	}

	@Override
	@ReadOperation
	public long getRevisionNumber() {
		synchronized (getRoot()) {
			// assert this.state.getRevisionNumber() ==
			// this.syncState.getChangeLog()
			// .getCurrentRevisionNumber() : "stateRev=" +
			// this.state.getRevisionNumber()
			// + " syncStateRev=" +
			// this.syncState.getChangeLog().getCurrentRevisionNumber();
			return this.modelState.getRevisionNumber();
		}
	}

	// implements IMemoryModel
	@Override
	public XExistsRevWritableModel getState() {
		return this.modelState;
	}

	@Override
	public XType getType() {
		return XType.XMODEL;
	}

	/**
	 * Checks whether this MemoryModel has a father-{@link XRepository} or not.
	 * 
	 * @return true, if this MemoryModel has a father-{@link XRepository}, false
	 *         otherwise.
	 */
	@ReadOperation
	private boolean hasFather() {
		return this.father != null;
	}

	@Override
	@ReadOperation
	public int hashCode() {
		synchronized (getRoot()) {
			int hashCode = getId().hashCode() + (int) getRevisionNumber();

			if (this.father != null) {
				hashCode += this.father.getId().hashCode();
			}

			return hashCode;
		}
	}

	@Override
	@ReadOperation
	public boolean hasObject(@NeverNull XId id) {
		synchronized (getRoot()) {
			assertThisEntityExists();
			return this.modelState.hasObject(id);
		}
	}

	@Override
	@ReadOperation
	public boolean isEmpty() {
		synchronized (getRoot()) {
			assertThisEntityExists();
			return this.modelState.isEmpty();
		}
	}

	@Override
	public boolean isSynchronized() {
		return getRevisionNumber() <= getSynchronizedRevision();
	}

	@Override
	@ReadOperation
	public Iterator<XId> iterator() {
		synchronized (getRoot()) {
			assertThisEntityExists();
			return this.modelState.iterator();
		}
	}

	@Override
	public boolean removeListenerForFieldEvents(XFieldEventListener changeListener) {
		synchronized (getRoot()) {
			return getRoot().removeListenerForFieldEvents(getAddress(), changeListener);
		}
	}

	// implement IMemoryModel
	@Override
	public boolean removeListenerForModelEvents(XModelEventListener changeListener) {
		synchronized (getRoot()) {
			return getRoot().removeListenerForModelEvents(getAddress(), changeListener);
		}
	}

	@Override
	public boolean removeListenerForObjectEvents(XObjectEventListener changeListener) {
		synchronized (getRoot()) {
			return getRoot().removeListenerForObjectEvents(getAddress(), changeListener);
		}
	}

	@Override
	public boolean removeListenerForTransactionEvents(XTransactionEventListener changeListener) {
		synchronized (getRoot()) {
			return getRoot().removeListenerForTransactionEvents(getAddress(), changeListener);
		}
	}

	@Override
	@ModificationOperation
	public boolean removeObject(@NeverNull XId objectId) {
		/*
		 * no synchronisation necessary here (except that in
		 * executeModelCommand())
		 */
		XModelCommand command = MemoryModelCommand.createRemoveCommand(getAddress(),
				XCommand.FORCED, objectId);
		long result = executeModelCommand(command);
		XyAssert.xyAssert(result >= 0 || result == XCommand.NOCHANGE);
		return result != XCommand.NOCHANGE;
	}

	// implement XSynchronizesChanges
	@Override
	public void setSessionActor(XId actorId, String passwordHash) {
		getRoot().setSessionActor(actorId);
		getRoot().setSessionPasswordHash(passwordHash);
	}

	@Override
	@ReadOperation
	public String toString() {
		return this.getId() + " rev[" + this.getRevisionNumber() + "]";
	}

	@Override
	public boolean exists() {
		return this.modelState.exists();
	}

	@Override
	public void setExists(boolean entityExists) {
		this.modelState.setExists(entityExists);
	}
}
