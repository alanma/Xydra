package org.xydra.core.model.session;

import org.xydra.annotations.ModificationOperation;
import org.xydra.core.change.XCommand;



/**
 * An interface that indicates that this object is able to execute
 * {@link XCommand}s.
 * 
 */
public interface XProtectedExecutesCommands {
	
	/**
	 * Execute the given command if possible.
	 * 
	 * Not all implementations will be able to execute all commands.
	 * 
	 * @param command
	 * @return {@link XCommand#FAILED} if the command failed,
	 *         {@link XCommand#NOCHANGE} if the command didn't change anything
	 *         or the revision number of the event caused by the command.
	 */
	@ModificationOperation
	long executeCommand(XCommand command);
	
}
