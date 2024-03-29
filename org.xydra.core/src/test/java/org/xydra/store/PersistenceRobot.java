package org.xydra.store;

import java.util.ArrayList;
import java.util.List;

import org.xydra.base.Base;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XAddress;
import org.xydra.base.XCompareUtils;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.util.DumpUtilsBase;
import org.xydra.base.value.XValue;
import org.xydra.core.XCopyUtils;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.GetWithAddressRequest;
import org.xydra.persistence.XydraPersistence;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.impl.memory.MemoryPersistence;
import org.xydra.store.rmof.impl.delegate.WritableRepositoryOnPersistence;


/**
 * A robot that acts on a {@link XydraPersistence} and does random actions.
 *
 * If the compile-time flag {@link #ONLY_ADD} is true, then: models, objects and
 * fields are only created, never deleted. Values are only added or changed,
 * never deleted.
 *
 * Thus any state seen by any thread can only grow. This allows to spot an error
 * we currently encounter: After some operations in multi-threading use, some
 * parts of the MOF-tree disappear from time to time on the remote server.
 *
 * The technique to spot the error is this: Random commands are created and
 * executed remotely. If they succeed, they are also executed locally. Thus
 * local and remote should be in the same tree-state (version numbers might
 * differ).
 *
 * If this happens, the client-side robot shows a dump of local and remote
 * snapshot and exists the JVM.
 *
 * @author xamde
 */
public class PersistenceRobot extends Thread {

	private static final Logger log = LoggerFactory.getLogger(PersistenceRobot.class);

	public static final boolean INCLUDE_TENTATIVE_STATE = true;

	private static final String MODELPREFIX = "a7";

	public static boolean ONLY_ADD = true;

	private final XId executingActorId;

	private final String id;

	public PersistenceRobot(final XydraPersistence remote, final XId executingActorId, final String id) {
		super();
		this.remote = remote;
		this.executingActorId = executingActorId;
		this.id = id;
	}

	private final XydraPersistence remote;

	private XydraPersistence local;

	private XId repositoryId;

	private WritableRepositoryOnPersistence localRepo;

	private List<XAddress> modelAddresses;

	private List<XAddress> objectAddresses;

	private List<XAddress> fieldAddresses;

	private List<XAddress> allAddresses;

	private List<XValue> values;

	private static final int MODELS = 3;
	private static final int OBJECT_PER_MODEL = 3;
	private static final int FIELDS_PER_OBJECT = 3;

	public void init() {
		this.repositoryId = this.remote.getRepositoryId();
		this.local = new MemoryPersistence(this.repositoryId);
		this.localRepo = new WritableRepositoryOnPersistence(this.local, this.executingActorId);
		this.modelAddresses = new ArrayList<XAddress>();
		this.objectAddresses = new ArrayList<XAddress>();
		this.fieldAddresses = new ArrayList<XAddress>();

		for(int i = 0; i < MODELS; i++) {
			this.modelAddresses.add(Base.toAddress(this.repositoryId,
			        Base.toId(MODELPREFIX + "model" + i), null, null));
			for(int j = 0; j < OBJECT_PER_MODEL; j++) {
				this.objectAddresses.add(Base.toAddress(this.repositoryId,
				        Base.toId(MODELPREFIX + "model" + i), Base.toId("object" + j), null));
				for(int k = 0; k < FIELDS_PER_OBJECT; k++) {
					this.fieldAddresses.add(Base.toAddress(this.repositoryId,
					        Base.toId(MODELPREFIX + "model" + i), Base.toId("object" + j),
					        Base.toId("field" + k)));
				}
			}
		}

		this.allAddresses = new ArrayList<XAddress>();
		this.allAddresses.addAll(this.modelAddresses);
		this.allAddresses.addAll(this.objectAddresses);
		this.allAddresses.addAll(this.fieldAddresses);

		// create some values
		this.values = new ArrayList<XValue>();
		for(int i = 0; i < 100; i++) {
			this.values.add(BaseRuntime.getValueFactory().createStringValue("AAA" + i));
		}

		// initially load all models to local repo and create if required
		final XWritableModel[] snapshots = new XWritableModel[this.modelAddresses.size()];
		for(int i = 0; i < this.modelAddresses.size(); i++) {
			snapshots[i] = this.remote.getModelSnapshot(new GetWithAddressRequest(
			        this.modelAddresses.get(i), INCLUDE_TENTATIVE_STATE));
			final XId modelId = this.modelAddresses.get(i).getModel();
			if(snapshots[i] == null) {
				final XRepositoryCommand cmd = BaseRuntime.getCommandFactory().createAddModelCommand(
				        this.repositoryId, modelId, true);
				this.remote.executeCommand(this.executingActorId, cmd);
				this.local.executeCommand(this.executingActorId, cmd);
			} else {
				// put in local repo
				XCopyUtils.copyData(snapshots[i], this.localRepo.createModel(modelId));
			}
		}
	}

