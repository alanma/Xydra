package org.xydra.core.model.sync;

import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.core.model.XLocalChange;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XSynchronizesChanges;
import org.xydra.index.query.Pair;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.GetEventsRequest;
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
	boolean requestRunning = false;
	
	private final XydraStore store;
	
	/**
	 * Wrap the given entity to be synchronized with the given store. Each
	 * entity should only be wrapped once or commands may be sent multiple
	 * times.
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
		assert success;
		
	}
	
	private void requestEnded(boolean noConnectionErrors, XSynchronizationCallback sc) {
		
		this.requestRunning = false;
		
		if(noConnectionErrors) {
			if(this.entity.countUnappliedLocalChanges() > 0) {
				// Send the remaining local changes.
				// FIXME this can create deep call stacks
				synchronize(sc);
			} else if(sc != null) {
				sc.onSuccess();
			}
		}
	}
	
	/**
	 * Query the store for new remote changes. Local changes will be sent
	 * immediately.
	 * 
	 * @param sc A callback that is notified if synchronizing was successful or
	 *            not. This may be null if no notification is desired.
	 */
	public void synchronize(final XSynchronizationCallback sc) {
		
		if(this.requestRunning) {
			// There are already requests running, start this request when they
			// are done.
			return;
		}
		this.requestRunning = true;
		
		final long syncRev = this.entity.getSynchronizedRevision();
		
		XLocalChange[] changes = this.entity.getLocalChanges();
		
		// Find the first local command that has not been sent to the server
		// yet.
		XLocalChange newChange = null;
		for(int i = 0; i < changes.length; i++) {
			if(!changes[i].isApplied()) {
				newChange = changes[i];
				break;
			}
		}
		
		if(newChange != null) {
			
			// There are commands to send.
			
			/*
			 * IMPROVE synchronize more than one change at a time
			 * 
			 * To do this, the server needs to be able to differentiate between
			 * revisions in the command that refer to local changes and those
			 * that refer to remote changes.
			 */

			final XLocalChange change = newChange;
			
			log.info("sync: sending command " + change.getCommand() + ", rev is " + syncRev);
			
			Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>> callback = new Callback<Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]>>() {
				
				@Override
				public void onFailure(Throwable exception) {
					log.error("sync: request error sending command", exception);
					if(sc != null) {
						sc.onRequestError(exception);
					}
					requestEnded(false, sc);
				}
				
				@Override
				public void onSuccess(Pair<BatchedResult<Long>[],BatchedResult<XEvent[]>[]> res) {
					
					assert res.getFirst().length == 1;
					assert res.getSecond().length == 1;
					
					BatchedResult<Long> commandRes = res.getFirst()[0];
					BatchedResult<XEvent[]> eventsRes = res.getSecond()[0];
					
					XEvent[] events = eventsRes.getResult();
					boolean gotEvents = (events != null && events.length != 0);
					
					boolean success = true;
					if(commandRes.getException() != null) {
						assert commandRes.getResult() == null;
						log.error("sync: error sending command", commandRes.getException());
						success = false;
						if(sc != null) {
							sc.onCommandErrror(commandRes.getException());
						}
						
					} else {
						
						assert commandRes.getResult() != null;
						long commandRev = commandRes.getResult();
						
						// command successfully synchronized
						if(commandRev >= 0) {
							
							if(commandRev <= syncRev) {
								log.error("sync: store returned a command revision " + commandRev
								        + " that isn't greater than our already synced revison "
								        + syncRev + " - store error?");
								// lost sync -> bad!!!
							}
							
							if(!gotEvents) {
								log.warn("sync: command applied remotely with revision "
								        + commandRev + ", but no new events - store error?");
								// lost sync -> bad!!!
							} else {
								log.info("sync: command applied remotely with revision "
								        + commandRev);
							}
							
							change.setRemoteResult(commandRev);
							
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
							assert commandRev == XCommand.FAILED;
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
					
					if(eventsRes.getException() != null) {
						assert events == null;
						log.error("sync: error getting events while sending command",
						        eventsRes.getException());
						if(sc != null) {
							sc.onEventsError(eventsRes.getException());
						}
						requestEnded(false, sc);
						return;
					}
					
					assert events != null;
					
					applyEvents(events);
					requestEnded(success, sc);
				}
				
			};
			
			this.store.executeCommandsAndGetEvents(change.getActor(), change.getPasswordHash(),
			        new XCommand[] { change.getCommand() },
			        new GetEventsRequest[] { new GetEventsRequest(this.entity.getAddress(),
			                syncRev + 1, Long.MAX_VALUE) }, callback);
			
		} else {
			
			// There are no commands to send, so just get new events.
			
			log.info("sync: getting events, rev is " + syncRev);
			
			Callback<BatchedResult<XEvent[]>[]> callback = new Callback<BatchedResult<XEvent[]>[]>() {
				
				@Override
				public void onFailure(Throwable exception) {
					log.error("sync: request error getting events", exception);
					if(sc != null) {
						sc.onRequestError(exception);
					}
					requestEnded(false, sc);
				}
				
				@Override
				public void onSuccess(BatchedResult<XEvent[]>[] res) {
					
					assert res.length == 1;
					BatchedResult<XEvent[]> eventsRes = res[0];
					
					if(eventsRes.getException() != null) {
						assert eventsRes.getResult() == null;
						log.error("sync: error getting events", eventsRes.getException());
						if(sc != null) {
							sc.onEventsError(eventsRes.getException());
						}
						requestEnded(false, sc);
						return;
					}
					
					assert eventsRes.getResult() != null;
					
					applyEvents(eventsRes.getResult());
					requestEnded(true, sc);
				}
				
			};
			
			// FIXME where to get the passwordHash?
			this.store.getEvents(this.entity.getSessionActor(), "",
			        new GetEventsRequest[] { new GetEventsRequest(this.entity.getAddress(),
			                syncRev + 1, Long.MAX_VALUE) }, callback);
			
		}
		
	}
	
}
