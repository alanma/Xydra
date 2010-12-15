package org.xydra.core.model.sync;

import java.util.List;

import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XSynchronizesChanges;
import org.xydra.core.model.impl.memory.LocalChange;
import org.xydra.index.XI;
import org.xydra.index.query.Pair;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.XydraStore;


/**
 * A class that can synchronize local and remote changes made to an
 * {@link XModel}. If an {@link XModel} or {@link XObject} is wrapped in the
 * {@link XSynchronizer}, it should no longer be used directly, so that all
 * events can be synchronized.
 * 
 * TODO handle timeouts (retry), other store/HTTP errors
 * 
 * TODO move this into S
 * 
 * @author dscharrer
 * 
 */
public class XSynchronizer {
	
	static private final Logger log = LoggerFactory.getLogger(XSynchronizer.class);
	
	private long lastSyncedRevison;
	private final XSynchronizesChanges entity;
	private final XydraStore store;
	private final List<LocalChange> changes;
	private final XAddress addr;
	
	boolean requestRunning = false;
	
	/**
	 * Start synchronizing the given model (which has no local changes) via the
	 * given service. Any further changes applied directly to the model will be
	 * lost. To persist changes supply the to the
	 * {@link #executeCommand(XCommand, Callback)} Method.
	 * 
	 * @param addr The address of the entity on the store. This is needed for
	 *            synchronizing single XObjects as the local copy of the entity
	 *            will not have a parent model and thus no model XID in it's
	 *            address.
	 * 
	 * @param entity A fully synchronized entity.
	 */
	public XSynchronizer(XAddress addr, XSynchronizesChanges entity, XydraStore store) {
		log.info("sync: init with entity " + addr + " | " + entity.getAddress());
		this.addr = addr;
		this.entity = entity;
		this.store = store;
		this.changes = entity.getLocalChanges(); // FIXME this is a hack
		this.lastSyncedRevison = getLocalRevisionNumber();
		assert addr.getField() == null;
		assert addr.getModel() != null;
		assert XI.equals(addr.getObject(), entity.getAddress().getObject());
	}
	
	/**
	 * Start synchronizing the given model (which has no local changes) via the
	 * given service. Any further changes applied directly to the model will be
	 * lost. To persist changes supply the to the
	 * {@link #executeCommand(XCommand, Callback)} Method.
	 */
	public XSynchronizer(XModel entity, XydraStore store) {
		this(entity.getAddress(), entity, store);
	}
	
	public long getLocalRevisionNumber() {
		return this.entity.getChangeLog().getCurrentRevisionNumber();
	}
	
