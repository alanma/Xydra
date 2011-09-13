package org.xydra.client.sync;

import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.client.Callback;
import org.xydra.client.ServiceException;
import org.xydra.client.XChangesService;
import org.xydra.client.XChangesService.CommandResult;
import org.xydra.core.model.XLocalChange;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XObject;
import org.xydra.core.model.XSynchronizesChanges;
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
 * Deprecated, replaced by {@link org.xydra.core.model.sync.XSynchronizer}
 * 
 * @author dscharrer
 * 
 */
@SuppressWarnings("deprecation")
@Deprecated
public class XSynchronizer {
	
	static private final Logger log = LoggerFactory.getLogger(XSynchronizer.class);
	
	private long syncRevison; // FIXME revision now tracked in entity.
	private final XSynchronizesChanges entity;
	private final XChangesService service;
	private final XAddress addr;
	
	boolean requestRunning = false;
	
	/**
	 * Start synchronizing the given model via the given service.
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
		assert addr.getField() == null;
		assert addr.getModel() != null;
		assert XI.equals(addr.getObject(), entity.getAddress().getObject());
	}
	
	/**
	 * Start synchronizing the given model via the given service.
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
		
		XLocalChange[] changes = this.entity.getLocalChanges();
		
		XLocalChange newChange = null;
		for(int i = 0; i < changes.length; i++) {
			if(!changes[i].isApplied()) {
				newChange = changes[i];
				break;
			}
		}
		
		if(newChange != null) {
			
			final XLocalChange change = newChange;
			
			log.info("sync: sending command " + change.getCommand() + ", rev is "
			        + this.syncRevison);
			
			this.service.executeCommand(this.addr, change.getCommand(), this.syncRevison + 1,
			        new Callback<CommandResult>() {
				        @Override
				        public void onFailure(Throwable error) {
					        if(error instanceof ServiceException) {
						        log.info("sync: error sending command: " + error.getMessage());
					        } else {
						        log.info("sync: error sending command", error);
					        }
					        // TODO handle error;
					        requestEnded(false);
				        }
				        
				        @Override
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
						        change.setRemoteResult(res.getResult());
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
				        
				        @Override
				        public void onFailure(Throwable error) {
					        if(error instanceof ServiceException) {
						        log.info("sync: error getting events: " + error.getMessage());
					        } else {
						        log.info("sync: error getting events", error);
					        }
					        // TODO handle error;
					        requestEnded(false);
				        }
				        
				        @Override
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
			log.info("sync: no remote changes to merge");
			return;
		}
		
		XEvent[] events = remoteChanges.toArray(new XEvent[remoteChanges.size()]);
		this.entity.synchronize(events);
		
	}
	
	protected void requestEnded(boolean immediateRequest) {
		this.requestRunning = false;
		// TODO should this only be done when autoSync == true?
		if(immediateRequest && this.entity.countUnappliedLocalChanges() > 0) {
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
