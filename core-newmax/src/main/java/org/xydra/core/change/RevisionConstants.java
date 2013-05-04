package org.xydra.core.change;

public class RevisionConstants {
	
	/** the entity is not existing; introduced at 02.05.2013 */
	public static final long NOT_EXISTING = -1;
	
	/** The revision cannot be efficiently calculated. */
	public static long REVISION_NOT_AVAILABLE = -2;
	
	/**
	 * A revision number has not been set for this entity. E.g. if this XEvent
	 * has no such father-entity.
	 */
	public static long REVISION_OF_ENTITY_NOT_SET = -4;
	
	/** revision number returned by parser if no revision number was found */
	public static final long NO_REVISION = -5;
	
}