	public XCommand doAction() {

		// choose an address
		XAddress target = chooseFromList(this.allAddresses);
		log.debug(this.id + "> Will do action on a " + target.getAddressedType());
		tryToLoad(target);
		XCommand cmd = null;

		if(localHasAddress(target)) {
			log.debug(this.id + "> " + target + " exists");

			if(target.getAddressedType() == XType.XFIELD) {
				// change or delete
				final boolean change = ONLY_ADD || Math.random() > 0.1d;
				if(change) {
					log.debug(this.id + "> " + target + " change field");
					cmd = createCommandToChangeField(target);
				} else {
					log.debug(this.id + "> " + target + " delete field");
					cmd = createCommandToDelete(target);
				}
			} else if(!ONLY_ADD) {
				// always delete
				log.debug(this.id + "> delete " + target);
				cmd = createCommandToDelete(target);
			}
		} else {
			// create
			while(target.getParent().getAddressedType() != XType.XREPOSITORY
			        && !localHasAddress(target.getParent())) {
				target = target.getParent();
			}
			log.debug(this.id + "> need to create " + target.getAddressedType() + " = " + target);
			cmd = createCommandToCreate(target);
		}

		if(cmd != null) {
			// execute
			final long result = this.remote.executeCommand(this.executingActorId, cmd);

			// if success, do also in local persistence
			if(result >= 0 || result == XCommand.NOCHANGE) {
				final long localResult = this.local.executeCommand(this.executingActorId, cmd);
				XyAssert.xyAssert(localResult >= 0 || result == XCommand.NOCHANGE);
				log.debug(this.id + "> command executed with result remote=" + result + " local="
				        + localResult);
				// get snapshot and compare with local
				compareSnapshotsFor(target);
			} else {
				log.warn(this.id + "> Failed to execute " + cmd + " with result=" + result);
			}
		}
		return cmd;

	}

	private void compareSnapshotsFor(final XAddress target) {
		// get remote snapshot to compare state
		final GetWithAddressRequest request = new GetWithAddressRequest(Base.resolveModel(target),
		        INCLUDE_TENTATIVE_STATE);
		final XWritableModel remoteModel = this.remote.getModelSnapshot(request);
		final XWritableModel localModel = this.local.getModelSnapshot(request);
		boolean compare;
		if(ONLY_ADD) {
			compare = XCompareUtils.containsTree(remoteModel, localModel);
		} else {
			compare = XCompareUtils.equalTree(localModel, remoteModel);
		}
		if(!compare) {
			log.debug(this.id + "> --- spot the difference ---");
			DumpUtilsBase.dump(this.id + "> remote", remoteModel);
			DumpUtilsBase.dump(this.id + "> local", localModel);
			log.debug(this.id + "> Exiting...");
			System.exit(1);
		}
	}

	private XCommand createCommandToCreate(final XAddress target) {
		switch(target.getAddressedType()) {
		case XREPOSITORY:
			throw new IllegalArgumentException("Not allowed");
		case XMODEL:
			return BaseRuntime.getCommandFactory().createForcedAddModelCommand(this.repositoryId,
			        target.getModel());
		case XOBJECT:
			return BaseRuntime.getCommandFactory().createForcedAddObjectCommand(Base.resolveModel(target),
			        target.getObject());
		case XFIELD:
			return BaseRuntime.getCommandFactory().createForcedAddFieldCommand(Base.resolveObject(target),
			        target.getField());
		}
		// dead code
		return null;
	}

