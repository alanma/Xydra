package org.xydra.core.model.sync;

import java.util.ArrayList;
import java.util.List;

import org.xydra.base.XId;
import org.xydra.base.change.ChangeType;
import org.xydra.base.change.XAtomicCommand;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XFieldCommand;
import org.xydra.base.change.XModelCommand;
import org.xydra.base.change.XObjectCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.change.impl.memory.MemoryFieldCommand;
import org.xydra.base.change.impl.memory.MemoryModelCommand;
import org.xydra.base.change.impl.memory.MemoryObjectCommand;
import org.xydra.base.change.impl.memory.MemoryRepositoryCommand;
import org.xydra.base.change.impl.memory.MemoryTransaction;
import org.xydra.core.model.XLocalChange;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XSynchronizesChanges;
import org.xydra.index.XI;
import org.xydra.index.query.Pair;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.sharedutils.XyAssert;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.RequestException;
import org.xydra.store.XydraStore;


/**
 * A class that can synchronize local and remote changes made to an
 * {@link XModel}.
 * 
 * @author dscharrer
 * 
 */
public class XSynchronizer {
    
    static private final Logger log = LoggerFactory.getLogger(XSynchronizer.class);
    
    private final XSynchronizesChanges entity;
    private boolean requestRunning = false;
    
    private final XydraStore store;
    
    /**
     * Wrap the given entity to be synchronized with the given store. Each
     * entity should only be wrapped once or commands may be sent multiple
     * times.
     * 
     * @param entity
     * @param store
     */
    public XSynchronizer(XSynchronizesChanges entity, XydraStore store) {
        log.info("sync: init with entity " + entity.getAddress() + " | " + entity.getAddress());
        if(entity.getAddress().getRepository() == null || entity.getAddress().getModel() == null) {
            throw new IllegalArgumentException(
                    "cannot synchronized entities without a repository and model ID, was: "
                            + entity.getAddress());
        }
        this.entity = entity;
        this.store = store;
    }
    
    private void applyEvents(XEvent[] remoteChanges) {
        
        if(remoteChanges.length == 0) {
            // no changes to merge
            return;
        }
        
        boolean success = this.entity.synchronize(remoteChanges);
        
        if(!success) {
            log.error("sync: error applying remote events");
        }
        XyAssert.xyAssert(success);
        
    }
    
    private abstract class SyncCallback<T> implements Callback<T> {
        
        protected XSynchronizationCallback sc;
        
        protected SyncCallback(XSynchronizationCallback sc) {
            this.sc = sc;
        }
        
        @Override
        public void onFailure(Throwable exception) {
            log.error("sync: request error sending command", exception);
            if(this.sc != null) {
                this.sc.onRequestError(exception);
            }
            requestEnded(false);
        }
        
        protected void requestEnded(boolean noConnectionErrors) {
            
            XyAssert.xyAssert(XSynchronizer.this.requestRunning);
            
            if(!noConnectionErrors) {
                // Abort if there are connection errors.
                XSynchronizer.this.requestRunning = false;
                return;
            }
            
            if(!checkDone()) {
                // Remaining events will be handled when returning to the
                // synchronize method.
                return;
            }
            
            doSynchronize(this.sc, false);
        }
        
        private boolean done = false;
        
        /**
         * Set the done flag to true.
         * 
         * @return the old done flag.
         */
        synchronized public boolean checkDone() {
            boolean oldDone = this.done;
            this.done = true;
            return oldDone;
        }
        
    }
    
