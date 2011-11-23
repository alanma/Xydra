package org.xydra.store;

import java.io.Serializable;

import org.xydra.base.change.XCommand;


/**
 * Part of the public {@link XydraStore} API, this class models the state a
 * model can be in. The revision number is always getting bigger, although not
 * necessarily in increments of one. However, even if the model does not exist
 * -- because it has been deleted -- it still has a revision number. This makes
 * synchronising models much easier to implement an causes few confusion for
 * users, we hope.
 * 
 * Contains a {@link #revision()}, which is the model's current revision number.
 * Non-existing models are signalled as {@link XCommand#FAILED}, i.e. those that
 * have just been removed from the repository.
 * 
 * Contains also a {@link #modelExists()} flag which is true if the model has
 * been created and not deleted yet. It can of course be-recreated.
 * 
 * @author xamde
 */
public class ModelRevision implements Serializable {
	
	private static final long serialVersionUID = 2428661786025001891L;
	
	public static final ModelRevision MODEL_DOES_NOT_EXIST_YET = new ModelRevision(-1, false);
	
	private final boolean modelExists;
	private final long revision;
	
	public ModelRevision(long revision, boolean modelExists) {
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
		return other instanceof ModelRevision && ((ModelRevision)other).revision == this.revision
		        && ((ModelRevision)other).modelExists == this.modelExists;
	}
	
	@Override
	public int hashCode() {
		return (int)(this.revision + (this.modelExists ? 0 : 1024));
	}
	
	/**
	 * @return [revisionnumber] + 'yes' if model exists, 'no' otherwise
	 */
	@Override
	public String toString() {
		return this.revision + (this.modelExists ? "yes" : "no");
	}
	
}
