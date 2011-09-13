package org.xydra.store.impl.gae.changes;

import org.xydra.base.XAddress;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.store.impl.gae.DebugFormatter;
import org.xydra.store.impl.gae.DebugFormatter.Timing;


/**
 * ThreadLocal revision numbers.
 * 
 * @author xamde
 * 
 */
public class ThreadLocalExactRevisionInfo implements IRevisionInfo {
	
	public static final String getCacheName(XAddress modelAddress) {
		return modelAddress + "/ThreadLocalExactRevisionInfo";
	}
	
	private SharedMinimalRevisionInfo sharedMinimalRevisionInfo;
	private XAddress modelAddress;
	private static final Logger log = LoggerFactory.getLogger(ThreadLocalExactRevisionInfo.class);
	private static final String DATASOURCENAME = "[.tl]";
	
	/**
	 * @param sharedMinimalRevisionInfo to which all updates are propagated
	 */
	public ThreadLocalExactRevisionInfo(XAddress modelAddress,
	        SharedMinimalRevisionInfo sharedMinimalRevisionInfo) {
		this.modelAddress = modelAddress;
		this.sharedMinimalRevisionInfo = sharedMinimalRevisionInfo;
		clear();
	}
	
	@Override
	public long getLastTaken(boolean mayAsk) {
		if(this.lastTaken == NOT_SET && mayAsk) {
			this.lastTaken = this.sharedMinimalRevisionInfo.getLastTaken(true);
		}
		log.trace(DebugFormatter.dataGet(DATASOURCENAME, "lastTaken", this.lastTaken, Timing.Now));
		return this.lastTaken;
	}
	
	@Override
	public void setLastTaken(long lastTaken) {
		this.lastTaken = lastTaken;
		this.sharedMinimalRevisionInfo.setLastTaken(lastTaken);
		log.trace(DebugFormatter.dataPut(DATASOURCENAME, "lastTaken", lastTaken, Timing.Now)
		        + ". New values=" + this.toString());
	}
	
	@Override
	public long getLastCommitted(boolean mayAsk) {
		if(this.lastCommitted == NOT_SET && mayAsk) {
			this.lastCommitted = this.sharedMinimalRevisionInfo.getLastCommitted(true);
		}
		log.trace(DebugFormatter.dataGet(DATASOURCENAME, "lastCommitted", this.lastCommitted,
		        Timing.Now));
		return this.lastCommitted;
	}
	
	@Override
	public void setLastCommitted(long lastCommitted) {
		this.lastCommitted = lastCommitted;
		this.sharedMinimalRevisionInfo.setLastCommitted(lastCommitted);
		RevisionInfoUtils.maintainInvariants(this, false);
		log.trace(DebugFormatter
		        .dataPut(DATASOURCENAME, "lastCommitted", lastCommitted, Timing.Now)
		        + ". New values=" + this.toString());
	}
	
	@Override
	public long getCurrentRev(boolean mayAsk) {
		if(this.currentRev == NOT_SET && mayAsk) {
			this.currentRev = this.sharedMinimalRevisionInfo.getCurrentRev(true);
		}
		log.trace(DebugFormatter.dataGet(DATASOURCENAME, "currentRev", this.currentRev, Timing.Now));
		return this.currentRev;
	}
	
	@Override
	public void setCurrentRev(long currentRev) {
		this.currentRev = currentRev;
		this.sharedMinimalRevisionInfo.setCurrentRev(currentRev);
		RevisionInfoUtils.maintainInvariants(this, true);
		log.trace(DebugFormatter.dataPut(DATASOURCENAME, "currentRev", currentRev, Timing.Now)
		        + ". New values=" + this.toString());
	}
	
	private long lastTaken;
	private long lastCommitted;
	private long currentRev;
	private Boolean exists;
	
	public void clear() {
		log.debug("Cleared ThreadLocalExactCache of " + this.modelAddress);
		this.currentRev = NOT_SET;
		this.lastCommitted = NOT_SET;
		this.lastTaken = NOT_SET;
	}
	
	@Override
	public String toString() {
		return "currentRev=" + this.currentRev + " lastCommitted=" + this.lastCommitted
		        + " lastTaken=" + this.lastTaken;
	}
	
	@Override
	public void setModelExists(boolean exists) {
		this.exists = exists;
	}
	
	@Override
	public Boolean modelExists() {
		return this.exists;
	}
	
}
