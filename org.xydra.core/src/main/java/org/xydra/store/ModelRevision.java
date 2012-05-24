package org.xydra.store;

import java.io.Serializable;

import org.xydra.annotations.NeverNull;
import org.xydra.sharedutils.XyAssert;


/**
 * Part of the public {@link XydraStore} API, this class models the state a
 * model can be in. The revision number is always getting bigger, although not
 * necessarily in increments of one. However, even if the model does not exist
 * -- because it has been deleted -- it still has a revision number. This makes
 * synchronising models much easier to implement. And causes few confusion for
 * users, we hope.
 * 
 * Contains a {@link #revision()}, which is the model's current revision number.
 * 
 * Non-existing models are either reported as {@link #MODEL_DOES_NOT_EXIST_YET}
 * if it never existed (has not been managed). A previously existing but now
 * deleted model simply has a growing, positive revision number. This makes it
 * easier to layer synchronisation protocols on top.
 * 
 * Contains also a {@link #modelExists()} flag which is true if the model is
 * currently in a state where it has been created and not deleted yet.
 * 
 * @author xamde
 */
public class ModelRevision implements Serializable {
	
	private static final long serialVersionUID = 2428661786025001891L;
	
	public static final long TENTATIVE_REV_UNDEFINED = -2;
	public static final ModelRevision MODEL_DOES_NOT_EXIST_YET = new ModelRevision(-1, false);
	
	/** model exists unknown for tentative */
	private final long tentativeRevision;
	/** About the standard revision */
	private final boolean modelExists;
	private final long revision;
	
	/**
	 * @param revision the current revision number
	 * @param modelExists true, if the model exists, false otherwise
	 * @param tentativeRevision allows to create tentative revisions
	 */
	public ModelRevision(long revision, boolean modelExists, long tentativeRevision) {
		this.revision = revision;
		this.modelExists = modelExists;
		this.tentativeRevision = tentativeRevision;
	}
	
	public ModelRevision(long revision, boolean modelExists) {
		this.revision = revision;
		this.modelExists = modelExists;
		this.tentativeRevision = TENTATIVE_REV_UNDEFINED;
	}
	
	public long revision() {
		return this.revision;
	}
	
	/**
	 * @return a tentative revision number which is defined as the highest
	 *         successfully committed event. It is at least equal to the normal,
	 *         stable model revision.
	 */
	public long tentativeRevision() {
		return this.tentativeRevision;
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
	
	/**
	 * @param other @NeverNull
	 * @return true if the other rev contains more up-to-date information than
	 *         this one
	 */
	public boolean isBetterThan(@NeverNull ModelRevision other) {
		XyAssert.xyAssert(other != null);
		assert other != null;
		// TODO tentative rev here?
		return this.revision > other.revision;
	}
	
}
