package org.xydra.store.impl.gae.ng;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

import org.xydra.annotations.Setting;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XWritableModel;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.index.impl.IteratorUtils;
import org.xydra.index.iterator.AbstractFilteringIterator;
import org.xydra.index.query.Pair;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.restless.utils.NanoClock;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.ModelRevision;
import org.xydra.store.impl.gae.DebugFormatter;
import org.xydra.store.impl.gae.FutureUtils;
import org.xydra.store.impl.gae.GaeOperation;
import org.xydra.store.impl.gae.IGaeModelPersistence;
import org.xydra.store.impl.gae.changes.GaeChange;
import org.xydra.store.impl.gae.changes.GaeChange.Status;
import org.xydra.store.impl.gae.changes.GaeLocks;
import org.xydra.store.impl.gae.changes.IGaeChangesService;
import org.xydra.store.impl.gae.changes.VoluntaryTimeoutException;
import org.xydra.store.impl.gae.ng.GaeModelRevInfo.Precision;
import org.xydra.store.impl.gae.snapshot.IGaeSnapshotService;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;


/**
 * Xydra allows transaction only within one model. The GAE implementation
 * maintains one change log per model. This class keeps all access to a model
 * within the datastore and memcache in one place.
 * 
 * If the datastore or memcache is called to read or write a certain model, that
 * access is triggered only from here.
 * 
 * 
 * Design decisions:
 * 
 * Chosen: 1A) Within 60 seconds (or another short timeout) the caller gets a
 * notice if her command got executed successfully, failed or got a timeout.
 * This gives always a quick response.
 * 
 * Alternative: 1B) Commands get enqueued, caller gets an event number and can
 * later check what has happened with the command. The command might wait for
 * some locks and take minutes for execution. This produces less dead change
 * events in the log.
 * 
 * @author xamde
 */
public class GaeModelPersistenceNG implements IGaeModelPersistence {
	
	static final Logger log = LoggerFactory.getLogger(GaeModelPersistenceNG.class);
	
	/* How many changes to fetch in one batch fetch at most */
	@Setting(value = "")
	private static final long MAX_CHANGES_FETCH_SIZE = 50;
	
	/**
	 * Initial time to wait before re-checking the status of an event who'se
	 * locks we need.
	 */
	@Setting(value = "")
	private static final long WAIT_INITIAL = 10;
	
	/**
	 * Maximum time to wait before re-checking the status of an event who's
	 * locks we need.
	 */
	@Setting(value = "")
	private static final long WAIT_MAX = 1000;
	
	private ChangeLogManager changelogManager;
	
	private final XAddress modelAddress;
	
	private RevisionManager revisionManager;
	
	private IGaeSnapshotService snapshotService;
	
	private TentativeSnapshotManagerAtomic baseSnapshotManager;
	
	private TentativeSnapshotManagerInTransaction tentativeSnapshotManager;
	
	public GaeModelPersistenceNG(XAddress modelAddress) {
		this.modelAddress = modelAddress;
		this.revisionManager = new RevisionManager(this.modelAddress);
		this.changelogManager = new ChangeLogManager(this.modelAddress, this.revisionManager);
		this.snapshotService = new GaeSnapshotServiceImplNG(this.changelogManager);
		this.baseSnapshotManager = new TentativeSnapshotManagerAtomic(modelAddress,
		        this.revisionManager, this.snapshotService);
		this.tentativeSnapshotManager = new TentativeSnapshotManagerInTransaction(
		        this.baseSnapshotManager);
	}
	
