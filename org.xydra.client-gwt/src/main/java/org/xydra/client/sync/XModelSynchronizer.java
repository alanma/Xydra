package org.xydra.client.sync;

import java.util.ArrayList;
import java.util.List;

import org.xydra.client.gwt.Callback;
import org.xydra.client.gwt.XChangesService;
import org.xydra.client.gwt.XChangesService.CommandResult;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.XCommand;
import org.xydra.core.change.XEvent;
import org.xydra.core.model.XID;
import org.xydra.core.model.XLoggedModel;
import org.xydra.core.model.XModel;
import org.xydra.index.query.Pair;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Timer;


public class XModelSynchronizer {
	
	private static final XID LOCAL_ACTOR = X.getIDProvider().fromString("local");
	
	private long syncRevison;
	private final XModel model;
	private final XChangesService service;
	private final List<Pair<XCommand,Callback<Long>>> commands;
	
	private final Timer timer = new Timer() {
		@Override
		public void run() {
			startRequest();
		}
	};
	
	public XModelSynchronizer(XModel model, XChangesService service) {
		this.model = model;
		this.service = service;
		this.commands = new ArrayList<Pair<XCommand,Callback<Long>>>();
		this.timer.scheduleRepeating(5000);
		this.syncRevison = this.model.getRevisionNumber();
	}
	
	public void executeCommand(XCommand command, Callback<Long> callback) {
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
			this.commands.add(new Pair<XCommand,Callback<Long>>(command, callback));
			startRequest();
		}
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
		
		if(!this.commands.isEmpty()) {
			
			final Pair<XCommand,Callback<Long>> p = this.commands.get(0);
			
			this.service.executeCommand(p.getFirst(), this.model.getRevisionNumber(),
			        new Callback<CommandResult>() {
				        public void onFailure(Throwable error) {
					        // TODO handle error;
					        if(p.getSecond() != null) {
						        p.getSecond().onFailure(error);
					        }
					        requestEnded();
				        }
				        
				        public void onSuccess(CommandResult res) {
					        if(res.getResult() != XCommand.FAILED) {
						        if(p.getSecond() != null) {
							        p.getSecond().onSuccess(res.getResult());
						        }
						        XModelSynchronizer.this.commands.remove(0);
					        } else {
						        if(res.getEvents().size() == 0) {
							        Log.warn("sync: command failed but no new events, sync lost?");
							        // lost sync -> bad!!!
						        } else {
							        // should fail in applyEvents
						        }
					        }
					        applyEvents(res.getEvents());
					        requestEnded();
				        }
				        
			        });
			
		} else {
			
			this.service.getEvents(this.model.getAddress().getRepository(), this.model.getID(),
			        this.model.getRevisionNumber(), XChangesService.NONE,
			        new Callback<List<XEvent>>() {
				        
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
	
	protected void applyEvents(List<XEvent> events) {
		
		this.model.rollback(this.syncRevison);
		
		for(XEvent event : events) {
			XCommand command = XX.createReplayCommand(event);
			long result = this.model.executeCommand(event.getActor(), command);
			if(result < 0) {
				Log.warn("sync: event from server doesn't apply: " + event
				        + "; generated command: " + command);
				// TODO should not happen (lost sync?)
			}
		}
		
		this.syncRevison = this.model.getRevisionNumber();
		
		// re-apply local commands
		for(int i = 0; i < this.commands.size(); i++) {
			
			Pair<XCommand,Callback<Long>> lc = this.commands.get(i);
			
			Log.info("sync: reapplying command " + lc.getFirst());
			
			long result = this.model.executeCommand(LOCAL_ACTOR, lc.getFirst());
			if(result == XCommand.FAILED) {
				if(lc.getSecond() != null) {
					Log.info("reapplying command " + lc.getFirst() + " failed");
					lc.getSecond().onFailure(null);
				}
				this.commands.remove(i);
			} else if(result == XCommand.NOCHANGE) {
				if(lc.getSecond() != null) {
					Log.info("command " + lc.getFirst() + " didn't change anything");
					lc.getSecond().onSuccess(result);
				}
				this.commands.remove(i);
			} else {
				
				// OK, waiting to apply command on server
			}
			
		}
	}
	
	protected void requestEnded() {
		this.requestRunning = false;
		if(!this.commands.isEmpty()) {
			startRequest();
		}
	}
	
}
