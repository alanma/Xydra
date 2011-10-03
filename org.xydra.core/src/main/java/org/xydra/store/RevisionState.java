package org.xydra.store;

import java.io.Serializable;


/**
 * Part of the public {@link XydraStore} API, this class models the state a
 * model can be in. The revision number is always getting bigger, although not
 * necessarily in increments of one. However, even if the model does not exist
 * -- because it has been deleted -- it still has a revision number. This makes
 * synchronising models much easier to implement an causes few confusion for
 * users, we hope.
 * 
 * @author xamde
 */
public class RevisionState implements Serializable {
	
	private static final long serialVersionUID = 2428661786025001891L;
	private final boolean modelExists;
	private final long revision;
	
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
	
	/**
	 * @return [revisionnumber] + '+' if model exists, '-' otherwise
	 */
	@Override
	public String toString() {
		return this.revision + (this.modelExists ? "+" : "-");
	}
	
}
