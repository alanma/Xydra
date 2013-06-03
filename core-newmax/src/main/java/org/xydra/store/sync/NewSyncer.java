package org.xydra.store.sync;

import java.util.ArrayList;
import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.change.XAtomicEvent;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XSyncEvent;
import org.xydra.base.change.XTransactionEvent;
import org.xydra.base.rmof.XRevWritableField;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.base.rmof.XRevWritableObject;
import org.xydra.core.model.impl.memory.EventDelta;
import org.xydra.core.model.impl.memory.IMemoryModel;
import org.xydra.core.model.impl.memory.sync.IEventMapper;
import org.xydra.core.model.impl.memory.sync.IEventMapper.IMappingResult;
import org.xydra.core.model.impl.memory.sync.ISyncLog;
import org.xydra.core.model.impl.memory.sync.ISyncLogEntry;
import org.xydra.core.model.impl.memory.sync.Root;
import org.xydra.core.model.impl.memory.sync.UnorderedEventMapper;
import org.xydra.index.query.Pair;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.persistence.GetEventsRequest;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.XydraStore;


/**
 * TODO andi: fix if possible; probably not possible
 * 
 * @author xamde
 * 
 */
public class NewSyncer {
    
    private static final Logger log = LoggerFactory.getLogger(NewSyncer.class);
    
    private String passwordHash;
    
    private XId actorId;
    
    private XRevWritableModel modelState;
    
    private ISyncLog syncLog;
    
    /**
     * @param store to send commands and get events
     * @param modelWithListeners
     * @param modelState
     * @param root
     * @param actorId
     * @param passwordHash
     * @param syncRev
     */
    public NewSyncer(XydraStore store, IMemoryModel modelWithListeners,
    
    XRevWritableModel modelState, Root root,
    
    XId actorId, String passwordHash, long syncRev) {
        this.remoteStore = store;
        this.modelWithListeners = modelWithListeners;
        this.modelState = modelState;
        this.root = root;
        this.syncLog = root.getSyncLog();
        this.actorId = actorId;
        this.passwordHash = passwordHash;
        this.syncRev = syncRev;
    }
    
    private XydraStore remoteStore;
    
    private long syncRev;
    
    // TODO make sure it runs only once at the same time
    public void startSync() {
        log.debug("Sync start on syncRev=" + this.syncRev + " modelRev="
                + this.modelState.getRevisionNumber() + "...");
        
        this.root.lock();
        
        // create commands to be sent to server from syncLog/localChanges
        ArrayList<XCommand> localCommandList = new ArrayList<XCommand>();
        
        Iterator<ISyncLogEntry> localChanges = this.syncLog.getLocalChanges();
        while(localChanges.hasNext()) {
            ISyncLogEntry sle = localChanges.next();
            XCommand cmd = sle.getCommand();
            log.debug("Scheduling local command " + cmd + " for syncing");
            localCommandList.add(cmd);
        }
        GetEventsRequest getEventRequest = new GetEventsRequest(this.modelState.getAddress(),
                this.syncRev + 1, Long.MAX_VALUE);
        
        // prepare batch request
        XCommand[] localCommandsArray = localCommandList.toArray(new XCommand[localCommandList
                .size()]);
        GetEventsRequest[] getEventRequestArray = new GetEventsRequest[] { getEventRequest };
        log.debug("Sync executeCommands(#" + localCommandList.size() + ")AndGetEvents(#?)");
        
        // contact remote store
        this.remoteStore.executeCommandsAndGetEvents(this.actorId, this.passwordHash,
                localCommandsArray, getEventRequestArray, new ServerCallback());
        // TODO let app continue to run while we wait?
    }
    
