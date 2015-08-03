package org.xydra.store.impl.gae.changes;

/**
 * A {@link GaeModelRevision} plus a flag to indicate if this is the final Model
 * rev or not
 *
 * @author xamde
 */
class CandidateRev {
	boolean finalModelRev = false;
	boolean inTentativeRange = false;
	GaeModelRevision gaeModelRev;

	/**
	 * @param gaeModelRev
	 *            never null
	 */
	public CandidateRev(final GaeModelRevision gaeModelRev) {
		assert gaeModelRev != null;
		assert gaeModelRev.getModelRevision() != null;
		this.gaeModelRev = gaeModelRev;
	}

	public boolean isFinalModelRev() {
		return this.finalModelRev;
	}

	public void markAsFinalRev() {
		this.finalModelRev = true;
	}

	public void setModelRev(final GaeModelRevision modelRev) {
		this.gaeModelRev = modelRev;
	}

	@Override
	public String toString() {
		return this.gaeModelRev + " finalRev?" + this.finalModelRev;
	}
}
