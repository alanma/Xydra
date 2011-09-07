package org.xydra.store.impl.gae.changes;

import org.xydra.base.XAddress;


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
	
	/**
	 * @param sharedMinimalRevisionInfo to which all updates are propagated
	 */
	public ThreadLocalExactRevisionInfo(SharedMinimalRevisionInfo sharedMinimalRevisionInfo) {
		this.sharedMinimalRevisionInfo = sharedMinimalRevisionInfo;
		clear();
	}
	
	@Override
    public long getLastTaken(boolean mayAsk) {
		if(this.lastTaken == NOT_SET && mayAsk) {
			this.lastTaken = this.sharedMinimalRevisionInfo.getLastTaken(true);
		}
		return this.lastTaken;
	}
	
	@Override
    public void setLastTaken(long lastTaken) {
		this.lastTaken = lastTaken;
		this.sharedMinimalRevisionInfo.setLastTaken(lastTaken);
	}
	
	@Override
    public long getLastCommitted(boolean mayAsk) {
		if(this.lastCommitted == NOT_SET && mayAsk) {
			this.lastCommitted = this.sharedMinimalRevisionInfo.getLastTaken(true);
		}
		return this.lastCommitted;
	}
	
	@Override
    public void setLastCommitted(long lastCommitted) {
		this.lastCommitted = lastCommitted;
		this.sharedMinimalRevisionInfo.setLastCommitted(lastCommitted);
		RevisionInfoUtils.maintainInvariants(this, false);
	}
	
	@Override
    public long getCurrentRev(boolean mayAsk) {
		if(this.currentRev == NOT_SET && mayAsk) {
			this.currentRev = this.sharedMinimalRevisionInfo.getCurrentRev(true);
		}
		return this.currentRev;
	}
	
	@Override
    public void setCurrentRev(long currentRev) {
		this.currentRev = currentRev;
		this.sharedMinimalRevisionInfo.setCurrentRev(currentRev);
		RevisionInfoUtils.maintainInvariants(this, true);
	}
	
	private long lastTaken;
	private long lastCommitted;
	private long currentRev;
	
	public void clear() {
		this.currentRev = NOT_SET;
		this.lastCommitted = NOT_SET;
		this.lastTaken = NOT_SET;
	}
	
}