    private class ServerCallback implements
            Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> {
        
        @Override
        public void onFailure(Throwable exception) {
            NewSyncer.this.onServerFailure(exception);
        }
        
        @Override
        public void onSuccess(Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]> pair) {
            NewSyncer.this.onServerSuccess(pair);
        }
        
    }
    
    private Root root;
    
    private IMemoryModel modelWithListeners;
    
    public void onServerFailure(Throwable exception) {
        // TODO stop syncing
    }
    
    public void onServerSuccess(Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]> pair) {
        BatchedResult<XEvent[]> singleResult = pair.getSecond()[0];
        
        // TODO deal with problems on server side
        Throwable t = singleResult.getException();
        
        if(t == null) {
            continueSync(singleResult.getResult());
        }
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
    private void continueSync(XEvent[] serverEvents) {
        log.debug("***** Computing eventDelta from " + serverEvents.length + " and local changes");
        
        /* calculated event delta */
        EventDelta eventDelta = new EventDelta();
        for(XEvent serverEvent : serverEvents) {
            
            // FIXME
            log.debug(">>> Server event: " + serverEvent);
            
            eventDelta.addEvent(serverEvent);
        }
        Iterator<ISyncLogEntry> localChanges = this.syncLog.getLocalChanges();
        while(localChanges.hasNext()) {
            ISyncLogEntry localSyncLogEntry = localChanges.next();
            XEvent localEvent = localSyncLogEntry.getEvent();
            log.debug("<<< Local event: " + localEvent);
            eventDelta.addInverseEvent(localEvent,
            // FIXME !!!!!!
                    1000,
                    // this.syncRev,
                    this.syncLog);
        }
        
        /* event mapping */
        // choose your event mapper here
        IEventMapper eventMapper = new UnorderedEventMapper();
        IMappingResult mapping = eventMapper.mapEvents(this.syncLog, serverEvents);
        
        // send sync events, let app see state before sync
        for(Pair<XEvent,ISyncLogEntry> p : mapping.getMapped()) {
            ISyncLogEntry sle = p.getSecond();
            // send sync-success events
            fireSyncEvent(sle, true);
        }
        for(ISyncLogEntry sle : mapping.getUnmappedLocalEvents()) {
            // TODO send sync-failed events
            fireSyncEvent(sle, false);
        }
        
        // start atomic section -----
        
        // change state
        
        // FIXME
        log.info("EventDelta NOW=" + eventDelta);
        
        eventDelta.applyTo(this.modelState);
        NewSyncer.applyEntityRevisionsToModel(serverEvents, this.modelState);
        
        long newSyncRev = -1;
        if(serverEvents.length > 0) {
            newSyncRev = serverEvents[serverEvents.length - 1].getRevisionNumber();
            // change changeLog
            log.debug("Truncating local syncLog down to syncRev=" + this.syncRev + "; highest was "
                    + this.syncLog.getLastEvent().getRevisionNumber());
            this.syncLog.truncateToRevision(this.syncRev);
            for(XEvent e : serverEvents) {
                log.debug("Current rev=" + this.syncLog.getCurrentRevisionNumber());
                log.debug("Appending event from server: " + e);
                this.syncLog.appendEvent(e);
            }
        }
        
        log.debug("Clearing local changes");
        this.syncLog.clearLocalChanges();
        
        if(serverEvents.length > 0) {
            log.debug("Setting new syncRev to " + newSyncRev);
            this.syncLog.setSynchronizedRevision(newSyncRev);
        }
        this.syncRev = newSyncRev;
        
        // end atomic section ----
        
        // send change events
        log.debug("Sending events");
        eventDelta.sendChangeEvents(this.root, this.modelWithListeners.getAddress(),
                this.modelWithListeners.getAddress().getParent());
        log.info("Done syncing");
    }
    
    /**
     * Inserts the correct revision Numbers to all entities. The changes were
     * already inserted via the EventDelta
     * 
     * @param serverEvents expected to be sorted: changes with the highest
     *            revision numbers are latest
     * @param model
     */
    public static void applyEntityRevisionsToModel(XEvent[] serverEvents, XRevWritableModel model) {
        
        for(XEvent anyEntityEvent : serverEvents) {
            if(anyEntityEvent instanceof XTransactionEvent) {
                XTransactionEvent transactionEvent = (XTransactionEvent)anyEntityEvent;
                for(XAtomicEvent xAtomicEvent : transactionEvent) {
                    applyEntityRevisionOfSingleEventToModel(model, xAtomicEvent);
                    
                }
            } else {
                applyEntityRevisionOfSingleEventToModel(model, anyEntityEvent);
            }
        }
        
    }
    
    public static void applyEntityRevisionOfSingleEventToModel(XRevWritableModel model,
            XEvent anyEntityEvent) {
        XAddress targetAddress = anyEntityEvent.getTarget();
        try {
            
            long newRev = anyEntityEvent.getRevisionNumber();
            
            XType targetedType = targetAddress.getAddressedType();
            // happens always
            model.setRevisionNumber(newRev);
            XRevWritableObject object;
            switch(targetedType) {
            case XREPOSITORY:
                // nothing
                break;
            case XMODEL:
                // an object was added / removed
                break;
            case XOBJECT:
                // a field was added / removed
                object = model.getObject(anyEntityEvent.getTarget().getObject());
                if(object == null)
                    break;
                object.setRevisionNumber(newRev);
                
                break;
            case XFIELD:
                // a value was changed
                object = model.getObject(anyEntityEvent.getTarget().getObject());
                if(object == null)
                    break;
                object.setRevisionNumber(newRev);
                XRevWritableField field = object.getField(anyEntityEvent.getTarget().getField());
                if(field == null)
                    break;
                field.setRevisionNumber(newRev);
                break;
            default:
                break;
            }
            
        } catch(Exception e) {
            e.printStackTrace();
            // TODO Max fragen, ob das so orthodox
            throw new RuntimeException("could not apply the revision number of entity "
                    + targetAddress.toString());
        }
    }
    
    /**
     * @param lc @NeverNull
     * @param result true iff sync-success, false if sync-error
     */
    private void fireSyncEvent(ISyncLogEntry sle, boolean result) {
        XEvent e = sle.getEvent();
        XAddress target = e.getTarget();
        XSyncEvent syncEvent = new XSyncEvent(target, result);
        
        switch(e.getTarget().getAddressedType()) {
        case XMODEL:
            this.root.fireSyncEvent(this.modelWithListeners.getAddress(), syncEvent);
            break;
        case XOBJECT:
            this.root.fireSyncEvent(target, syncEvent);
            break;
        case XFIELD:
            this.root.fireSyncEvent(target, syncEvent);
            break;
        case XREPOSITORY:
            this.root.fireSyncEvent(this.modelWithListeners.getAddress().getParent(), syncEvent);
            break;
        default:
            throw new RuntimeException("cannot happen");
        }
    }
}
