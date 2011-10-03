package org.xydra.store.impl.gae.changes;

import org.xydra.base.XAddress;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.RevisionState;
import org.xydra.store.impl.gae.DebugFormatter;
import org.xydra.store.impl.gae.DebugFormatter.Timing;
import org.xydra.store.impl.gae.GaeOperation;
import org.xydra.store.impl.gae.InstanceContext;


/**
 * Manages the interplay of JVM instance cache (A {@link RevisionInfo} in
 * {@link InstanceContext}, the memcached values {@link MemcacheRevisionManager}
 * and the datastore value TODO)
 * 
 * This class should be used as a singleton.
 * 
 * TODO It is thread-safe.
 * 
 * TODO The calling class must call {@link #loadFromMemcache()} to profit from
 * the memcache.
 * 
 * @author xamde
 */
public class SharedRevisionManager implements IRevisionInfo {
	
	private static final String DATASOURCENAME = "[.sr]";
	
	private static final Logger log = LoggerFactory.getLogger(SharedRevisionManager.class);
	
	public static final int WRITE_TO_MEMCACHE_EVERY = 100; // revisions
	
	/**
	 * @param modelAddress ..
	 * @return cache name in local JVM {@link InstanceContext}
	 */
	public static final String getCacheName(XAddress modelAddress) {
		return modelAddress + "/revisions";
	}
	
	private MemcacheRevisionManager memcacheRevisionManager;
	
	private XAddress modelAddress;
	
	private RevisionInfo sharedRevisionInfo;
	
	@GaeOperation()
	SharedRevisionManager(XAddress modelAddress) {
		log.debug(DebugFormatter.init(DATASOURCENAME));
		this.modelAddress = modelAddress;
		this.sharedRevisionInfo = new RevisionInfo();
		this.memcacheRevisionManager = new MemcacheRevisionManager(modelAddress);
	}
	
	@Override
	public void clear() {
		log.debug(DebugFormatter.clear(DATASOURCENAME));
		this.sharedRevisionInfo.clear();
		this.memcacheRevisionManager.clear();
	}
	
	@Override
	public long getCurrentRev() {
		synchronized(this) {
			long result = this.sharedRevisionInfo.getCurrentRev();
			log.debug(DebugFormatter.dataGet(DATASOURCENAME, "currentRev", result, Timing.Now));
			return result;
		}
	}
	
	@Override
	public long getLastCommitted() {
		synchronized(this) {
			long result = this.sharedRevisionInfo.getLastCommitted();
			log.debug(DebugFormatter.dataGet(DATASOURCENAME, "lastCommited", result, Timing.Now));
			return result;
		}
	}
	
	@Override
	public long getLastTaken() {
		synchronized(this) {
			long result = this.sharedRevisionInfo.getLastTaken();
			log.debug(DebugFormatter.dataGet(DATASOURCENAME, "lastTaken", result, Timing.Now));
			return result;
		}
	}
	
	@Override
	public RevisionState getRevisionState() {
		synchronized(this) {
			RevisionState result = this.sharedRevisionInfo.getRevisionState();
			log.debug(DebugFormatter.dataGet(DATASOURCENAME, "revisionState", result, Timing.Now));
			return result;
		}
	}
	
	public void loadFromMemcache() {
		/* make sure to read only once */
		if(!this.memcacheRevisionManager.hasReadFromMemcache()) {
			this.memcacheRevisionManager.readFromMemcache();
			// push update to shared cache
			this.sharedRevisionInfo = this.memcacheRevisionManager.getCacheEntry();
		}
		
	}
	
	@Override
	public Boolean modelExists() {
		synchronized(this) {
			Boolean result = this.sharedRevisionInfo.modelExists();
			log.debug(DebugFormatter.dataGet(DATASOURCENAME, "modelExists", result, Timing.Now));
			return result;
		}
	}
	
	@Override
	public void setCurrentRevisionStateIfRevIsHigher(RevisionState revisionState) {
		log.debug(DebugFormatter
		        .dataPut(DATASOURCENAME, "revisionState", revisionState, Timing.Now));
		this.sharedRevisionInfo.setCurrentRevisionStateIfRevIsHigher(revisionState);
	}
	
	@Override
	public void setLastCommittedIfHigher(long lastCommitted) {
		log.debug(DebugFormatter
		        .dataPut(DATASOURCENAME, "lastCommitted", lastCommitted, Timing.Now));
		this.sharedRevisionInfo.setLastCommittedIfHigher(lastCommitted);
	}
	
	@Override
	public void setLastTakenIfHigher(long lastTaken) {
		log.debug(DebugFormatter.dataPut(DATASOURCENAME, "lastTaken", lastTaken, Timing.Now));
		this.sharedRevisionInfo.setLastTakenIfHigher(lastTaken);
	}
	
	@Override
	public String toString() {
		return "sharedInfo: " + this.sharedRevisionInfo.toString() + " memcache:"
		        + this.memcacheRevisionManager.toString();
	}
	
	public void writeToMemcache() {
		log.debug("Write to memcache " + this.modelAddress);
		this.memcacheRevisionManager.writeToMemcache(this.sharedRevisionInfo);
	}
	
}