	/**
	 * Phase 2: Depending on the command, fetch the required information to
	 * compute if the command is legal -- and if so -- what events results from
	 * executing it. I.e. there are implied events to be considered.
	 * 
	 * As the locks synchronised access, the currently locked parts are stable,
	 * i.e. not being changed by other parts. They reflect the state before this
	 * command and can be considered the current rev -- even if other command
	 * are still working in parallel on irrelevant parts.
	 * 
	 * @param command
	 * @param gaeLocks
	 * @return
	 */
	private ExecutionResult checkPreconditionsComputeEventsUpdateTOS(XCommand command,
	        GaeChange change) {
		ExecutionResult result;
		this.tentativeSnapshotManager.clearCaches();
		if(command.getChangeType() == ChangeType.TRANSACTION) {
			result = checkPreconditionsAndComputeEvents_txn((XTransaction)command, change);
		} else {
			if(command.getTarget().getAddressedType() == XType.XREPOSITORY) {
				result = checkPreconditionsAndComputeEvents_repository((XRepositoryCommand)command,
				        change);
			} else {
				result = ExecuteDecideAlgorithms.checkPreconditionsAndComputeEvents_atomic(
				        (XAtomicCommand)command, change, false, this.tentativeSnapshotManager);
			}
		}
		
		if(result.getStatus() == Status.SuccessExecuted) {
			this.tentativeSnapshotManager.saveTentativeObjectSnapshots();
		}
		
		return result;
	}
	
	/**
	 * @param rc
	 * @return
	 */
	private ExecutionResult checkPreconditionsAndComputeEvents_repository(XRepositoryCommand rc,
	        GaeChange change) {
		
		GaeModelRevInfo info = this.revisionManager.getInfo();
		boolean modelExists = info.isModelExists();
		
		switch(rc.getChangeType()) {
		case ADD:
			if(!modelExists) {
				return ExecutionResult.successCreatedModel(rc, change,
				        info.getLastStableSuccessChange());
			} else if(rc.isForced()) {
				return ExecutionResult.successNoChange("Model exists");
			} else {
				return ExecutionResult.failed("Safe RepositoryCommand ADD failed; model!=null");
			}
			
		case REMOVE:
			long modelRev = this.revisionManager.getInfo().getLastStableSuccessChange();
			if((!modelExists || modelRev != rc.getRevisionNumber()) && !rc.isForced()) {
				return ExecutionResult.failed("Safe RepositoryCommand REMOVE failed. Reason: "
				        + (!modelExists ? "model is null" : "modelRevNr:" + modelRev + " cmdRevNr:"
				                + rc.getRevisionNumber() + " forced:" + rc.isForced()));
			} else if(modelExists) {
				log.debug("Removing model " + this.modelAddress + " " + modelRev);
				return ExecutionResult.successRemovedModel(change, rc,
				        this.tentativeSnapshotManager);
			} else {
				return ExecutionResult.successNoChange("Model did not exist");
			}
			
		default:
			throw new AssertionError("XRepositoryCommand with unexpected type: " + rc);
		}
	}
	
	/**
	 * Apply the {@link XCommand XCommands} contained in the given
	 * {@link XTransaction}. If one of the {@link XCommand XCommands} failed,
	 * the {@link XTransaction} will remain partially applied, already executed
	 * {@link XCommand XCommands} will not be rolled back.
	 * 
	 * @param transaction The {@link XTransaction} which is to be executed
	 * @return the {@link ExecutionResult}
	 * 
	 *         TODO it might be a good idea to tell the caller of this method
	 *         which commands of the transaction were executed and not only
	 *         return false
	 */
	private ExecutionResult checkPreconditionsAndComputeEvents_txn(XTransaction transaction,
	        GaeChange change) {
		
		ITentativeSnapshotManager tmsInTxn = new TentativeSnapshotManagerInTransaction(
		        this.tentativeSnapshotManager);
		
		List<ExecutionResult> results = new LinkedList<ExecutionResult>();
		for(int i = 0; i < transaction.size(); i++) {
			XAtomicCommand command = transaction.getCommand(i);
			ExecutionResult atomicResult = ExecuteDecideAlgorithms
			        .checkPreconditionsAndComputeEvents_atomic(command, change, true, tmsInTxn);
			if(atomicResult.getStatus().isFailure()) {
				return ExecutionResult.failed("txn failed at command " + command + " Reason: "
				        + atomicResult.getDebugHint());
			}
			
			results.add(atomicResult);
		}
		return ExecutionResult.successTransaction(transaction, change, results);
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof GaeModelPersistenceNG
		        && ((GaeModelPersistenceNG)other).modelAddress.equals(this.modelAddress);
		
	}
	
