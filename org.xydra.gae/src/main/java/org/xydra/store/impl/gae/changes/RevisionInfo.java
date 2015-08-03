package org.xydra.store.impl.gae.changes;

import java.io.Serializable;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.xgae.util.XGaeDebugHelper;
import org.xydra.xgae.util.XGaeDebugHelper.Timing;

/**
 * This class:
 *
 * 1) Maintains these invariants: currentRev >= lastCommitted >= lastTaken.
 *
 * 2) Is {@link Serializable} and can be used in MemCache.
 *
 * 3) Is thread-safe.
 *
 * 4) ... is only relevant within the GAE implementation.
 *
 * A defined currentRev requires a defined value for modelExists.
 *
 * @author xamde
 */
public class RevisionInfo implements Serializable {

	private static final Logger log = LoggerFactory.getLogger(RevisionInfo.class);

	/**
	 * Returned if a value is not set. For non-existing models or if there is no
	 * known value, the value is -1.
	 */
	public static final long NOT_SET = -1;

	private static final long serialVersionUID = -8537625185285087183L;

	private String datasourceName;

	/** never null */
	private GaeModelRevision gaeModelRev;

	private long lastCommitted;

	private long lastTaken;

	/**
	 * Create a revision info that knows nothing.
	 *
	 * @param datasourceName
	 *            for debugging purposes
	 */
	public RevisionInfo(final String datasourceName) {
		this.datasourceName = datasourceName;
		this.gaeModelRev = GaeModelRevision.createGaeModelRevDoesNotExistYet();
		clear();
	}

	/**
	 * Create a revision info initialised with given values
	 *
	 * @param datasourceName
	 *            for debugging purposes
	 * @param modelRev
	 *            never null
	 * @param lastCommited
	 *            ..
	 * @param lastTaken
	 *            ..
	 */
	public RevisionInfo(final String datasourceName, final GaeModelRevision modelRev, final long lastCommited,
			final long lastTaken) {
		assert modelRev != null;
		this.datasourceName = datasourceName;
		this.gaeModelRev = modelRev;
		this.lastCommitted = lastCommited;
		this.lastTaken = lastTaken;
		log.debug(XGaeDebugHelper.init(this.datasourceName));
	}

	/**
	 * Reset to initial values that denote zero knowledge.
	 */
	public void clear() {
		log.debug(XGaeDebugHelper.clear(this.datasourceName));
		this.gaeModelRev.clear();
		this.lastCommitted = NOT_SET;
		this.lastTaken = NOT_SET;
	}

	/**
	 * @return the {@link GaeModelRevision}, never null
	 */
	public GaeModelRevision getGaeModelRevision() {
		synchronized (this) {
			final GaeModelRevision result = this.gaeModelRev;
			log.trace(XGaeDebugHelper.dataGet(this.datasourceName, "gaeModelRev", result,
					Timing.Now));
			return result;
		}
	}

	/**
	 * @return lastCommited if defined, {@link #NOT_SET} otherwise.
	 */
	public synchronized long getLastCommitted() {
		synchronized (this) {
			final long result = this.lastCommitted;
			log.trace(XGaeDebugHelper.dataGet(this.datasourceName, "lastCommitted", result,
					Timing.Now));
			return result;
		}
	}

	/**
	 * @return the last known revision number that has been grabbed by a change.
	 *         No guarantees are made that no higher revision numbers aren't
	 *         taken already. Returns {@link #NOT_SET} if not known.
	 */
	public synchronized long getLastTaken() {
		synchronized (this) {
			final long result = this.lastTaken;
			log.trace(XGaeDebugHelper.dataGet(this.datasourceName, "lastTaken", result, Timing.Now));
			return result;
		}
	}

	// public void setCurrentModelRevisionIfRevIsHigher(ModelRevision modelRev)
	// {
	// log.trace(DebugFormatter.dataPut(this.datasourceName,
	// "gaeModelRev.currentRev", modelRev,
	// Timing.Now));
	// this.getGaeModelRevision().setCurrentModelRevisionIfRevIsHigher(modelRev);
	// }

	// /**
	// * Set the given value as the new internal value only if it is higher than
	// * the current internal value.
	// *
	// * @param newGaeRev Can not be null.
	// */
	// public synchronized void
	// setCurrentModelRevisionIfRevIsHigher(GaeModelRevision newGaeRev) {
	// if(newGaeRev == null) {
	// throw new IllegalArgumentException("revisionState can not be null");
	// }
	// assert newGaeRev != null;
	// if(this.gaeModelRev.getModelRevision() == null
	// || (newGaeRev.getModelRevision() != null &&
	// newGaeRev.getModelRevision().revision() > this.gaeModelRev
	// .getModelRevision().revision())) {
	// log.debug(DebugFormatter.dataPut(this.datasourceName, "revisionState",
	// newGaeRev,
	// Timing.Now));
	// this.gaeModelRev = newGaeRev;
	// // invariant: currentRev >= lastCommitted
	// setLastCommittedIfHigher(newGaeRev.getModelRevision().revision());
	// }
	// }