    private class CommandsCallback extends
            SyncCallback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> {
        
        protected long syncRev;
        private List<XLocalChange> changes;
        
        protected CommandsCallback(XSynchronizationCallback sc, long syncRev,
                List<XLocalChange> changes) {
            super(sc);
            this.changes = changes;
        }
        
        @Override
        public void onSuccess(Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]> res) {
            
            XyAssert.xyAssert(res.getFirst().length == this.changes.size());
            XyAssert.xyAssert(res.getSecond().length == 1);
            
            BatchedResult<Long>[] commandRess = res.getFirst();
            BatchedResult<XEvent[]> eventsRes = res.getSecond()[0];
            
            XEvent[] events = eventsRes.getResult();
            boolean gotEvents = (events != null && events.length != 0);
            
            boolean success = true;
            for(int i = 0; i < commandRess.length; i++) {
                
                BatchedResult<Long> commandRes = commandRess[i];
                
                if(commandRes.getException() != null) {
                    XyAssert.xyAssert(commandRes.getResult() == null);
                    log.error("sync: error sending command", commandRes.getException());
                    success = false;
                    if(this.sc != null) {
                        this.sc.onCommandErrror(commandRes.getException());
                    }
                    
                } else {
                    
                    XyAssert.xyAssert(commandRes.getResult() != null);
                    assert commandRes.getResult() != null;
                    long commandRev = commandRes.getResult();
                    
                    // command successfully synchronized
                    if(commandRev >= 0) {
                        
                        if(commandRev <= this.syncRev) {
                            log.error("sync: store returned a command revision " + commandRev
                                    + " that isn't greater than our already synced revison "
                                    + this.syncRev + " - store error?");
                            // lost sync -> bad!!!
                        }
                        
                        if(!gotEvents) {
                            log.warn("sync: command applied remotely with revision " + commandRev
                                    + ", but no new events - store error?");
                            // lost sync -> bad!!!
                        } else {
                            log.info("sync: command applied remotely with revision " + commandRev);
                        }
                        
                        this.changes.get(i).setRemoteResult(commandRev);
                        
                    } else if(commandRev == XCommand.NOCHANGE) {
                        if(!gotEvents) {
                            log.warn("sync: command didn't change anything remotely, "
                                    + "but no new events, sync lost?");
                            // lost sync -> bad!!!
                        } else {
                            log.info("sync: command didn't change anything remotely, "
                                    + "got new events");
                            // command should be marked as redundant when
                            // merging remote events
                        }
                        
                    } else {
                        XyAssert.xyAssert(commandRev == XCommand.FAILED);
                        if(!gotEvents) {
                            log.warn("sync: command failed but no new events, sync lost?");
                            // lost sync -> bad!!!
                        } else {
                            log.info("sync: command failed remotely, got new events");
                            // command should be marked as failed when
                            // merging
                            // remote events
                        }
                    }
                    
                }
                
            }
            
            if(eventsRes.getException() != null) {
                XyAssert.xyAssert(events == null);
                log.error("sync: error getting events while sending command",
                        eventsRes.getException());
                if(this.sc != null) {
                    this.sc.onEventsError(eventsRes.getException());
                }
                requestEnded(false);
                return;
            }
            
            XyAssert.xyAssert(events != null, "events not null");
            assert events != null;
            
            applyEvents(events);
            requestEnded(success);
        }
    }
    
    private class EventsCallback extends SyncCallback<BatchedResult<XEvent[]>[]> {
        
        protected EventsCallback(XSynchronizationCallback sc) {
            super(sc);
        }
        
        @Override
        public void onSuccess(BatchedResult<XEvent[]>[] res) {
            
            XyAssert.xyAssert(res.length == 1);
            BatchedResult<XEvent[]> eventsRes = res[0];
            
            if(eventsRes.getException() != null) {
                XyAssert.xyAssert(eventsRes.getResult() == null);
                log.error("sync: error getting events", eventsRes.getException());
                if(this.sc != null) {
                    this.sc.onEventsError(eventsRes.getException());
                }
                requestEnded(false);
                return;
            }
            
            XyAssert.xyAssert(eventsRes.getResult() != null);
            assert eventsRes.getResult() != null;
            
            applyEvents(eventsRes.getResult());
            requestEnded(true);
        }
        
    }
    
    /**
     * Query the store for new remote changes. Local changes will be sent
     * immediately.
     * 
     * @param sc A callback that is notified if synchronizing was successful or
     *            not. This may be null if no notification is desired.
     */
    public void synchronize(XSynchronizationCallback sc) {
        
        synchronized(this) {
            if(this.requestRunning) {
                // There are already requests running, start this request when
                // they are done.
                return;
            }
            this.requestRunning = true;
        }
        
        doSynchronize(sc, true);
        
    }
    
    private static XCommand fixCommand(long syncRev, XCommand command, int idx) {
        
        if(command == null) {
            throw new RequestException("command was null");
        }
        
        if(command instanceof XAtomicCommand) {
            return fixAtomicCommand(syncRev, idx, (XAtomicCommand)command);
        }
        
        XyAssert.xyAssert(command instanceof XTransaction);
        XTransaction trans = (XTransaction)command;
        
        boolean isRelative = false;
        for(XAtomicCommand ac : trans) {
            if(!ac.isForced() && ac.getRevisionNumber() > syncRev) {
                XyAssert.xyAssert(ac.getRevisionNumber() < XCommand.RELATIVE_REV);
                isRelative = true;
                break;
            }
        }
        
        if(!isRelative) {
            return trans;
        }
        
        XAtomicCommand[] fixedCommands = new XAtomicCommand[trans.size()];
        for(int i = 0; i < trans.size(); i++) {
            fixedCommands[i] = fixAtomicCommand(syncRev, idx, trans.getCommand(i));
            if(fixedCommands[i] == null) {
                return null;
            }
        }
        
        return MemoryTransaction.createTransaction(trans.getTarget(), fixedCommands);
    }
    
    private static XAtomicCommand fixAtomicCommand(long syncRev, int i, XAtomicCommand ac) {
        
        if(ac.isForced() || ac.getRevisionNumber() <= syncRev) {
            // not relative
            return ac;
        }
        
        XyAssert.xyAssert(ac.getRevisionNumber() < XCommand.RELATIVE_REV);
        
        assert ac instanceof XFieldCommand || ac.getChangeType() != ChangeType.ADD : " add entity commands don't have real / relative revisions";
        
        long rev = ac.getRevisionNumber() - syncRev + XCommand.RELATIVE_REV;
        
        XyAssert.xyAssert(rev < XCommand.RELATIVE_REV + i);
        
        if(ac instanceof XRepositoryCommand) {
            XyAssert.xyAssert(ac.getChangeType() == ChangeType.REMOVE);
            return MemoryRepositoryCommand.createRemoveCommand(ac.getTarget(), rev,
                    ((XRepositoryCommand)ac).getModelId());
        } else if(ac instanceof XModelCommand) {
            XyAssert.xyAssert(ac.getChangeType() == ChangeType.REMOVE);
            return MemoryModelCommand.createRemoveCommand(ac.getTarget(), rev,
                    ((XModelCommand)ac).getObjectId());
        } else if(ac instanceof XObjectCommand) {
            XyAssert.xyAssert(ac.getChangeType() == ChangeType.REMOVE);
            return MemoryObjectCommand.createRemoveCommand(ac.getTarget(), rev,
                    ((XObjectCommand)ac).getFieldId());
        } else if(ac instanceof XFieldCommand) {
            switch(ac.getChangeType()) {
            case ADD:
                return MemoryFieldCommand.createAddCommand(ac.getTarget(), rev,
                        ((XFieldCommand)ac).getValue());
            case CHANGE:
                return MemoryFieldCommand.createChangeCommand(ac.getTarget(), rev,
                        ((XFieldCommand)ac).getValue());
            case REMOVE:
                return MemoryFieldCommand.createRemoveCommand(ac.getTarget(), rev);
            default:
                throw new AssertionError("unexpected command: " + ac);
            }
        } else {
            throw new AssertionError("unexpected command: " + ac);
        }
    }
    
    private void doSynchronize(XSynchronizationCallback sc, boolean isFirst) {
        
        XyAssert.xyAssert(XSynchronizer.this.requestRunning);
        
        final List<XLocalChange> newChanges = new ArrayList<XLocalChange>();
        boolean first = isFirst;
        do {
            
            long syncRev = this.entity.getSynchronizedRevision();
            
            XLocalChange[] changes = this.entity.getLocalChanges();
            
            // Find the first local command that has not been sent to the server
            // yet.
            newChanges.clear();
            XId actorId = null;
            String psw = null;
            for(int i = 0; i < changes.length; i++) {
                if(!changes[i].isApplied()) {
                    if(!newChanges.isEmpty()
                            && (!XI.equals(actorId, changes[i].getActor()) || !XI.equals(psw,
                                    changes[i].getPasswordHash()))) {
                        break;
                    }
                    newChanges.add(changes[i]);
                    actorId = changes[i].getActor();
                    psw = changes[i].getPasswordHash();
                }
            }
            
            SyncCallback<?> callback = null;
            if(!newChanges.isEmpty()) {
                
                // There are commands to send.
                
                log.info("sync: sending commands " + newChanges + ", rev is " + syncRev);
                
                CommandsCallback cc = new CommandsCallback(sc, syncRev, newChanges);
                callback = cc;
                
                XCommand[] commands = new XCommand[newChanges.size()];
                int i = 0;
                for(XLocalChange lc : newChanges) {
                    if(i == 0) {
                        commands[i] = lc.getCommand();
                    } else {
                        commands[i] = fixCommand(syncRev, lc.getCommand(), i);
                    }
                    i++;
                }
                
                this.store.executeCommandsAndGetEvents(actorId, psw, commands,
                        new GetEventsRequest[] { new GetEventsRequest(this.entity.getAddress(),
                                syncRev + 1, Long.MAX_VALUE) }, cc);
                
            } else if(first) {
                
                // There are no commands to send, so just get new events.
                
                log.info("sync: getting events, rev is " + syncRev);
                
                EventsCallback ec = new EventsCallback(sc);
                callback = ec;
                
                // FIXME where to get the passwordHash?
                this.store.getEvents(this.entity.getSessionActor(), this.entity
                        .getSessionPassword(), new GetEventsRequest[] { new GetEventsRequest(
                        this.entity.getAddress(), syncRev + 1, Long.MAX_VALUE) }, ec);
                
            }
            
            if(callback != null && !callback.checkDone()) {
                // Remaining commands will be synchronized when the callback is
                // invoked.
                return;
            }
            
            first = false;
            
        } while(!newChanges.isEmpty());
        
        if(sc != null) {
            sc.onSuccess();
        }
        XyAssert.xyAssert(this.requestRunning);
        this.requestRunning = false;
        
    }
}
