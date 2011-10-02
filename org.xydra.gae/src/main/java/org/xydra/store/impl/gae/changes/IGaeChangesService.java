package org.xydra.store.impl.gae.changes;

import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XEvent;
import org.xydra.store.Callback;
import org.xydra.store.GetEventsRequest;
import org.xydra.store.XydraStore;
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
	 * @return the model's current revision number. Non-existing models are
	 *         signalled as {@link XCommand#FAILED}, i.e. those that have just
	 *         been removed from the repository.
	 * @see XydraStore#getModelRevisions(XID, String, XAddress[], Callback)
	 */
	long getCurrentRevisionNumber();
	
	/**
	 * Fetch a range of events from the datastore.
	 * 
	 * See also {@link GetEventsRequest}.
	 * 
	 * @see XydraStore#getEvents(XID, String, GetEventsRequest[],
	 *      org.xydra.store.Callback)
	 * 
	 *      TODO localVm Implementation should cache retrieved events in a
	 *      localVmCache and never ask for them again.
	 * 
	 * @param beginRevision inclusive
	 * @param endRevision inclusive
	 * @return a list of events or null if this model was never created. The
	 *         list might contain fewer elements than the range implies.
	 */
	List<XEvent> getEventsBetween(long beginRevision, long endRevision);
	
	AsyncValue getValue(long fieldRev, int transindex);
	
	/**
	 * @return true if model exists
	 */
	boolean exists();
	
	/**
	 * Grabs the lowest available revision number and registers a change for
	 * that revision number with the provided locks.
	 * 
	 * @param locks which locks to get
	 * @param actorId The actor to record in the change {@link Entity}.
	 * @return Information associated with the change such as the grabbed
	 *         revision, the locks, the start time and the change {@link Entity}
	 *         .
	 * 
	 *         Note: Reads revCache.lastTaken
	 */
	GaeChange grabRevisionAndRegisterLocks(GaeLocks locks, XID actorId);
	
	/**
	 * Cache given change, if status is committed.
	 * 
	 * @param change to be cached
	 */
	void cacheCommittedChange(GaeChange change);
	
	GaeChange getChange(long rev);
	
	/**
	 * @return A revision number up to which all changes are guaranteed to be
	 *         committed. This revision number may be cached and out-dated.
	 */
	long getLastCommited();
	
	/**
	 * Mark the given change as committed.
	 * 
	 * @param status The new (and final) status.
	 */
	void commit(GaeChange change, Status status);
	
}
