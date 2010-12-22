package org.xydra.client.sync;

import java.util.List;

import org.xydra.client.Callback;
import org.xydra.client.ServiceException;
import org.xydra.client.XChangesService;
import org.xydra.client.XChangesService.CommandResult;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XSynchronizesChanges;
import org.xydra.core.model.impl.memory.LocalChange;
import org.xydra.index.XI;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.XydraStore;


/**
 * A class that can synchronize local and remote changes made to an
 * {@link XModel}. If an {@link XModel} or {@link XObject} is wrapped in the
 * {@link XSynchronizer}, it should no longer be used directly, so that all
 * events can be synchronized.
 * 
 * TODO remove this once the store-based implementation in core works and there
 * is a {@link XydraStore} network implementation
 * 
 * @author dscharrer
 * 
 */
public class XSynchronizer {
	
	static private final Logger log = LoggerFactory.getLogger(XSynchronizer.class);
	
	private long syncRevison; // FIXME revision now tracked in entity.
	private final XSynchronizesChanges entity;
	private final XChangesService service;
	private final List<LocalChange> changes;
	private final XAddress addr;
	
	boolean requestRunning = false;
	
	/**
	 * Start synchronizing the given model (which has no local changes) via the
	 * given service. Any further changes applied directly to the model will be
	 * lost. To persist changes supply the to the
	 * {@link #executeCommand(XCommand, Callback)} Method.
	 * 
	 * @param addr The address of the entity on the server. This is needed for
	 *            synchronizing single XObjects as the local copy of the entity
	 *            will not have a parent model and thus no model XID in it's
	 *            address.
	 */
	public XSynchronizer(XAddress addr, XSynchronizesChanges entity, XChangesService service) {
		log.info("sync: init with entity " + addr + " | " + entity.getAddress());
		this.addr = addr;
		this.entity = entity;
		this.service = service;
		this.syncRevison = getLocalRevisionNumber();
		this.changes = entity.getLocalChanges(); // FIXME hack
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
	public XSynchronizer(XModel entity, XChangesService service) {
		this(entity.getAddress(), entity, service);
	}
	
	public long getLocalRevisionNumber() {
		return this.entity.getChangeLog().getCurrentRevisionNumber();
	}
	
	private void startRequest() {
		
		if(this.requestRunning) {
			return;
		}
		this.requestRunning = true;
		
		if(!this.changes.isEmpty()) {
			
			final LocalChange change = this.changes.get(0);
			
			log.info("sync: sending command " + change.command + ", rev is " + this.syncRevison);
			
			this.service.executeCommand(this.addr, change.command, this.syncRevison + 1,
			        new Callback<CommandResult>() {
				        public void onFailure(Throwable error) {
					        if(error instanceof ServiceException) {
						        log.info("sync: error sending command: " + error.getMessage());
					        } else {
						        log.info("sync: error sending command", error);
					        }
					        // TODO handle error;
					        requestEnded(false);
				        }
				        
				        public void onSuccess(CommandResult res) {
					        if(res.getResult() != XCommand.FAILED) {
						        if(res.getResult() >= 0) {
							        log.info("sync: command applied remotely");
						        } else {
							        if(res.getEvents().size() == 0) {
								        log.warn("sync: command didn't change anything remotely, "
								                + "but not new events, sync lost?");
								        // lost sync -> bad!!!
							        } else {
								        log.info("sync: command failed remotely, got new events");
							        }
							        
						        }
						        if(change.callback != null) {
							        change.callback.applied(res.getResult());
						        }
						        XSynchronizer.this.changes.remove(0);
					        } else {
						        if(res.getEvents().size() == 0) {
							        log.warn("sync: command failed but no new events, sync lost?");
							        // lost sync -> bad!!!
						        } else {
							        log.info("sync: command failed remotely, got new events");
							        // should fail in applyEvents
						        }
					        }
					        applyEvents(res.getEvents());
					        requestEnded(true);
				        }
				        
			        });
			
		} else {
			
			log.info("sync: getting events, rev is " + this.syncRevison);
			
			this.service.getEvents(this.addr, this.syncRevison + 1, XChangesService.NONE,
			        new Callback<List<XEvent>>() {
				        
				        public void onFailure(Throwable error) {
					        if(error instanceof ServiceException) {
						        log.info("sync: error getting events: " + error.getMessage());
					        } else {
						        log.info("sync: error getting events", error);
					        }
					        // TODO handle error;
					        requestEnded(false);
				        }
				        
				        public void onSuccess(List<XEvent> events) {
					        applyEvents(events);
					        requestEnded(true);
				        }
			        }, this.entity.getAddress());
			
		}
		
	}
	
	protected void applyEvents(List<XEvent> remoteChanges) {
		
		if(remoteChanges.size() == 0) {
			// no changes to merge
			return;
		}
		
		log.info("sync: merging " + remoteChanges.size() + " remote and " + this.changes.size()
		        + " local changes, local rev is " + getLocalRevisionNumber() + " (synced to "
		        + this.syncRevison + ")");
		
		XEvent[] events = remoteChanges.toArray(new XEvent[remoteChanges.size()]);
		long[] results = this.entity.synchronize(events);
		
		this.syncRevison += remoteChanges.size();
		
		log.info("sync: merged changes, new local rev is " + getLocalRevisionNumber()
		        + " (synced to " + this.syncRevison + ")");
		
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
		
		for(int i = results.length - 1; i >= 0; i--) {
			if(results[i] < 0) {
				this.changes.remove(i);
			}
		}
		
	}
	
	protected void requestEnded(boolean immediateRequest) {
		this.requestRunning = false;
		// TODO should this only be done when autoSync == true?
		if(immediateRequest && !this.changes.isEmpty()) {
			startRequest();
		}
	}
	
	/**
	 * Query the server for new remote changes. Local changes will be sent
	 * immediately.
	 */
	public void synchronize() {
		startRequest();
	}
	
}
