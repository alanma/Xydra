package org.xydra.store.impl.gae.changes;

import org.xydra.store.ModelRevision;


public class GaeModelRevision extends ModelRevision {
	
	public static final GaeModelRevision GAE_MODEL_DOES_NOT_EXIST_YET = new GaeModelRevision(
	        ModelRevision.MODEL_DOES_NOT_EXIST_YET.revision(), false,
	        ModelRevision.MODEL_DOES_NOT_EXIST_YET.revision());
	
	private static final long serialVersionUID = -6269753819062518229L;
	
	private long lastSilentCommitted;
	
	/**
	 * @param revision ..
	 * @param modelExists ..
	 * @param lastSilentCommitted highest committed revision s for which:
	 *            <ol>
	 *            <li>s >= revision;</li>
	 *            <li>s <= lastCommited;</li>
	 *            <li>for all x | r <= x <= lastCommited: x.status ==
	 *            {SuccessNoChange | FailedPreconditions | FailedTimeout }</li>
	 *            </ol>
	 */
	public GaeModelRevision(long revision, boolean modelExists, long lastSilentCommitted) {
		super(revision, modelExists);
		this.lastSilentCommitted = lastSilentCommitted;
	}
	
	public long getLastSilentCommitted() {
		return this.lastSilentCommitted;
	}
	
	public void setLastSilentCommittedIfHigher(long lastSilentCommitted) {
		if(lastSilentCommitted > getLastSilentCommitted()) {
			this.lastSilentCommitted = lastSilentCommitted;
		}
	}
	
}
