package org.xydra.client.gwt.sync;

import java.util.ArrayList;
import java.util.List;

import org.xydra.client.gwt.Callback;
import org.xydra.client.gwt.XChangesService;
import org.xydra.client.gwt.XChangesService.CommandResult;
import org.xydra.core.X;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XID;
import org.xydra.core.model.XLoggedModel;
import org.xydra.core.model.XModel;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Timer;


public class XModelSynchronizer {
	
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
		Log.info("sync: got command: " + command);
		long result = this.model.executeCommand(LOCAL_ACTOR, command);
		if(result == XCommand.FAILED) {
			if(callback != null) {
				callback.onFailure(null);
			}
		} else if(result == XCommand.NOCHANGE) {
			if(callback != null) {
				callback.onSuccess(result);
			}
		} else {
			this.commands.add(command);
			this.callbacks.add(callback);
			startRequest();
		}
		assert this.callbacks.size() == this.commands.size();
	}
	
	public XLoggedModel getModel() {
		return this.model;
	}
	
	boolean requestRunning = false;
	
	private void startRequest() {
		
		if(this.requestRunning) {
			return;
		}
		this.requestRunning = true;
		
		Log.info("sync: starting request, rev is " + this.syncRevison);
		
		assert this.callbacks.size() == this.commands.size();
		
		if(!this.commands.isEmpty()) {
			
			final XCommand command = this.commands.get(0);
			final Callback<Long> callback = this.callbacks.get(0);
			
			this.service.executeCommand(command, this.syncRevison, new Callback<CommandResult>() {
				public void onFailure(Throwable error) {
					// TODO handle error;
					if(callback != null) {
						callback.onFailure(error);
					}
					requestEnded();
				}
				
				public void onSuccess(CommandResult res) {
					if(res.getResult() != XCommand.FAILED) {
						if(callback != null) {
							callback.onSuccess(res.getResult());
						}
						XModelSynchronizer.this.commands.remove(0);
						XModelSynchronizer.this.callbacks.remove(0);
					} else {
						if(res.getEvents().size() == 0) {
							Log.warn("sync: command failed but no new events, sync lost?");
							// lost sync -> bad!!!
						} else {
							Log.info("command failed remotely, got new events: " + command);
							// should fail in applyEvents
						}
					}
					applyEvents(res.getEvents());
					requestEnded();
				}
				
			});
			
		} else {
			
			this.service.getEvents(this.model.getAddress().getRepository(), this.model.getID(),
			        this.syncRevison, XChangesService.NONE, new Callback<List<XEvent>>() {
				        
				        public void onFailure(Throwable error) {
					        // TODO handle error;
					        requestEnded();
				        }
				        
				        public void onSuccess(List<XEvent> events) {
					        applyEvents(events);
					        requestEnded();
				        }
			        });
			
		}
		
	}
	
	protected void applyEvents(List<XEvent> remoteChanges) {
		
		long[] results = this.model.synchronize(remoteChanges, this.syncRevison, LOCAL_ACTOR,
		        this.commands);
		
		this.syncRevison += remoteChanges.size();
		
		Log.info("sync: merged changes, new rev is " + this.syncRevison);
		
		for(int i = 0; i < results.length; i++) {
			if(results[i] < 0) {
				this.callbacks.get(i).onSuccess(results[i]);
			}
		}
		
		for(int i = results.length - 1; i >= 0; i--) {
			if(results[i] < 0) {
				this.callbacks.remove(i);
				this.commands.remove(i);
			}
		}
		
	}
	
	protected void requestEnded() {
		assert this.callbacks.size() == this.commands.size();
		this.requestRunning = false;
		if(!this.commands.isEmpty()) {
			startRequest();
		}
	}
	
}
