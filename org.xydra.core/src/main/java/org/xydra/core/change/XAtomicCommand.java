package org.xydra.core.change;

/**
 * An {@link XCommand} that is not made up of multiple other {@link XCommand
 * XCommands}.
 */
public interface XAtomicCommand extends XCommand {
	
	/**
	 * @return true if this event should be applied even if the revision number
	 *         of the entity that will be changed by this command doesn't match
	 *         the revision number this command refers to.
	 */
	boolean isForced();
	
	/**
	 * @return the current revision number of the entity this {@link XCommand}
	 *         refers to (for field commands) or adds/removes (for other
	 *         commands).
	 */
	long getRevisionNumber();
	
}
