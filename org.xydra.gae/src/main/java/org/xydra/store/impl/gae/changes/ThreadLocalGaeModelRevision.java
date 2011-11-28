package org.xydra.store.impl.gae.changes;

import org.xydra.base.XAddress;
import org.xydra.core.model.impl.memory.UUID;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.impl.gae.DebugFormatter;
import org.xydra.store.impl.gae.DebugFormatter.Timing;
import org.xydra.store.impl.gae.RevisionManager;


/**
 * ThreadLocal revision numbers. Helps to keep revision numbers consistent
 * within a single web request. Thread-local revisions are used, e.g., for
 * getSnapshot() and getCurrentRevision() kind of requests.
 * 
 * @author xamde
 */
public class ThreadLocalGaeModelRevision {
	
	public static final String DATASOURCENAME = "[.tl-" + UUID.uuid(4) + "]";
	
	private static final Logger log = LoggerFactory.getLogger(ThreadLocalGaeModelRevision.class);
	
	private XAddress modelAddress;
	
	/** never null */
	private GaeModelRevision gaeModelRev;
	
	public ThreadLocalGaeModelRevision(XAddress modelAddress) {
		this.modelAddress = modelAddress;
		log.debug(DebugFormatter.init(DATASOURCENAME + "-" + this.modelAddress));
		this.gaeModelRev = null;
	}
	
	public void clear() {
		log.debug(DebugFormatter.clear(DATASOURCENAME + "-" + this.modelAddress));
		this.gaeModelRev = null;
	}
	
	public long getCurrentRev() {
		long currentRev = getCurrentRev_internal();
		log.debug(DebugFormatter.dataGet(DATASOURCENAME, "currentRev", currentRev, Timing.Now));
		return currentRev;
	}
	
	/**
	 * No logging
	 * 
	 * @return current rev
	 */
	private long getCurrentRev_internal() {
		return this.gaeModelRev.getModelRevision() == null ? RevisionInfo.NOT_SET
		        : this.gaeModelRev.getModelRevision().revision();
	}
	
	@Override
	public String toString() {
		return DATASOURCENAME + "(" + this.modelAddress + ") = "
		        + (this.gaeModelRev == null ? "null" : this.gaeModelRev.toString());
	}
	
	public GaeModelRevision getGaeModelRev() {
		return this.gaeModelRev;
	}
	
	/**
	 * A caller that learned something new about revisions should also tell this
	 * in the {@link RevisionManager#getInstanceRevisionInfo()}
	 * 
	 * @param gaeModelRevision never null
	 */
	public void setGaeModelRev(GaeModelRevision gaeModelRevision) {
		this.gaeModelRev = gaeModelRevision;
		log.debug(DebugFormatter.dataPut(DATASOURCENAME, "currentRev", getCurrentRev_internal(),
		        Timing.Now));
	}
	
}
