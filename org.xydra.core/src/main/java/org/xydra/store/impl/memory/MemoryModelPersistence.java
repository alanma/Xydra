package org.xydra.store.impl.memory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.impl.memory.RevisionConstants;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.impl.memory.SimpleModel;
import org.xydra.core.XCopyUtils;
import org.xydra.core.change.EventUtils;
import org.xydra.core.model.delta.ChangedModel;
import org.xydra.core.model.delta.DeltaUtils;
import org.xydra.core.model.impl.memory.Executor;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.ModelRevision;


/**
 * A helper class used by {@link MemoryPersistence} to manage individual models.
 *
 * @author dscharrer
 *
 */

public class MemoryModelPersistence {

    static private Logger log = LoggerFactory.getLogger(MemoryModelPersistence.class);

    private final List<XEvent> events = new ArrayList<XEvent>();

    /**
     * The current state of the model, or null if the model doesn't currently
     * exist.
     */
    private final XRevWritableModel model;

    XAddress modelAddr;

    public MemoryModelPersistence(final XAddress modelAddr) {
        this.modelAddr = modelAddr;
        final SimpleModel nonExisting = new SimpleModel(modelAddr);
        nonExisting.setExists(false);
        nonExisting.setRevisionNumber(RevisionConstants.NOT_EXISTING);
        this.model = nonExisting;
    }

    synchronized public long executeCommand(final XId actorId, final XCommand command) {

        assert this.model != null;
        final ChangedModel changedModel = new ChangedModel(this.model);
        final boolean success = changedModel.executeCommand(command);
        if(!success) {
            log.warn("command " + command + " failed");
            return XCommand.FAILED;
        }
        if(!changedModel.hasChanges()) {
            return XCommand.NOCHANGE;
        }

        return applyChanes(changedModel, this.model, actorId, command, this.events);
    }

    /**
     * Public for testing only.
     *
     * @param changedModel
     * @param model
     * @param actorId
     * @param command
     * @param eventList
     * @return resulting revision number
     */
    public static long applyChanes(final ChangedModel changedModel, final XRevWritableModel model, final XId actorId,
            final XCommand command, final List<XEvent> eventList) {
        // create event
        final long currentModelRev = model.getRevisionNumber();
        final List<XAtomicEvent> events = new LinkedList<XAtomicEvent>();
        DeltaUtils.createEventsForChangedModel(events, actorId, changedModel,
                command.getChangeType() == ChangeType.TRANSACTION);
        long currentObjectRev;
        if(command.getTarget().getObject() != null) {
            // object txn
            final XRevWritableObject objectState = model.getObject(command.getTarget().getObject());
            if(objectState == null) {
                throw new IllegalArgumentException("Cannot execute an objectCommand (" + command
                        + ") on a non-existing object");
            } else {
                currentObjectRev = objectState.getRevisionNumber();
            }
        } else {
            // model txn
            currentObjectRev = RevisionConstants.REVISION_OF_ENTITY_NOT_SET;
        }

        final XEvent event = Executor.createSingleEvent(events, actorId, command.getTarget(),
                currentModelRev, currentObjectRev);

        // apply event
        EventUtils.applyEvent(model, event);
        eventList.add(event);

        return event.getRevisionNumber();

        // long newModelRev = getRevisionNumber() + 1;
        //
        // /*
        // * Parse the command and calculate the changes needed to apply it.
        // This
        // * does not actually change the model.
        // */
        // ChangedModel changedModel = DeltaUtils.executeCommand(this.model,
        // command);
        // if(changedModel == null) {
        // // There was something wrong with the command.
        // return XCommand.FAILED;
        // }
        //
        // // Create events. Do this before we destroy any necessary information
        // by
        // // changing the model.
        // List<XAtomicEvent> events = DeltaUtils.createEvents(this.modelAddr,
        // changedModel, actorId,
        // newModelRev, command.getChangeType() == ChangeType.TRANSACTION);
        // XyAssert.xyAssert(events != null);
        // assert events != null;
        //
        // if(events.isEmpty()) {
        // /*
        // * Note how this differs from the GAE implementation where failed
        // * commands produce a NoChange event
        // */
        // return XCommand.NOCHANGE;
        // }
        //
        // XEvent event;
        // if(events.size() > 1 || command.getChangeType() ==
        // ChangeType.TRANSACTION) {
        // // Create a transaction event.
        //
        // // check whether it needs to be a model or an object transaction
        // if(command.getTarget().getAddressedType() == XType.XMODEL
        // || command.getTarget().getAddressedType() == XType.XREPOSITORY) {
        // /*
        // * if the target is a repository and the list of events contains
        // * more than one event, a model with child-object was removed,
        // * which is why we need to construct a transaction event in this
        // * case.
        // */
        //
        // event = MemoryTransactionEvent.createTransactionEvent(actorId,
        // this.modelAddr,
        // events, getRevisionNumber(), XEvent.REVISION_OF_ENTITY_NOT_SET);
        // } else {
        // assert command.getTarget().getAddressedType() == XType.XOBJECT;
        //
        // event = MemoryTransactionEvent.createTransactionEvent(actorId,
        // command.getTarget(),
        // events, XEvent.REVISION_OF_ENTITY_NOT_SET, getRevisionNumber());
        // }
        //
        // } else {
        // event = events.get(0);
        // }
        // this.events.add(event);
        // XyAssert.xyAssert(this.events.get((int)newModelRev) == event);
        //
        // // Actually apply the changes.
        // this.model = DeltaUtils.applyChanges(this.modelAddr, this.model,
        // changedModel, newModelRev);
        //
        // XyAssert.xyAssert(getRevisionNumber() == newModelRev);
        // XyAssert.xyAssert(this.model == null ||
        // this.model.getRevisionNumber() == newModelRev);
        //
        // return newModelRev;
    }