	/**
	 * Write the events that describe the transformation of the model into the
	 * new state.
	 * 
	 * Assumes that we have all the required locks.
	 * 
	 * @param change The change that the command belongs to.
	 * @param partialSnapshot on which the command is executed, can be null
	 */
	private void execute_saveEventsReleaseLocks(ExecutionResult executionResult, GaeChange change)
	        throws VoluntaryTimeoutException {
		if(executionResult.getStatus().isFailure()) {
			change.giveUpIfTimeoutCritical();
			this.changelogManager.commitAndClearLocks(change, Status.FailedPreconditions);
			return;
		}
		XyAssert.xyAssert(executionResult.getStatus().isSuccess());
		
		List<XAtomicEvent> atomicEvents = executionResult.getAtomicEvents();
		log.debug("[r" + change.rev + "] generated " + atomicEvents.size() + " events");
		if(atomicEvents.size() > 1000) {
			log.warn("Created over 1000 events (" + atomicEvents.size()
			        + ") GA?category=xydra&action=saveManyEvents&label=events&value="
			        + atomicEvents.size());
			try {
				throw new RuntimeException("Over 1000 events");
			} catch(Exception e) {
				log.warn("Over 1000 events", e);
			}
		}
		XyAssert.xyAssert(atomicEvents != null);
		if(atomicEvents.isEmpty()) {
			change.giveUpIfTimeoutCritical();
			this.changelogManager.commitAndClearLocks(change, Status.SuccessNochange);
			log.debug("No change");
			return;
		}
		Pair<int[],List<Future<Key>>> res = change.setEvents(atomicEvents);
		// Wait on all changes.
		for(Future<Key> future : res.getSecond()) {
			FutureUtils.waitFor(future);
		}
		
		change.giveUpIfTimeoutCritical();
		this.changelogManager.commitAndClearLocks(change, Status.SuccessExecuted);
	}
	
	/**
	 * Phase 2: Check locks from our change number on downwards: We check all
	 * changes until:
	 * 
	 * a) a change is timed out. We try to progress the change.
	 * 
	 * b) the change is 'Creating' and has a lock that we need: We give up.
	 * 
	 * c) we reached the last known lastSilentCommited number: We know we have
	 * all required locks and execute.
	 * 
	 * We check by batch-fetching. We have a maximal window size (might need to
	 * be smaller when large values are there). 1 MB fetch size, max size of
	 * change ca. 10 objects each with 4k => 40k. So we fetch 25 changes.
	 * 
	 * @param ourChange waiting to be executed
	 * @throws VoluntaryTimeoutException
	 */
	private void execute_waitForLocks(GaeChange ourChange) throws VoluntaryTimeoutException {
		long lastCommited = this.revisionManager.getInfo().getLastCommitted();
		XyAssert.xyAssert(lastCommited == -1 || ourChange.rev > lastCommited,
		        "If a lastSilentCommitted is not undefiend (i.e. == -1), "
		                + "it must be before this change");
		
		Interval pendingChangesSearchRange = new Interval(lastCommited, ourChange.rev - 1);
		if(pendingChangesSearchRange.isEmpty()) {
			/* working window is zero, there are no pending changes */
			return;
		}
		
		Interval fetchRange = pendingChangesSearchRange.copy();
		fetchRange.adjustStartToFitSizeIfNecessary(MAX_CHANGES_FETCH_SIZE);
		
		List<GaeChange> changes = this.changelogManager.getChanges(fetchRange);
		for(int i = 0; i < changes.size(); i++) {
			GaeChange otherChange = changes.get(i);
			/* Always check if a change is timed-out, and if so, progress it */
			if(this.changelogManager.progressChangeIfTimedOut(otherChange)) {
				/* now it cannot have any locks */
				continue;
			}
			if(otherChange.getStatus().isCommitted()) {
				this.revisionManager.foundNewHigherCommitedChange(otherChange);
				continue;
			} else {
				/* its pending, somebody else is just working on it */
				GaeLocks nextLocks = otherChange.getLocks();
				if(nextLocks.isConflicting(ourChange.getLocks())) {
					execute_waitForOtherThreadToCommit(ourChange, otherChange);
				}
			}
		}
	}
	
