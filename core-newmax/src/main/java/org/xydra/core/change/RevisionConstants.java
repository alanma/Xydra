package org.xydra.core.change;

public class RevisionConstants {
	
	/** the entity is not existing; introduced at 02.05.2013 */
	public static final long NOTEXISTING = -1;
	
	/** The revision cannot be efficiently calculated. */
	public static long REVISIONNOTAVAILABLE = -2;
	
	/**
	 * A revision number has not been set for this entity. E.g. if this XEvent
	 * has no such father-entity.
	 */
	public static long REVISIONOFENTITYNOTSET = -4;
	
	/** revision number returned by parser if no revision number was found */
	public static final long NO_REVISION = -5;
	
}
