package org.xydra.store.impl.gae.changes;

public class RevisionInfoUtils {
	
	/**
	 * Make sure: current <= committed <= lastTaken.
	 * 
	 * Needs only be called if either currentRev or committed have changed.
	 * 
	 * @param revs where to maintain the invariants
	 * @param currentHasChanged ..
	 */
	public static void maintainInvariants(IRevisionInfo revs, boolean currentHasChanged) {
		long committed = revs.getLastCommitted(true);
		if(currentHasChanged) {
			long current = revs.getCurrentRev(true);
			if(current > committed) {
				revs.setLastCommitted(current);
				committed = current;
			}
		}
		long lastTaken = revs.getLastTaken(true);
		if(committed > lastTaken) {
			revs.setLastTaken(committed);
		}
	}
	
}