	/**
	 * The nextChange is uncommitted and holds conflicting locks, so we need to
	 * wait. Waiting is done by sleeping increasing intervals and then checking
	 * the change entity again.
	 * 
	 * The locks that we already "acquired" cannot be released before entering
	 * the waiting mode, as releasing them before completely executing our own
	 * change would allow other changes with conflicting locks and a revision
	 * greater than ours to execute before our own change.
	 * 
	 * @param ourChange
	 * @param nextChange is in status 'Creating' and we wait for its commit
	 * @throws VoluntaryTimeoutException
	 */
	private void execute_waitForOtherThreadToCommit(GaeChange ourChange, GaeChange nextChange)
	        throws VoluntaryTimeoutException {
		long waitTime = WAIT_INITIAL;
		while(!(nextChange.isTimedOut())) {
			
			ourChange.giveUpIfTimeoutCritical();
			
			/*
			 * IMPROVE save own command if waitTime is too long (so that we can
			 * be rolled forward in case of timeout)
			 */
			try {
				Thread.sleep(waitTime);
			} catch(InterruptedException e) {
				// ignore interrupt
			}
			// IMPROVE update own lastActivity?
			
			nextChange.reload();
			
			if(nextChange.getStatus().isCommitted()) {
				XyAssert.xyAssert(!nextChange.hasLocks(),
				        "now finished, so cannot have no locks anymore");
				this.revisionManager.foundNewHigherCommitedChange(nextChange);
				return;
			}
			
			waitTime = Algorithms.increaseExponentiallyWithFactorAndMaximum(waitTime, 2, WAIT_MAX);
		}
		
		XyAssert.xyAssert(nextChange.isTimedOut(), "nextChange timed out");
		this.changelogManager.commitAndClearLocks(nextChange, Status.FailedTimeout);
	}
	