	private void startRequest() {
		
		if(this.requestRunning) {
			// There are already requests running, start this request when they
			// are done.
			return;
		}
		this.requestRunning = true;
		
		if(!this.changes.isEmpty()) {
			
			// There are commands to send.
			
			// IMPROVE synchronize more than one change at a time
			final LocalChange change = this.changes.get(0);
			
			log.info("sync: sending command " + change.command + ", rev is "
			        + this.lastSyncedRevison);
			
			Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback = new Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>>() {
				
				@Override
				public void onFailure(Throwable exception) {
					log.info("sync: request error sending command", exception);
					// TODO handle error;
					requestEnded(false);
				}
				
				@Override
				public void onSuccess(Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]> res) {
					
					assert res.getFirst().length == 1;
					assert res.getSecond().length == 1;
					
					BatchedResult<Long> commandRes = res.getFirst()[0];
					BatchedResult<XEvent[]> eventsRes = res.getSecond()[0];
					
					if(commandRes.getException() != null) {
						assert commandRes.getResult() == null;
						log.info("sync: error sending command", commandRes.getException());
						// TODO handle error;
						requestEnded(false);
						return;
					}
					
					assert commandRes.getResult() != null;
					long commandRev = commandRes.getResult();
					
					XEvent[] events = eventsRes.getResult();
					boolean gotEvents = (events != null && events.length != 0);
					
					if(commandRev != XCommand.FAILED) {
						// command successfully synchronized
						if(commandRev >= 0) {
							
							if(commandRev <= XSynchronizer.this.lastSyncedRevison) {
								log.error("sync: store returned a command revision " + commandRev
								        + " that isn't greater than our already synced revison "
								        + XSynchronizer.this.lastSyncedRevison + " - store error?");
								// lost sync -> bad!!!
							}
							
							if(!gotEvents) {
								log.warn("sync: command applied remotely with revision "
								        + commandRev + ", but no new events - store error?");
								// lost sync -> bad!!!
								// FIXME this can lead to infinite send-command
								// loops
							} else {
								log.info("sync: command applied remotely with revision "
								        + commandRev);
							}
						} else {
							if(!gotEvents) {
								log.warn("sync: command didn't change anything remotely, "
								        + "but no new events, sync lost?");
								// lost sync -> bad!!!
								// FIXME this can lead to infinite send-command
								// loops
							} else {
								log.info("sync: command didn't change anything remotely, "
								        + "got new events");
							}
							
						}
						if(change.callback != null) {
							change.callback.applied(commandRev);
						}
						XSynchronizer.this.changes.remove(0);
					} else {
						if(!gotEvents) {
							log.warn("sync: command failed but no new events, sync lost?");
							// lost sync -> bad!!!
							// FIXME this can lead to infinite send-command
							// loops
						} else {
							log.info("sync: command failed remotely, got new events");
							// local command should fail to re-apply in
							// applyEvents
						}
					}
					
					if(eventsRes.getException() != null) {
						assert events == null;
						log.info("sync: error getting events while sending command", eventsRes
						        .getException());
						// TODO handle error;
						requestEnded(false);
						return;
					}
					
					assert events != null;
					
					applyEvents(events);
					requestEnded(true);
				}
				
			};
			
			this.store.executeCommandsAndGetEvents(change.actor, change.passwordHash,
			        new XCommand[] { change.command },
			        new GetEventsRequest[] { new GetEventsRequest(this.addr,
			                this.lastSyncedRevison + 1, Long.MAX_VALUE) }, callback);
			
		} else {
			
			// There are no commands to send, so just get new events.
			
			log.info("sync: getting events, rev is " + this.lastSyncedRevison);
			
			Callback<BatchedResult<XEvent[]>[]> callback = new Callback<BatchedResult<XEvent[]>[]>() {
				
				@Override
				public void onFailure(Throwable exception) {
					log.info("sync: request error getting events", exception);
					// TODO handle error;
					requestEnded(false);
				}
				
				@Override
				public void onSuccess(BatchedResult<XEvent[]>[] res) {
					
					assert res.length == 1;
					BatchedResult<XEvent[]> eventsRes = res[0];
					
					if(eventsRes.getException() != null) {
						assert eventsRes.getResult() == null;
						log.info("sync: error getting events", eventsRes.getException());
						// TODO handle error;
						requestEnded(false);
						return;
					}
					
					assert eventsRes.getResult() != null;
					
					applyEvents(eventsRes.getResult());
					requestEnded(true);
				}
				
			};
			
			// FIXME where to get the passwordHash?
			this.store.getEvents(this.entity.getSessionActor(), "",
			        new GetEventsRequest[] { new GetEventsRequest(this.addr,
			                this.lastSyncedRevison + 1, Long.MAX_VALUE) }, callback);
			
		}
		
	}
	
	protected void applyEvents(XEvent[] remoteChanges) {
		
		if(remoteChanges.length == 0) {
			// no changes to merge
			return;
		}
		
		log.info("sync: merging " + remoteChanges.length + " remote and " + this.changes.size()
		        + " local changes, local rev is " + getLocalRevisionNumber() + " (synced to "
		        + this.lastSyncedRevison + ")");
		
		long[] results = this.entity.synchronize(remoteChanges, this.lastSyncedRevison);
		assert results.length == this.changes.size();
		
		this.lastSyncedRevison += remoteChanges.length;
		
		log.info("sync: merged changes, new local rev is " + getLocalRevisionNumber()
		        + " (synced to " + this.lastSyncedRevison + ")");
		
		for(int i = 0; i < results.length; i++) {
			LocalChange change = this.changes.get(i);
			if(results[i] == XCommand.FAILED) {
				log.info("sync: client command conflicted: " + change.command);
				if(change.callback != null) {
					change.callback.failed();
				}
			} else if(results[i] == XCommand.NOCHANGE) {
				log.info("sync: client command redundant: " + change.command);
				if(change.callback != null) {
					change.callback.applied(results[i]);
				}
			}
		}
		
		// IMPROVE this is O(nLocalChanges^2) worst case
		for(int i = results.length - 1; i >= 0; i--) {
			if(results[i] < 0) {
				this.changes.remove(i);
			}
		}
		
	}
	
	protected void requestEnded(boolean noConnectionErrors) {
		this.requestRunning = false;
		// TODO should this only be done when autoSync == true?
		if(noConnectionErrors && !this.changes.isEmpty()) {
			startRequest();
		}
	}
	
	/**
	 * Query the store for new remote changes. Local changes will be sent
	 * immediately.
	 */
	public void synchronize() {
		startRequest();
	}
	
}
