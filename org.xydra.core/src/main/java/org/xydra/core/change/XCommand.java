package org.xydra.core.change;

import org.xydra.core.model.XAddress;


public interface XCommand {
	
	/**
	 * special revision for commands that should be executed no matter what the
	 * current revision is
	 */
	static final long FORCED = -1;
	
	/**
	 * only for ADD events (except XFieldCommand), others should use a specific
	 * revision
	 */
	static final long SAFE = -2;
	
	/**
	 * revision of new fields during a transaction.
	 */
	static final long NEW = 0;
	
	/**
	 * returned when executing commands to indicate that the command failed
	 */
	static final long FAILED = -1;
	
	/**
	 * returned when executing commands to indicate that the command succeeded
	 * but nothing actually changed
	 */
	static final long NOCHANGE = -2;
	
	/**
	 * returned when executing repository commands to indicate that the command
	 * succeeded and changed something
	 */
	static final long CHANGED = 0;
	
	/**
	 * @return the type of change.
	 */
	ChangeType getChangeType();
	
	/**
	 * WHERE will this change happen?
	 * 
	 * For repository, model and object commands this does NOT include the
	 * model, object or field being added or removed.
	 * 
	 * @return the XAddress of the place where the change happened
	 */
	XAddress getTarget();
	
}
