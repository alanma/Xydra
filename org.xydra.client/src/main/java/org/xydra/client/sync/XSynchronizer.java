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
import org.xydra.core.model.XAddress;
import org.xydra.core.model.XID;
import org.xydra.core.model.XModel;
import org.xydra.core.model.XSynchronizesChanges;
import org.xydra.index.XI;

import com.allen_sauer.gwt.log.client.Log;


/**
 * A class that can synchronize local and remote changes made to an
 * {@link XModel}.
 * 
 * @author dscharrer
 * 
 */
public class XSynchronizer {
	
	private static final XID LOCAL_ACTOR = X.getIDProvider().fromString("local");
	
	private long syncRevison;
	private final XSynchronizesChanges entity;
	private final XChangesService service;
	private final List<XCommand> commands;
	private final List<XCommandCallback> callbacks;
	private final XAddress addr;
	
	boolean requestRunning = false;
	
	/**
	 * Start synchronizing the given model (which has no local changes) via the
	 * given service. Any further changes applied directly to the model will be
	 * lost. To persist changes supply the to the
	 * {@link #executeCommand(XCommand, Callback)} Method.
	 */
	public XSynchronizer(XAddress addr, XSynchronizesChanges entity, XChangesService service) {
		Log.info("sync: init with entity " + addr + " | " + entity.getAddress());
		this.addr = addr;
		this.entity = entity;
		this.service = service;
		this.commands = new ArrayList<XCommand>();
		this.callbacks = new ArrayList<XCommandCallback>();
		this.syncRevison = getLocalRevisionNumber();
		assert addr.getField() == null;
		assert addr.getModel() != null;
		assert XI.equals(addr.getObject(), entity.getAddress().getObject());
	}
	
	/**
	 * Execute a command. The command will be immediately applied locally.
	 * 
	 * @param command The command to apply.
	 * @param callback A callback that will be notified if the command fails or
	 *            is permanently applied.
	 */
	public void executeCommand(XCommand command, XCommandCallback callback) {
		assert this.callbacks.size() == this.commands.size();
		Log.info("sync: got command: " + command);
		long result = this.entity.executeCommand(LOCAL_ACTOR, command);
		if(result == XCommand.FAILED) {
			Log.warn("sync: command failed immediately");
			if(callback != null) {
				callback.failed();
				callback.failedPost();
			}
		} else if(result == XCommand.NOCHANGE) {
			Log.warn("sync: command already redundant");
			if(callback != null) {
				callback.applied(result);
			}
		} else {
			this.commands.add(command);
			this.callbacks.add(callback);
			startRequest();
		}
		assert this.callbacks.size() == this.commands.size();
	}
	
	public long getLocalRevisionNumber() {
		return this.entity.getChangeLog().getCurrentRevisionNumber();
	}
	
	private void startRequest() {
		
		if(this.requestRunning) {
			return;
		}
		this.requestRunning = true;
		
		assert this.callbacks.size() == this.commands.size();
		
		if(!this.commands.isEmpty()) {
			
			XCommand command = this.commands.get(0);
			final XCommandCallback callback = this.callbacks.get(0);
			
			Log.info("sync: sending command " + command + ", rev is " + this.syncRevison);
			
			this.service.executeCommand(this.addr, command, this.syncRevison,
			        new Callback<CommandResult>() {
				        public void onFailure(Throwable error) {
					        if(error instanceof ServiceException) {
						        Log.info("sync: error sending command: " + error.getMessage());
					        } else {
						        Log.info("sync: error sending command", error);
					        }
					        // TODO handle error;
					        requestEnded(false);
				        }
				        
				        public void onSuccess(CommandResult res) {
					        if(res.getResult() != XCommand.FAILED) {
						        if(res.getResult() >= 0) {
							        Log.info("sync: command applied remotely");
						        } else {
							        if(res.getEvents().size() == 0) {
								        Log.warn("sync: command didn't change anything remotely, "
								                + "but not new events, sync lost?");
								        // lost sync -> bad!!!
							        } else {
								        Log.info("sync: command failed remotely, got new events");
							        }
							        
						        }
						        if(callback != null) {
							        callback.applied(res.getResult());
						        }
						        XSynchronizer.this.commands.remove(0);
						        XSynchronizer.this.callbacks.remove(0);
					        } else {
						        if(res.getEvents().size() == 0) {
							        Log.warn("sync: command failed but no new events, sync lost?");
							        // lost sync -> bad!!!
						        } else {
							        Log.info("sync: command failed remotely, got new events");
							        // should fail in applyEvents
						        }
					        }
					        applyEvents(res.getEvents());
					        requestEnded(true);
				        }
				        
			        }, this.entity.getAddress());
			
		} else {
			
			Log.info("sync: getting events, rev is " + this.syncRevison);
			
			this.service.getEvents(this.addr, this.syncRevison, XChangesService.NONE,
			        new Callback<List<XEvent>>() {
				        
				        public void onFailure(Throwable error) {
					        if(error instanceof ServiceException) {
						        Log.info("sync: error getting events: " + error.getMessage());
					        } else {
						        Log.info("sync: error getting events", error);
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
		
		Log.info("sync: merging " + remoteChanges.size() + " remote and " + this.commands.size()
		        + " local changes, local rev is " + getLocalRevisionNumber() + " (synced to "
		        + this.syncRevison + ")");
		
		long[] results = this.entity.synchronize(remoteChanges, this.syncRevison, LOCAL_ACTOR,
		        this.commands, null);
		
		this.syncRevison += remoteChanges.size();
		
		Log.info("sync: merged changes, new local rev is " + getLocalRevisionNumber()
		        + " (synced to " + this.syncRevison + ")");
		
		for(int i = 0; i < results.length; i++) {
			XCommandCallback callback = this.callbacks.get(i);
			if(results[i] == XCommand.FAILED) {
				Log.info("sync: client command conflicted: " + this.commands.get(i));
				if(callback != null) {
					callback.failedPost();
				}
			} else if(results[i] == XCommand.NOCHANGE) {
				Log.info("sync: client command redundant: " + this.commands.get(i));
				if(callback != null) {
					callback.applied(results[i]);
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
	
	/**
	 * Query the server for new remote changes. Local changes will be sent
	 * immediately.
	 */
	public void synchronize() {
		startRequest();
	}
	
}
