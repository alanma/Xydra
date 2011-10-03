package org.xydra.store.impl.gae.changes;

import java.io.Serializable;

import org.xydra.store.RevisionState;


/**
 * This class:
 * 
 * 1) Implements {@link IRevisionInfo} and maintains the invarian currentRev >=
 * lastCommitted >= lastTaken.
 * 
 * 2) Is {@link Serializable} and can be used in MemCache.
 * 
 * 3) Knows if it has internal knowledge that has not been saved yet. Methods:
 * {@link #hasUnsavedChanges()} and {@link #markChangesAsSaved()}.
 * 
 * 4) Is thread-safe.
 * 
 * @author xamde
 * 
 */
public class RevisionInfo implements Serializable, IRevisionInfo {
	
	private static final long serialVersionUID = -8537625185285087183L;
	
	/**
	 * Create a revision info that knows nothing.
	 */
	public RevisionInfo() {
		clear();
	}
	
	@Override
	public void clear() {
		this.revisionState = null;
		this.lastCommitted = NOT_SET;
		this.lastTaken = NOT_SET;
		this.unsavedChanges = 0;
	}
	
	/** can be null */
	private RevisionState revisionState;
	private long lastCommitted;
	private long lastTaken;
	private int unsavedChanges;
	
	@Override
	public synchronized long getLastCommitted() {
		return this.lastCommitted;
	}
	
	@Override
	public synchronized long getLastTaken() {
		return this.lastTaken;
	}
	
	@Override
	public synchronized long getCurrentRev() {
		return this.revisionState == null ? NOT_SET : this.revisionState.revision();
	}
	
	@Override
	public synchronized Boolean modelExists() {
		return this.revisionState == null ? null : this.revisionState.modelExists();
	}
	
	@Override
	public synchronized void setCurrentRevisionStateIfRevIsHigher(RevisionState revisionState) {
		if(revisionState == null) {
			throw new IllegalArgumentException("revisionState can not be null");
		}
		if(this.revisionState == null || revisionState.revision() > this.revisionState.revision()) {
			this.revisionState = revisionState;
			this.unsavedChanges++;
			// invariant: currentRev >= lastCommitted
			setLastCommittedIfHigher(revisionState.revision());
		}
	}
	
	@Override
	public synchronized void setLastCommittedIfHigher(long lastCommitted) {
		if(lastCommitted > this.lastCommitted) {
			this.lastCommitted = lastCommitted;
			this.unsavedChanges++;
			// invariant: lastCommitted >= lastTaken
			setLastTakenIfHigher(lastCommitted);
		}
	}
	
	@Override
	public synchronized void setLastTakenIfHigher(long lastTaken) {
		if(lastTaken > this.lastTaken) {
			this.lastTaken = lastTaken;
			this.unsavedChanges++;
		}
	}
	
	public synchronized boolean hasUnsavedChanges() {
		return this.unsavedChanges > 0;
	}
	
	public synchronized void markChangesAsSaved() {
		this.unsavedChanges = 0;
	}
	
	@Override
	public RevisionState getRevisionState() {
		return this.revisionState;
	}
	
	@Override
	public String toString() {
		return "{current:" + getCurrentRev() + ",lastTaken:" + getLastTaken() + ",lastCommitted:"
		        + getLastCommitted() + ",modelExists:" + modelExists() + "}; unsavedChanges="
		        + this.hasUnsavedChanges();
	}
	
}
