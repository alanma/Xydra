package org.xydra.base.change.impl.memory;

import org.xydra.base.change.XCommand;

public class RevisionConstants {

	/**
	 * Used by {@link XCommand}.
	 */
	public static final long JUST_CREATED_ENTITY = 0;

	/** -1. The entity is not existing; introduced at 02.05.2013 */
	public static final long NOT_EXISTING = -1;

	/**
	 * Impl note: This revision is also persisted on the server-side. Changing
	 * this will invalidate parsing old data.
	 *
	 * See {@link XCommand#NOCHANGE} for docu of semantics.
	 */
	public static final long COMMAND_FAILED = -1;

	/**
	 * Impl note: This revision is also persisted on the server-side. Changing
	 * this will invalidate parsing old data.
	 *
	 * See {@link XCommand#NOCHANGE} for docu of semantics.
	 */
	public static final long NOCHANGE = -2;

	/**
	 * The revision cannot be efficiently calculated.
	 *
	 * Used by XEvent.
	 */
	public static final long REVISION_NOT_AVAILABLE = -20;

	/**
	 * A revision number has not been set for this entity. E.g. if this XEvent
	 * has no such father-entity.
	 *
	 * Used by XEvent.
	 */
	public static final long REVISION_OF_ENTITY_NOT_SET = -21;

	/**
	 * Revision number returned by parser if no revision number was found.
	 *
	 * Used by MemoryMOF, SerializedModel
	 */
	public static final long NO_REVISION = -22;

	public static final long COMMAND_INTENT_FORCED = -10;

	public static final long COMMAND_INTENT_SAFE_STATE_BOUND = -11;

	/**
	 * The most recent available revisions. Exact meaning depends on usage
	 * context.
	 */
	public static final long LATEST = -30;

	public static String getName(final long revConstant) {
		if (revConstant == COMMAND_FAILED) {
			return "command failed";
		}
		if (revConstant == JUST_CREATED_ENTITY) {
			return "entity just created";
		}
		if (revConstant == LATEST) {
			return "latest";
		}
		if (revConstant == NO_REVISION) {
			return "no revision found";
		}
		if (revConstant == NOT_EXISTING) {
			return "entity does not exist";
		}
		if (revConstant == REVISION_NOT_AVAILABLE) {
			return "revision not available, maybe later";
		}
		if (revConstant == REVISION_OF_ENTITY_NOT_SET) {
			return "revision has not been set";
		}
		return "unknown " + revConstant;
	}
}
