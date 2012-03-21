package org.xydra.store.impl.gae.changes;

import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XEvent;
import org.xydra.store.Callback;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.XydraStore;
import org.xydra.store.impl.gae.RevisionManager;
import org.xydra.store.impl.gae.changes.GaeChange.Status;
import org.xydra.store.impl.gae.changes.GaeEvents.AsyncValue;

import com.google.appengine.api.datastore.Entity;


public interface IGaeChangesService {
	
	/**
	 * @return the {@link XAddress} of the model managed by this
	 *         {@link IGaeChangesService} instance.
	 */
	XAddress getModelAddress();
	
	/**
	 * Fetch a range of events from the datastore.
	 * 
	 * See also {@link GetEventsRequest}.
	 * 
	 * @param address TODO
	 * @param beginRevision inclusive
	 * @param endRevision inclusive
	 * 
	 * @see XydraStore#getEvents(XID, String, GetEventsRequest[],
	 *      org.xydra.store.Callback)
	 * 
	 *      TODO localVm Implementation should cache retrieved events in a
	 *      localVmCache and never ask for them again.
	 * 
	 * @return a list of events or null if this model was never created. The
	 *         list might contain fewer elements than the range implies.
	 */
	List<XEvent> getEventsBetween(XAddress address, long beginRevision, long endRevision);
	
	AsyncValue getValue(long fieldRev, int transindex);
	
	/**
	 * Calculate with model change log and try to progress the currently known
	 * current revision number (from {@link RevisionManager} The result is
	 * stored in {@link RevisionManager} and returned for convenience.
	 * 
	 * @return ...
	 * 
	 * @see XydraStore#getModelRevisions(XID, String, XAddress[], Callback)
	 */
	GaeModelRevision calculateCurrentModelRevision();
	
	/**
	 * Grabs the lowest available revision number and registers a change for
	 * that revision number with the provided locks.
	 * 
	 * @param lastTaken
	 * 
	 * @param locks which locks to get
	 * @param actorId The actor to record in the change {@link Entity}.
	 * @return Information associated with the change such as the grabbed
	 *         revision, the locks, the start time and the change {@link Entity}
	 *         .
	 * 
	 *         Note: Reads revCache.lastTaken
	 */
	GaeChange grabRevisionAndRegisterLocks(long lastTaken, GaeLocks locks, XID actorId);
	
	/**
	 * Cache given change, if status is committed.
	 * 
	 * @param change to be cached
	 */
	void cacheCommittedChange(GaeChange change);
	
	GaeChange getChange(long rev);
	
	/**
	 * Mark the given change as committed.
	 * 
	 * @param change
	 * 
	 * @param status The new (and final) status.
	 */
	void commit(GaeChange change, Status status);
	
	/**
	 * @return true if this model has ever been created in the store. Might have
	 *         been deleted afterwards.
	 */
	boolean modelHasBeenManaged();
	
}
