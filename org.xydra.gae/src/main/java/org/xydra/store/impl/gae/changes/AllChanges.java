package org.xydra.store.impl.gae.changes;

import java.util.HashMap;
import java.util.Map;

import org.xydra.base.XAddress;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.sharedutils.XyAssert;

/**
 * In-memory representation for some change events. Required for
 * {@link GaeChangesServiceImpl3}
 *
 * This class is NOT thread-safe.
 *
 * @author xamde
 */
public class AllChanges {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AllChanges.class);

	/** Just the debug name */
	private final Map<Long, GaeChange> localMap = new HashMap<Long, GaeChange>();
	private final CommitedChanges commitedChanges;

	/**
	 * @param modelAddress
	 */
	public AllChanges(final XAddress modelAddress) {
		this.commitedChanges = new CommitedChanges(modelAddress);
	}

	/**
	 * @param rev
	 * @return cached change for this revisions. Can be null if (1) is really
	 *         null, (2) was just never indexed
	 */
	GaeChange getCachedChange(final long rev) {
		GaeChange change = this.commitedChanges.getCachedChange(rev);
		if (change == null) {
			change = this.localMap.get(rev);
		}
		return change;
	}

	/**
	 * Cache given change
	 *
	 * @param change
	 *            to be cached; never null
	 */
	public void cacheCommittedChange(final GaeChange change) {
		XyAssert.xyAssert(change != null);
		assert change != null;

		if (!change.getStatus().canChange()) {
			this.commitedChanges.cacheStableChange(change);
		} else {
			this.localMap.put(change.rev, change);
		}
	}

}