	/**
	 * Responsible for executing changes for one model, in the datastore.
	 * 
	 * This variant checks preconditions by retrieving the latest snapshots and
	 * updating those parts that the executing change has locked. There is no
	 * separate executing step that can be rolled forward.
	 * 
	 * There is no additional state stored in the GAE datastore besides that
	 * used by the {@link IGaeChangesService} and {@link IGaeSnapshotService}
	 * implementations.
	 * 
	 * @param command to be executed
	 * @param actorId
	 * @return the resulting revision number
	 */
	@Override
	public long executeCommand(XCommand command, XID actorId) throws VoluntaryTimeoutException {
		log.debug("----------------------------------------- Execute "
		        + DebugFormatter.format(command));
		NanoClock c = new NanoClock().start();
		XyAssert.xyAssert(this.modelAddress.equalsOrContains(command.getChangedEntity()),
		        "cannot handle command " + command + " - it does not address a model");
		
		GaeLocks locks = GaeLocks.createLocks(command);
		c.stopAndStart("createlocks");
		XyAssert.xyAssert(locks.size() > 0, "no locks created for command %s", command);
		
		/*
		 * Phase 1: Starting from lastTaken try to execute a GAE-transaction
		 * that writes the locks for this command into the change log. Our
		 * status: Creating
		 */
		// FIXME !!! log less
		log.info("Phase 1: grabRevisionAndRegister " + locks.size() + " locks = " + locks);
		GaeChange change = grabRevisionAndRegisterLocks(locks, actorId);
		XyAssert.xyAssert(change.rev >= 0);
		c.stopAndStart("grabRevisionAndRegisterLocks");
		
		/* Phase 2: Entering synchronised code ... */
		// FIXME log less
		log.info("Phase 1: waitForLocks");
		execute_waitForLocks(change);
		c.stopAndStart("waitForLocks");
		
		/* --- Code synchronised by Xydra locks in GAE datastore --- */
		/* Phase 3 */
		// FIXME log less
		log.info("[r" + change.rev + "] Phase 3: check constraints, compute events = " + change
		        + ", command = " + command);
		ExecutionResult executionResult = checkPreconditionsComputeEventsUpdateTOS(command, change);
		/* --- End of code synchronised by Xydra locks in GAE datastore --- */
		
		/* Phase 4: Write result in change-log, releasing the locks ... */
		// FIXME log less
		log.info("[r" + change.rev + "] Phase 4: saveEvents '" + executionResult.getStatus().name()
		        + "' change = " + change + ", command = " + command);
		execute_saveEventsReleaseLocks(executionResult, change);
		c.stopAndStart("saveEvents");
		XyAssert.xyAssert(change.getStatus().isCommitted(),
		        "If we reach this line, change must be committed");
		
		this.revisionManager.foundNewHigherCommitedChange(change);
		
		if(log.isInfoEnabled() || executionResult.getStatus().isFailure() && log.isWarnEnabled()) {
			String msg = "[r"
			        + change.rev
			        + "] -> "
			        + executionResult.getStatus()
			        + "."
			        +
			        
			        (executionResult.getDebugHint() != null ? " Reason: "
			                + executionResult.getDebugHint() : "")
			        
			        + " Stats: " + c.getStats();
			if(executionResult.getStatus().isFailure()) {
				log.warn("+++ " + msg);
			} else {
				log.info("+++ " + msg);
			}
		}
		
		switch(executionResult.getStatus()) {
		case FailedPreconditions:
			return XCommand.FAILED;
		case FailedTimeout:
			return XCommand.FAILED;
		case SuccessExecuted:
			return change.rev;
		case SuccessNochange:
			return XCommand.NOCHANGE;
		default:
		case Creating:
			throw new AssertionError("Cannot happen");
		}
	}
	
	@Override
	public List<XEvent> getEventsBetween(XAddress address, long beginRevision, long endRevision) {
		List<XEvent> events = this.changelogManager.getEventsBetween(beginRevision, endRevision);
		if(address.getAddressedType() == XType.XMODEL) {
			XyAssert.xyAssert(address.equals(this.modelAddress));
			/**
			 * Fullfill the XydraStore spec and return null if the model has not
			 * been managed
			 */
			if(events.size() == 0) {
				return null;
			}
			return events;
		} else {
			XyAssert.xyAssert(XX.resolveModel(address).equals(this.modelAddress), "", address,
			        this.modelAddress);
			AbstractFilteringIterator<XEvent> it = new AbstractFilteringIterator<XEvent>(
			        events.iterator()) {
				
				@Override
				protected boolean matchesFilter(XEvent entry) {
					return XX.resolveModel(entry.getTarget()).equals(
					        GaeModelPersistenceNG.this.modelAddress);
				}
				
			};
			return IteratorUtils.addAll(it, new LinkedList<XEvent>());
		}
	}
	
	/**
	 * @param includeTentative if true, then in addition to the stable model
	 *            revision number also the unstable tentative revision number is
	 *            calculated.
	 * @return the current {@link ModelRevision} or null
	 */
	@Override
	public ModelRevision getModelRevision(boolean includeTentative) {
		GaeModelRevInfo info = this.revisionManager.getInfo();
		if(info.getPrecision() != Precision.Precise) {
			// compute
			computeMorePreciseCurrentRev();
		}
		
		long revision = info.getLastStableSuccessChange();
		boolean modelExists = info.isModelExists();
		ModelRevision modelRev;
		if(includeTentative) {
			long tentativeRevision = info.getLastSuccessChange();
			modelRev = new ModelRevision(revision, modelExists, tentativeRevision);
		} else {
			modelRev = new ModelRevision(revision, modelExists);
		}
		return modelRev;
	}
	