	void setDatasourceName(final String datasourceName) {
		this.datasourceName = datasourceName;
	}

	protected void setGaeModelRev(final GaeModelRevision gaeModelRev) {
		assert gaeModelRev != null;
		log.debug(XGaeDebugHelper.dataPut(this.datasourceName, "gaeModelRev", gaeModelRev,
				Timing.Now));
		this.gaeModelRev = gaeModelRev;
	}

	/**
	 * Set the given value as the new internal value only if it is higher than
	 * the current internal value.
	 *
	 * @param lastCommitted
	 *            ..
	 */
	public synchronized void setLastCommittedIfHigher(final long lastCommitted) {
		if (lastCommitted > this.lastCommitted) {
			log.debug(XGaeDebugHelper.dataPut(this.datasourceName, "lastCommitted", lastCommitted,
					Timing.Now));
			this.lastCommitted = lastCommitted;
			// invariant: lastCommitted >= lastTaken
			setLastTakenIfHigher(lastCommitted);
		}
	}

	// public void setLastSilentCommittedIfHigher(long l) {
	// log.debug(DebugFormatter.dataPut(this.datasourceName,
	// "gaeModelRev.lastSilentCommitted", l,
	// Timing.Now));
	// this.getGaeModelRevision().setLastSilentCommittedIfHigher(l);
	// }

	/**
	 * Set the given value as the new internal value only if it is higher than
	 * the current internal value.
	 *
	 * @param lastTaken
	 *            ..
	 */
	public synchronized void setLastTakenIfHigher(final long lastTaken) {
		if (lastTaken > this.lastTaken) {
			log.debug(XGaeDebugHelper.dataPut(this.datasourceName, "lastTaken", lastTaken,
					Timing.Now));
			this.lastTaken = lastTaken;
		}
	}

	@Override
	public String toString() {
		return "{current:" + getGaeModelRevision() + "; lastTaken:" + getLastTaken()
				+ ",lastCommitted:" + getLastCommitted() + "}";
	}

	public boolean isBetterThan(final RevisionInfo other) {
		if (other == null) {
			return true;
		}

		if (this.gaeModelRev.getLastSilentCommitted() > other.gaeModelRev.getLastSilentCommitted()) {
			return true;
		}

		if (other.gaeModelRev.getModelRevision() == null) {
			if (this.gaeModelRev.getModelRevision() != null) {
				return true;
			} else {
				// both have no modelRev and same lastSilentCommited
				return this.lastTaken > other.lastTaken;
			}
		} else {
			assert other.gaeModelRev.getModelRevision() != null;
			return this.gaeModelRev.getModelRevision().isBetterThan(
					other.gaeModelRev.getModelRevision());
		}
	}

	/**
	 * @param gaeModelRev
	 *            never null
	 */
	public void setCurrentGaeModelRevIfRevisionIsHigher(final GaeModelRevision gaeModelRev) {
		assert gaeModelRev.getModelRevision() != null;
		if (gaeModelRev.getModelRevision().revision() > this.gaeModelRev.getModelRevision()
				.revision()) {
			log.trace(XGaeDebugHelper.dataPut(this.datasourceName, "gaeModelRev", gaeModelRev,
					Timing.Now));
			this.gaeModelRev = gaeModelRev;
		}
	}

	// /**
	// * @return currentRev if defined, {@link #NOT_SET} otherwise.
	// */
	// public synchronized long getCurrentRev() {
	// synchronized(this) {
	// long result = this.revisionState == null ? NOT_SET :
	// this.revisionState.revision();
	// log.trace(DebugFormatter.dataGet(this.datasourceName, "currentRev",
	// result, Timing.Now));
	// return result;
	// }
	// }
	//
	// /**
	// * @return true if model exists; false if not; null if not known.
	// */
	// public Boolean modelExists() {
	// synchronized(this) {
	// Boolean result = this.revisionState == null ? null :
	// this.revisionState.modelExists();
	// log.debug(DebugFormatter
	// .dataGet(this.datasourceName, "modelExists", result, Timing.Now));
	// return result;
	// }
	// }

}
