package org.xydra.base.change;

/**
 * An {@link XCommand} that is not made up of multiple other {@link XCommand
 * XCommands}.
 */
public interface XAtomicCommand extends XCommand {
	
	/**
	 * @return the current revision number of the entity this {@link XCommand}
	 *         refers to (for field commands) or adds/removes (for other
	 *         commands). Can also be {@link XCommand#FORCED} or
	 *         {@link XCommand#SAFE}.
	 * 
	 *         More precisely: There are three kinds of commands: forced, safe
	 *         bound to revisions, safe bound to state.
	 * 
	 *         Forced commands have the rev nr FORCED.
	 * 
	 *         safe with precondition have SAFE. Such commands are only executed
	 *         if possible, e.g. an object add command is only executed as
	 *         success, if the object was not there yet.
	 * 
	 *         All other rev nrs denote an expected rev nr. Only if the entitiy
	 *         this command refers to has the expected given rev nr at execution
	 *         time, then the command will be executed.
	 */
	long getRevisionNumber();
	
	/**
	 * @return true if this event should be applied even if the revision number
	 *         of the entity that will be changed by this command doesn't match
	 *         the revision number this command refers to.
	 */
	boolean isForced();
	
}