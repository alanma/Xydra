package org.xydra.store.impl.gae.changes;

@Deprecated
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
		long committed = revs.getLastCommitted();
		if(currentHasChanged) {
			long current = revs.getCurrentRev();
			if(current > committed) {
				revs.setLastCommittedIfHigher(current);
				committed = current;
			}
		}
		long lastTaken = revs.getLastTaken();
		if(committed > lastTaken) {
			revs.setLastTakenIfHigher(committed);
		}
	}
	
}