    public boolean exists() {
        return this.model != null && this.model.exists();
    }

    synchronized public List<XEvent> getEvents(final XAddress address, final long beginRevision,
            final long endRevision) {

        if(this.events.isEmpty()) {
            return null;
        }

        final long currentRev = getRevisionNumber();
        final long start = beginRevision < 0 ? 0 : beginRevision;
        final long end = endRevision > currentRev ? currentRev : endRevision;

        log.info("getEvents: [" + beginRevision + "," + endRevision + "]=>[" + start + "," + end
                + "] curr:" + currentRev + " size:" + this.events.size() + " modelrev:"
                + (this.model == null ? -2 : this.model.getRevisionNumber()));

        if(start > end) {
            // happens if start >= currentRev, which is allowed
            return new ArrayList<XEvent>();
        }

        final List<XEvent> result = new ArrayList<XEvent>();

        /*
         * filter a (sub-) list
         *
         * Note: Can handle max. Integer.MAX events = 2^31 which is a lot of
         * events and the standard java containers cannot contain more anyway,
         * at least the array-based ArrayList.
         */
        for(final XEvent xe : this.events.subList((int)start, (int)end + 1)) {
            // TODO how to filter transaction events? ~Daniel
            // TODO should this filtering be done in the calling
            // DelegateToPersistenceAndArm since it needs to filter for access
            // rights anyway?
            if(address.equalsOrContains(xe.getChangedEntity())) {
                result.add(xe);
            }
        }

        return result;
    }

    /**
     * @return the snapshot or null if not found
     */
    synchronized public XRevWritableModel getModelSnapshot() {
        if(this.model == null || !this.model.exists()) {
			return null;
		}

        return XCopyUtils.createSnapshot(this.model);
    }

    synchronized public XRevWritableObject getObjectSnapshot(final XId objectId) {
        /*
         * if this model has not been created yet, there cannot be an object
         * snapshot
         */
        if(this.model == null) {
            return null;
        }

        return XCopyUtils.createSnapshot(this.model.getObject(objectId));
    }

    synchronized public long getRevisionNumber() {
        return this.events.size() - 1;
    }

    public ModelRevision getModelRevision() {
        return new ModelRevision(getRevisionNumber(), exists());
    }

}
