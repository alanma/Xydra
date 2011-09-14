package org.xydra.store;

/**
 * 
 * 
 * @author xamde
 * 
 */
public class RevisionState {
	
	private boolean modelExists;
	private long revision;
	
	public RevisionState(long revision, boolean modelExists) {
		this.revision = revision;
		this.modelExists = modelExists;
	}
	
	public long revision() {
		return this.revision;
	}
	
	public boolean modelExists() {
		return this.modelExists;
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof RevisionState && ((RevisionState)other).revision == this.revision
		        && ((RevisionState)other).modelExists == this.modelExists;
	}
	
	@Override
	public int hashCode() {
		return (int)(this.revision + (this.modelExists ? 0 : 1024));
	}
	
	@Override
	public String toString() {
		return this.revision + (this.modelExists ? "+" : "-");
	}
	
}
