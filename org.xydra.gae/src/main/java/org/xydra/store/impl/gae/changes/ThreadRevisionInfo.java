package org.xydra.store.impl.gae.changes;

import org.xydra.base.XAddress;
import org.xydra.core.model.impl.memory.UUID;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.RevisionState;
import org.xydra.store.impl.gae.DebugFormatter;
import org.xydra.store.impl.gae.DebugFormatter.Timing;


/**
 * ThreadLocal revision numbers.
 * 
 * @author xamde
 * 
 */
public class ThreadRevisionInfo {
	
	public static final String DATASOURCENAME = "[.tl-" + UUID.uuid(4) + "]";
	
	private static final Logger log = LoggerFactory.getLogger(ThreadRevisionInfo.class);
	
	private XAddress modelAddress;
	
	private RevisionState revisionState;
	
	public ThreadRevisionInfo(XAddress modelAddress) {
		this.modelAddress = modelAddress;
		log.debug(DebugFormatter.init(DATASOURCENAME + "-" + this.modelAddress));
		this.revisionState = null;
	}
	
	public void clear() {
		log.debug(DebugFormatter.clear(DATASOURCENAME + "-" + this.modelAddress));
		this.revisionState = null;
	}
	
	public long getCurrentRev() {
		long currentRev = getCurrentRev_internal();
		log.debug(DebugFormatter.dataGet(DATASOURCENAME, "currentRev", currentRev, Timing.Now));
		return currentRev;
	}
	
	private long getCurrentRev_internal() {
		return this.revisionState == null ? IRevisionInfo.NOT_SET : this.revisionState.revision();
	}
	
	public Boolean modelExists() {
		Boolean modelExists = this.revisionState == null ? null : this.revisionState.modelExists();
		return modelExists;
	}
	
	public void setRevisionStateIfRevIsHigher(long currentRev, boolean modelExists) {
		setRevisionStateIfRevIsHigherAndNotNull(new RevisionState(currentRev, modelExists));
	}
	
	/**
	 * // TODO Caller should also update instanceContext
	 * 
	 * @param revisionState can be null, but null is ignored
	 */
	public void setRevisionStateIfRevIsHigherAndNotNull(RevisionState revisionState) {
		if(revisionState == null) {
			return;
		}
		
		if(this.revisionState == null || this.revisionState.revision() < revisionState.revision()) {
			if(this.revisionState == null) {
				log.debug(DATASOURCENAME + " was null");
			}
			this.revisionState = revisionState;
			log.debug(DebugFormatter.dataPut(DATASOURCENAME, "currentRev",
			        getCurrentRev_internal(), Timing.Now));
		}
	}
	
	@Override
	public String toString() {
		return DATASOURCENAME + "(" + this.modelAddress + ") = "
		        + (this.revisionState == null ? "null" : this.revisionState.toString());
	}
	
	public RevisionState getRevisionState() {
		return this.revisionState;
	}
	
}
