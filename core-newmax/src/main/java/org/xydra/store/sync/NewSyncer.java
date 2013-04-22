package org.xydra.store.sync;

import java.util.ArrayList;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.base.change.XSyncEvent;
import org.xydra.base.rmof.XRevWritableModel;
import org.xydra.core.model.XField;
import org.xydra.core.model.XObject;
import org.xydra.core.model.impl.memory.EventDelta;
import org.xydra.core.model.impl.memory.EventSequenceMapper;
import org.xydra.core.model.impl.memory.EventSequenceMapper.Result;
import org.xydra.core.model.impl.memory.IMemoryModel;
import org.xydra.core.model.impl.memory.LocalChange;
import org.xydra.core.model.impl.memory.LocalChanges;
import org.xydra.core.model.impl.memory.ModelUtils;
import org.xydra.core.model.impl.memory.Root;
import org.xydra.core.model.impl.memory.XWritableChangeLog;
import org.xydra.index.query.Pair;
import org.xydra.persistence.GetEventsRequest;
import org.xydra.store.BatchedResult;
import org.xydra.store.Callback;
import org.xydra.store.XydraStore;


public class NewSyncer {
	
	private String passwordHash;
	
	private XId actorId;
	
	private XRevWritableModel modelState;
	
	private XWritableChangeLog changeLog;
	
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
		this.store = store;
		this.modelWithListeners = modelWithListeners;
		this.modelState = modelState;
		this.root = root;
		this.changeLog = root.getWritableChangeLog();
		this.localChanges = root.getLocalChanges();
		this.actorId = actorId;
		this.passwordHash = passwordHash;
		this.syncRev = syncRev;
	}
	
	private LocalChanges localChanges;
	
	private XydraStore store;
	
	private long syncRev;
	
	// TODO make sure it runs only once at the same time
	public void startSync() {
		
		this.localChanges.lock();
		
		// create commands to be sent from localChanges
		ArrayList<XCommand> commands = new ArrayList<XCommand>();
		for(LocalChange lc : this.localChanges.getList()) {
			XCommand cmd = lc.getCommand();
			commands.add(cmd);
		}
		GetEventsRequest getEventRequest = new GetEventsRequest(this.modelState.getAddress(),
		        this.syncRev, -1);
		this.store.executeCommandsAndGetEvents(this.actorId, this.passwordHash,
		        commands.toArray(new XCommand[commands.size()]),
		        new GetEventsRequest[] { getEventRequest }, new ServerCallback());
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
	 * <li>Add all Local Change-events (inversed) to the Event Delta -> so the
	 * Event Delta represents the integration of server state and local state
	 * <li>Fire sync events: The ones that concern locally triggered changes and
	 * the ones that concern changes received from the server
	 * <li>Apply the Event Delta to the Model State
	 * <li>update the Change Log, clear all local changes
	 * <li>send change events
	 * </ol>
	 * 
	 * TODO Max fragen, wo/wann revision numbers im model eingetragen werden
	 * 
	 * @param serverEvents
	 */
	private void continueSync(XEvent[] serverEvents) {
		// on callback:
		Result result = EventSequenceMapper.map(serverEvents, this.localChanges);
		
		EventDelta eventDelta = new EventDelta();
		for(XEvent e : serverEvents) {
			eventDelta.addEvent(e);
		}
		
		for(LocalChange lc : this.localChanges.getList()) {
			XEvent e = lc.getEvent();
			eventDelta.addInverseEvent(e, this.syncRev, this.changeLog);
		}
		
		// send sync events, let app see state before sync
		for(Pair<XEvent,LocalChange> p : result.mapped) {
			LocalChange lc = p.getSecond();
			// send sync-success events
			fireSyncEvent(lc, true);
		}
		for(LocalChange lc : result.nonMappedLocalEvents) {
			// TODO send sync-failed events
			fireSyncEvent(lc, false);
		}
		
		// start atomic section -----
		
		// change state
		eventDelta.applyTo(this.modelState);
		
		long newSyncRev = serverEvents[serverEvents.length - 1].getRevisionNumber();
		
		// change changeLog
		this.changeLog.truncateToRevision(this.syncRev);
		for(XEvent e : serverEvents) {
			this.changeLog.appendEvent(e);
		}
		
		this.localChanges.clear();
		
		this.root.setSyncRevision(newSyncRev);
		
		// end atomic section ----
		
		// send change events
		eventDelta.sendChangeEvents(this.root, this.modelWithListeners.getAddress(),
		        this.modelWithListeners.getFather().getAddress());
	}
	
	/**
	 * @param lc @NeverNull
	 * @param result true iff sync-success, false if sync-error
	 */
	private void fireSyncEvent(LocalChange lc, boolean result) {
		XEvent e = lc.getEvent();
		XAddress target = e.getTarget();
		XSyncEvent syncEvent = new XSyncEvent(target, result);
		
		switch(e.getTarget().getAddressedType()) {
		case XMODEL:
			this.root.fireSyncEvent(this.modelWithListeners.getAddress(), syncEvent);
			break;
		case XOBJECT:
			XObject object = ModelUtils.getObject(this.modelWithListeners, target);
			this.root.fireSyncEvent(object.getAddress(), syncEvent);
			break;
		case XFIELD:
			XField field = ModelUtils.getField(this.modelWithListeners, target);
			this.root.fireSyncEvent(field.getAddress(), syncEvent);
			break;
		case XREPOSITORY:
		default:
			throw new RuntimeException("cannot happen");
		}
	}
}
