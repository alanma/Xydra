package org.xydra.store.impl.gae.changes;

import java.io.Serializable;

import org.xydra.persistence.ModelRevision;

/**
 * Contains a {@link ModelRevision} and a lastSilentCommit number
 * 
 * @author xamde
 * 
 */
public class GaeModelRevision implements Serializable {

	public static final GaeModelRevision createGaeModelRevDoesNotExistYet() {
		return new GaeModelRevision(ModelRevision.MODEL_DOES_NOT_EXIST_YET.revision(),
				ModelRevision.MODEL_DOES_NOT_EXIST_YET);
	}

	private static final long serialVersionUID = -6269753819062518229L;

	/**
	 * A pointer into the change item stack indicating the last such change item
	 * that has been processed without resulting in advancing the currentRev.
	 */
	private long lastSilentCommitted;

	/**
	 * can be null if not known
	 */
	private ModelRevision modelRev;

	/**
	 * @param lastSilentCommitted
	 *            highest committed revision s for which:
	 *            <ol>
	 *            <li>s >= revision;</li>
	 *            <li>s <= lastCommited;</li>
	 *            <li>for all x | r <= x <= lastCommited: x.status ==
	 *            {SuccessNoChange | FailedPreconditions | FailedTimeout }</li>
	 *            </ol>
	 *            . Use {@link RevisionInfo#NOT_SET} if not known.
	 * @param modelRev
	 *            can be null if not known
	 */
	public GaeModelRevision(long lastSilentCommitted, ModelRevision modelRev) {
		assert lastSilentCommitted >= -1;
		assert modelRev != null;
		this.lastSilentCommitted = lastSilentCommitted;
		this.modelRev = modelRev;
	}

	/**
	 * @return lastSilentCommited, if defined. Returns
	 *         {@link RevisionInfo#NOT_SET} if not known.
	 */
	public long getLastSilentCommitted() {
		return this.lastSilentCommitted;
	}

	/**
	 * @return null if not known
	 */
	public ModelRevision getModelRevision() {
		return this.modelRev;
	}

	void setLastSilentCommittedIfHigher(long lastSilentCommitted) {
		if (lastSilentCommitted > this.lastSilentCommitted) {
			this.lastSilentCommitted = lastSilentCommitted;
		}
	}

	public void clear() {
		this.lastSilentCommitted = -1;
		this.modelRev = ModelRevision.MODEL_DOES_NOT_EXIST_YET;
	}

	/**
	 * @param modelRev
	 *            never null
	 */
	void setCurrentModelRevisionIfRevIsHigher(ModelRevision modelRev) {
		assert modelRev != null;
		if (this.modelRev == null) {
			this.modelRev = modelRev;
		} else if (modelRev.revision() > this.modelRev.revision()) {
			this.modelRev = modelRev;
		}
	}

	@Override
	public String toString() {
		return getModelRevision() + "; silentCommit:" + this.lastSilentCommitted;
	}

}