	private void computeMorePreciseCurrentRev() {
		long lastCommited = this.revisionManager.getInfo().getLastStableCommitted();
		
		Interval currentSearchRange = new Interval(lastCommited, lastCommited
		        + MAX_CHANGES_FETCH_SIZE);
		
		while(true) {
			List<GaeChange> changes = this.changelogManager.getChanges(currentSearchRange);
			if(changes.isEmpty()) {
				this.revisionManager.getInfo().setPrecision(Precision.Precise);
				return;
			}
			for(int i = 0; i < changes.size(); i++) {
				GaeChange change = changes.get(i);
				if(change == null) {
					this.revisionManager.getInfo().setPrecision(Precision.Precise);
					return;
				}
				/* Always check if a change is timed-out, and if so, progress it */
				if(this.changelogManager.progressChangeIfTimedOut(change)) {
					// revision was incremented
				} else if(change.getStatus().isCommitted()) {
					this.revisionManager.foundNewHigherCommitedChange(change);
				} else {
					/* its pending, somebody else is just working on it */
					this.revisionManager.getInfo().setPrecision(Precision.Precise);
					return;
				}
			}
			currentSearchRange = currentSearchRange.moveRight();
		}
	}
	
	@Override
	public XWritableObject getObjectSnapshot(XID objectId, boolean includeTentative) {
		if(includeTentative) {
			// short-cut
			TentativeObjectSnapshot tos = this.tentativeSnapshotManager
			        .getTentativeObjectSnapshot(XX.resolveObject(this.modelAddress, objectId));
			if(tos == null) {
				return null;
			}
			if(tos.isObjectExists()) {
				return tos.asRevWritableObject();
			} else {
				return null;
			}
		}
		
		// slow
		XWritableModel snapshot = getSnapshot(includeTentative);
		if(snapshot == null) {
			return null;
		}
		return snapshot.getObject(objectId);
	}
	
	@Override
	synchronized public XWritableModel getSnapshot(boolean includeTentative) {
		if(!this.revisionManager.getInfo().isModelExists()) {
			return null;
		}
		
		long modelRev = this.revisionManager.getInfo().getLastStableSuccessChange();
		XRevWritableModel snapshot = this.snapshotService.getModelSnapshot(modelRev,
		        !includeTentative);
		
		log.debug("return snapshot rev " + snapshot.getRevisionNumber() + " for model "
		        + this.modelAddress);
		return snapshot;
	}
	
	/**
	 * Grabs the lowest available revision number and registers a change for
	 * that revision number with the provided locks.
	 * 
	 * @param lastTaken
	 * 
	 * @param locks which locks to get
	 * @param actorId The actor to record in the change {@link Entity}.
	 * @return Information associated with the change such as the grabbed
	 *         revision, the locks, the start time and the change {@link Entity}
	 *         .
	 * 
	 *         Note: Reads revCache.lastTaken
	 */
	@GaeOperation(memcacheRead = true ,datastoreRead = true ,datastoreWrite = true ,memcacheWrite = true)
	GaeChange grabRevisionAndRegisterLocks(GaeLocks locks, XID actorId) {
		long lastTaken = this.revisionManager.getInfo().getLastTaken();
		XyAssert.xyAssert(lastTaken >= -1);
		long start = lastTaken + 1;
		
		GaeChange change = this.changelogManager
		        .grabRevisionAndRegisterLocks(locks, actorId, start);
		return change;
	}
	
	@Override
	public int hashCode() {
		return this.modelAddress.hashCode();
	}
	
	@Override
	public boolean modelHasBeenManaged() {
		GaeChange change = this.changelogManager.getChange(0);
		return change != null;
	}
	
}