	private XCommand createCommandToChangeField(final XAddress target) {
		XyAssert.xyAssert(target.getAddressedType() == XType.XFIELD);

		final XWritableField field = this.localRepo.getModel(target.getModel())
		        .getObject(target.getObject()).getField(target.getField());
		if(field.isEmpty()) {
			log.debug(this.id + "> field " + target + " is empty, add value");
			// create value
			return BaseRuntime.getCommandFactory().createForcedAddValueCommand(target,
			        chooseFromList(this.values));
		} else {
			// change or remove value
			final boolean change = ONLY_ADD || Math.random() > 0.1;
			if(change) {
				log.debug(this.id + "> change value of field " + target + " from "
				        + field.getValue());
				return BaseRuntime.getCommandFactory().createForcedChangeValueCommand(target,
				        chooseFromList(this.values));
			} else {
				log.debug(this.id + "> remove value of field " + target);
				return BaseRuntime.getCommandFactory().createForcedRemoveValueCommand(target);
			}
		}
	}

	private static XCommand createCommandToDelete(final XAddress target) {
		switch(target.getAddressedType()) {
		case XREPOSITORY:
			throw new IllegalArgumentException("Not allowed");
		case XMODEL:
			return BaseRuntime.getCommandFactory().createForcedRemoveModelCommand(target);
		case XOBJECT:
			return BaseRuntime.getCommandFactory().createForcedRemoveObjectCommand(target);
		case XFIELD:
			return BaseRuntime.getCommandFactory().createForcedRemoveFieldCommand(target);
		}
		// dead code
		return null;
	}

	private void tryToLoad(final XAddress target) {
		if(target.getAddressedType() == XType.XMODEL) {
			tryToLoadModel(target);
		} else {
			tryToLoadObject(target);
		}
	}

	private void tryToLoadObject(final XAddress target) {
		final XAddress objectAddress = Base.resolveObject(target);
		final XWritableObject objectSnapshot = this.remote.getObjectSnapshot(new GetWithAddressRequest(
		        objectAddress, INCLUDE_TENTATIVE_STATE));
		if(objectSnapshot != null) {
			// put in local repo
			final XWritableModel localModel = this.localRepo.getModel(target.getModel());
			XyAssert.xyAssert(localModel != null); assert localModel != null;
			final XWritableObject localObject = localModel.createObject(target.getObject());
			assert localObject != null : "Remote has snapshot " + objectAddress
			        + " but local is missing the object";
			XCopyUtils.copyData(objectSnapshot, localObject);
		}
	}

	private void tryToLoadModel(final XAddress target) {
		final XWritableModel snapshot = this.remote.getModelSnapshot(new GetWithAddressRequest(target,
		        INCLUDE_TENTATIVE_STATE));
		if(snapshot != null) {
			// put in local repo
			final XWritableModel localModel = this.localRepo.getModel(target.getModel());
			XCopyUtils.copyData(snapshot, localModel);
		}
	}

	private boolean localHasAddress(final XAddress target) {
		XyAssert.xyAssert(target.getAddressedType() != XType.XREPOSITORY);
		final XWritableModel model = this.localRepo.getModel(target.getModel());
		if(model != null) {
			if(target.getAddressedType() == XType.XMODEL) {
				return true;
			}
			final XWritableObject object = model.getObject(target.getObject());
			if(object != null) {
				if(target.getAddressedType() == XType.XOBJECT) {
					return true;
				}
				final XWritableField field = object.getField(target.getField());
				if(field != null) {
					return true;
				}
			}
		}
		return false;
	}

	public static <T> T chooseFromList(final List<T> list) {
		final int i = (int)(Math.random() * list.size());
		return list.get(i);
	}

	private static final int MAX_ACTIONS = 40;

	@Override
	public void run() {
		init();
		for(int i = 0; i < MAX_ACTIONS; i++) {
			log.info(this.id + "> Action " + i + "/" + MAX_ACTIONS);
			final XCommand cmd = doAction();
			if(cmd != null) {
				log.debug(this.id + "> Executed " + cmd);
			}
		}
	}

}
