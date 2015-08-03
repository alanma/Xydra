package org.xydra.store.sync;

import java.util.ArrayList;
import java.util.Iterator;

import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XSyncEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.base.rmof.impl.ISyncableState;
import org.xydra.core.model.XSynchronizesChanges;
import org.xydra.core.model.impl.memory.EventDelta;
import org.xydra.core.model.impl.memory.IMemoryMOFEntity;
import org.xydra.core.model.impl.memory.sync.IEventMapper;
import org.xydra.core.model.impl.memory.sync.IEventMapper.IMappingResult;
import org.xydra.core.model.impl.memory.sync.ISyncLog;
import org.xydra.core.model.impl.memory.sync.ISyncLogEntry;
import org.xydra.core.model.impl.memory.sync.Root;
import org.xydra.core.model.impl.memory.sync.UnorderedEventMapper;
import org.xydra.index.query.Pair;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.persistence.GetEventsRequest;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.XydraStore;


/**
 * @author xamde
 */
@RunsInGWT(true)
public class NewSyncer {

    private class ServerCallback implements
            Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> {

        @Override
        public void onFailure(final Throwable exception) {
            onServerFailure(exception);
        }

        @Override
        public void onSuccess(final Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]> pair) {
            onServerSuccess(pair);
        }

    }

    private static final Logger log = LoggerFactory.getLogger(NewSyncer.class);

    /**
     * @param syncableState
     * @param atomicEvent
     */
    public static void applyEntityRevisionOfSingleEvent(final ISyncableState syncableState,
            final XAtomicEvent atomicEvent) {
        if(syncableState instanceof XRevWritableModel) {
            applyEntityRevisionOfSingleEventToModel((XRevWritableModel)syncableState, atomicEvent);
        } else if(syncableState instanceof XRevWritableObject) {
            applyEntityRevisionOfSingleEventToObject((XRevWritableObject)syncableState, atomicEvent);
        } else {
            throw new RuntimeException("Cannot sync instanceof of "
                    + syncableState.getClass().getName());
        }
    }

    public static void applyEntityRevisionOfSingleEventToModel(final XRevWritableModel model,
            final XAtomicEvent atomicEvent) {
        final long newRev = atomicEvent.getRevisionNumber();

        // repo has no revision number

        // model rev is always updated
        model.setRevisionNumber(newRev);

        // object rev
        final XId objectId = atomicEvent.getChangedEntity().getObject();
        if(objectId == null) {
            return;
        }
        final XRevWritableObject object = model.getObject(objectId);
        if(object == null) {
            return;
        }
        applyEntityRevisionOfSingleEventToObject(object, atomicEvent);
    }

    /**
     * Set object and field rev from event
     *
     * @param object @NeverNull
     * @param atomicEvent
     */
    public static void applyEntityRevisionOfSingleEventToObject(final XRevWritableObject object,
            final XAtomicEvent atomicEvent) {
        final long newRev = atomicEvent.getRevisionNumber();

        assert object != null;
        object.setRevisionNumber(newRev);

        // field rev
        final XId fieldId = atomicEvent.getChangedEntity().getField();
        if(fieldId == null) {
            return;
        }
        final XRevWritableField field = object.getField(fieldId);
        if(field == null) {
            return;
        }
        field.setRevisionNumber(newRev);
    }

    /**
     * Inserts the correct revision Numbers to all entities. The changes were
     * already inserted via the EventDelta
     *
     * @param serverEvents expected to be sorted: changes with the highest
     *            revision numbers are latest (last)
     * @param syncableState
     */
    public static void applyEntityRevisionsToModel(final XEvent[] serverEvents,
            final ISyncableState syncableState) {

        for(final XEvent anyEntityEvent : serverEvents) {
            if(anyEntityEvent instanceof XTransactionEvent) {
                final XTransactionEvent transactionEvent = (XTransactionEvent)anyEntityEvent;
                for(final XAtomicEvent xAtomicEvent : transactionEvent) {
                    applyEntityRevisionOfSingleEvent(syncableState, xAtomicEvent);

                }
            } else {
                applyEntityRevisionOfSingleEvent(syncableState, (XAtomicEvent)anyEntityEvent);
            }
        }

    }

    private final XId actorId;

    private final ISyncableState syncableState;

    private final XAddress entityAddress;

    private final String passwordHash;

    private final XydraStore remoteStore;

    private final Root root;

    private XSynchronizationCallback synchronizationCallback;

    private final ISyncLog syncLog;

    private long syncRev;

    /**
     * @param store to send commands and get events
     * @param modelWithListeners
     * @param modelState
     * @param root
     * @param actorId
     * @param passwordHash
     * @param syncRev
     */
    public NewSyncer(final XydraStore store, final IMemoryMOFEntity modelWithListeners,

    final XRevWritableModel modelState, final Root root,

    final XId actorId, final String passwordHash, final long syncRev) {
        this.remoteStore = store;
        this.entityAddress = modelWithListeners.getAddress();
        this.syncableState = modelState;
        this.root = root;
        this.syncLog = root.getSyncLog();
        this.actorId = actorId;
        this.passwordHash = passwordHash;
        this.syncRev = syncRev;
    }

    public NewSyncer(final XydraStore store, final XSynchronizesChanges syncableEntity,
            final ISyncableState syncableState) {
        this.remoteStore = store;
        this.syncableState = syncableState;
        this.entityAddress = syncableEntity.getAddress();
        this.root = syncableEntity.getRoot();
        this.syncLog = this.root.getSyncLog();
        this.actorId = syncableEntity.getSessionActor();
        this.passwordHash = syncableEntity.getSessionPasswordHash();
        this.syncRev = syncableEntity.getSynchronizedRevision();
    }

    /**
     * Continue the sync process. Means:
     * <ol>
     * <li>Add all server events to the Event Delta
     * <li>Add all Local Change-events (inverted) to the Event Delta -> so the
     * Event Delta represents the integration of server state and local state
     *
     * <li>Event mapping: Find out which local commands to consider as success
     * and which as failed. Depends on chosen sync algorithm.
     *
     * <li>Fire sync events</li>
     * <li>Apply the Event Delta to the Model State</li>
     * <li>update the Change Log, clear all local changes</li>
     * <li>Send change events</li>
     * </ol>
     *
     * @param serverEvents
     */
    public void continueSync(final XEvent[] serverEvents) {
        if(log.isDebugEnabled()) {
			log.debug("***** Computing eventDelta from " + serverEvents.length
                    + " server events and n local changes");
		}

        try {

            /* calculated event delta */
            final EventDelta eventDelta = new EventDelta();
            for(final XEvent serverEvent : serverEvents) {
                if(log.isTraceEnabled()) {
					log.trace(">>> Server event: " + serverEvent);
				}
                eventDelta.addEvent(serverEvent);
            }
            final Iterator<ISyncLogEntry> localChanges = this.syncLog.getLocalChanges();
            while(localChanges.hasNext()) {
                final ISyncLogEntry localSyncLogEntry = localChanges.next();
                final XEvent localEvent = localSyncLogEntry.getEvent();
                assert this.syncLog.getEventAt(localEvent.getRevisionNumber()) == localEvent;
                if(log.isDebugEnabled()) {
					log.debug("<<< Local event: " + localEvent);
				}
                eventDelta.addInverseEvent(localEvent, this.syncLog);
            }

            // state of eventDelta matters now a lot

            /* event mapping */
            // choose your event mapper here
            final IEventMapper eventMapper = new UnorderedEventMapper();
            final IMappingResult mapping = eventMapper.mapEvents(this.syncLog, serverEvents);

            // send sync events, let app see state before sync
            for(final Pair<XEvent,XEvent> p : mapping.getMapped()) {
                final XEvent event = p.getSecond();
                // send sync-success events
                fireSyncEvent(event, true);
            }
            for(final XEvent event : mapping.getUnmappedLocalEvents()) {
                fireSyncEvent(event, false);
            }

            // start atomic section -----

            // change model state
            eventDelta.applyTo(this.syncableState);

            log.info("State now = " + this.syncableState);

            // change model state revison numbers
            NewSyncer.applyEntityRevisionsToModel(serverEvents, this.syncableState);

            // change sync log
            long newSyncRev = -1;
            if(serverEvents.length > 0) {
                newSyncRev = serverEvents[serverEvents.length - 1].getRevisionNumber();
                // change changeLog
                this.syncLog.truncateToRevision(this.syncRev);
                log.info("Current SyncLog=" + this.syncLog);
                if(log.isDebugEnabled()) {
					log.debug("Appending events to syncLog");
				}
                for(final XEvent e : serverEvents) {
                    if(log.isDebugEnabled()) {
						log.debug("Current rev=" + this.syncLog.getCurrentRevisionNumber());
					}
                    if(log.isDebugEnabled()) {
						log.debug("### Appending event from server: " + e);
					}
                    this.syncLog.appendEvent(e);
                }
            } else {
                if(log.isDebugEnabled()) {
					log.debug("No server appends received, synclog remains unchanged");
				}
            }

            if(log.isDebugEnabled()) {
				log.debug("Clearing local changes");
			}
            this.syncLog.clearLocalChanges();

            if(serverEvents.length > 0) {
                if(log.isDebugEnabled()) {
					log.debug("Setting new syncRev to " + newSyncRev);
				}
                this.syncLog.setSynchronizedRevision(newSyncRev);
            }
            this.syncRev = newSyncRev;

            // end atomic section ----

            // send change events
            if(log.isDebugEnabled()) {
				log.debug("Sending " + eventDelta.getEventCount() + " events");
			}
            eventDelta.sendChangeEvents(this.root, this.entityAddress,
                    this.entityAddress.getParent());

        } catch(final Exception e) {
            throw new RuntimeException("error while syncing", e);
        } finally {
            this.root.unlock();
        }

        log.info("Done syncing");
        if(this.synchronizationCallback != null) {
            this.synchronizationCallback.onSuccess();
        }
    }

    /**
     * @param lc @NeverNull
     * @param result true iff sync-success, false if sync-error
     */
    private void fireSyncEvent(final XEvent event, final boolean result) {
        final XAddress target = event.getTarget();
        final XSyncEvent syncEvent = new XSyncEvent(target, result);

        switch(event.getTarget().getAddressedType()) {
        case XMODEL:
            this.root.fireSyncEvent(this.entityAddress, syncEvent);
            break;
        case XOBJECT:
            this.root.fireSyncEvent(target, syncEvent);
            break;
        case XFIELD:
            this.root.fireSyncEvent(target, syncEvent);
            break;
        case XREPOSITORY:
            this.root.fireSyncEvent(this.entityAddress.getParent(), syncEvent);
            break;
        default:
            throw new RuntimeException("cannot happen");
        }
    }

    public void onServerFailure(final Throwable t) {
        log.warn("Sync exception", t);
        if(this.synchronizationCallback != null) {
            this.synchronizationCallback.onRequestError(t);
        }

        // TODO stop syncing, release lock
        this.root.unlock();
    }

    public void onServerSuccess(final Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]> pair) {
        final BatchedResult<XEvent[]> singleResult = pair.getSecond()[0];

        final Throwable t = singleResult.getException();

        if(t == null) {
            continueSync(singleResult.getResult());
        } else {
            // TODO deal with problems on server side
            log.warn("Got an exception from server", t);
        }
    }

    /**
     * @param synchronizationCallback @CanBeNull
     */
    public void startSync(final XSynchronizationCallback synchronizationCallback) {
        if(log.isDebugEnabled()) {
			log.debug("Sync start on " + this.entityAddress + " syncRev=" + this.syncRev
                    + " entityRev=" + this.syncableState.getRevisionNumber() + "...");
		}

        // TODO make sure it runs only once at the same time
        if(this.root.isLocked()) {
            log.warn("Syncer seems to run twice");
        }
        this.root.lock();
        this.synchronizationCallback = synchronizationCallback;

        // create commands to be sent to server from syncLog/localChanges
        final ArrayList<XCommand> localCommandList = new ArrayList<XCommand>();

        final Iterator<ISyncLogEntry> localChanges = this.syncLog.getLocalChanges();
        while(localChanges.hasNext()) {
            final ISyncLogEntry sle = localChanges.next();
            final XCommand cmd = sle.getCommand();
            if(log.isDebugEnabled()) {
				log.debug("Scheduling local command " + cmd + " for syncing");
			}
            localCommandList.add(cmd);
        }
        final GetEventsRequest getEventRequest = new GetEventsRequest(this.syncableState.getAddress(),
                this.syncRev + 1, Long.MAX_VALUE);

        // prepare batch request
        final XCommand[] localCommandsArray = localCommandList.toArray(new XCommand[localCommandList
                .size()]);
        final GetEventsRequest[] getEventRequestArray = new GetEventsRequest[] { getEventRequest };
        if(log.isDebugEnabled()) {
			log.debug("Sync executeCommands(#" + localCommandList.size() + ")AndGetEvents(#?)");
		}

        // contact remote store
        this.remoteStore.executeCommandsAndGetEvents(this.actorId, this.passwordHash,
                localCommandsArray, getEventRequestArray, new ServerCallback());
        // IMPROVE let app continue to run while we wait?
    }
}
