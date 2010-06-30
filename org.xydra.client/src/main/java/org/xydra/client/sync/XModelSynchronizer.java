package org.xydra.client.sync;

import java.util.ArrayList;
import java.util.List;

import org.xydra.client.Callback;
import org.xydra.client.ServiceException;
import org.xydra.client.XChangesService;
import org.xydra.client.XChangesService.CommandResult;
import org.xydra.core.X;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;

import com.google.gwt.user.client.Timer;


public class XModelSynchronizer {
	
	private static final Logger log = LoggerFactory.getLogger(XModelSynchronizer.class);
	
	private static final XID LOCAL_ACTOR = X.getIDProvider().fromString("local");
	
	private long syncRevison;
	private final XModel model;
	private final XChangesService service;
	private final List<XCommand> commands;
	private final List<Callback<Long>> callbacks;
	
	private final Timer timer = new Timer() {
		@Override
		public void run() {
			startRequest();
		}
	};
	
	public XModelSynchronizer(XModel model, XChangesService service) {
		this.model = model;
		this.service = service;
		this.commands = new ArrayList<XCommand>();
		this.callbacks = new ArrayList<Callback<Long>>();
		this.timer.scheduleRepeating(5000);
		this.syncRevison = this.model.getRevisionNumber();
	}
	
	public void executeCommand(XCommand command, Callback<Long> callback) {
		assert this.callbacks.size() == this.commands.size();
		log.info("sync: got command: " + command);
		long result = this.model.executeCommand(LOCAL_ACTOR, command);
		if(result == XCommand.FAILED) {
			if(callback != null) {
				log.warn("sync: command failed immediately");
				callback.onFailure(null);
			}
		} else if(result == XCommand.NOCHANGE) {
			if(callback != null) {
				log.warn("sync: command already redundant");
				callback.onSuccess(result);
			}
		} else {
			this.commands.add(command);
			this.callbacks.add(callback);
			startRequest();
		}
		assert this.callbacks.size() == this.commands.size();
	}
	
	boolean requestRunning = false;
	
	private void startRequest() {
		
		if(this.requestRunning) {
			return;
		}
		this.requestRunning = true;
		
		assert this.callbacks.size() == this.commands.size();
		
		if(!this.commands.isEmpty()) {
			
			XCommand command = this.commands.get(0);
			final Callback<Long> callback = this.callbacks.get(0);
			
			log.info("sync: sending command " + command + ", rev is " + this.syncRevison);
			
			this.service.executeCommand(command, this.syncRevison, new Callback<CommandResult>() {
				public void onFailure(Throwable error) {
					if(error instanceof ServiceException) {
						log.info("sync: error sending command: " + error.getMessage());
					} else {
						log.info("sync: error sending command", error);
					}
					// TODO handle error;
					if(callback != null) {
						callback.onFailure(error);
					}
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
						if(callback != null) {
							callback.onSuccess(res.getResult());
						}
						XModelSynchronizer.this.commands.remove(0);
						XModelSynchronizer.this.callbacks.remove(0);
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
			
			this.service.getEvents(this.model.getAddress().getRepository(), this.model.getID(),
			        this.syncRevison, XChangesService.NONE, new Callback<List<XEvent>>() {
				        
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
			        });
			
		}
		
	}
	
	protected void applyEvents(List<XEvent> remoteChanges) {
		
		if(remoteChanges.size() == 0) {
			// no changes to merge
			return;
		}
		
		log.info("sync: merging " + remoteChanges.size() + " remote and " + this.commands.size()
		        + " local changes, local rev is " + this.model.getRevisionNumber() + " (synced to "
		        + this.syncRevison + ")");
		
		long[] results = this.model.synchronize(remoteChanges, this.syncRevison, LOCAL_ACTOR,
		        this.commands);
		
		this.syncRevison += remoteChanges.size();
		
		log.info("sync: merged changes, new local rev is " + this.model.getRevisionNumber()
		        + " (synced to " + this.syncRevison + ")");
		
		for(int i = 0; i < results.length; i++) {
			Callback<Long> callback = this.callbacks.get(i);
			if(results[i] == XCommand.FAILED) {
				log.info("sync: client command conflicted: " + this.commands.get(i));
				if(callback != null) {
					callback.onFailure(null);
				}
			} else if(results[i] == XCommand.NOCHANGE) {
				log.info("sync: client command redundant: " + this.commands.get(i));
				if(callback != null) {
					callback.onSuccess(results[i]);
				}
			}
		}
		
		for(int i = results.length - 1; i >= 0; i--) {
			if(results[i] < 0) {
				this.callbacks.remove(i);
				this.commands.remove(i);
			}
		}
		
	}
	
	protected void requestEnded(boolean immediateRequest) {
		assert this.callbacks.size() == this.commands.size();
		this.requestRunning = false;
		if(immediateRequest && !this.commands.isEmpty()) {
			startRequest();
		}
	}
	
	public void stopRefreshing() {
		this.timer.cancel();
	}
	
}
