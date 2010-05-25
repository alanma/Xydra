package org.xydra.core.change;

/**
 * A command that is not made up of multiple other commands.
 */
public interface XAtomicCommand extends XCommand {
	
	/**
	 * @return true if this event should be applied even if the revision number
	 *         doesn't match.
	 */
	boolean isForced();
	
	/**
	 * @return the current revision number of the entity this XCommand refers to
	 *         (for field commands) or adds/removes (for other commands).
	 */
	long getRevisionNumber();
	
}
