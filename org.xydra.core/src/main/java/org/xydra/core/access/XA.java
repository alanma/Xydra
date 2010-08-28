package org.xydra.core.access;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.X;
import org.xydra.core.model.XID;


/**
 * Access management related constants.
 * 
 * @author dscharrer
 */
@RunsInAppEngine
@RunsInGWT
@RunsInJava
public class XA {
	
	/**
	 * The access right to read content.
	 */
	public final static XID ACCESS_READ = X.getIDProvider().fromString("read");
	
	/**
	 * The access right to write content.
	 */
	public final static XID ACCESS_WRITE = X.getIDProvider().fromString("write");
	
	/**
	 * The access right to allow others access.
	 */
	public final static XID ACCESS_ALLOW = X.getIDProvider().fromString("allow_access");
	
	/**
	 * The access right to deny others access.
	 */
	public final static XID ACCESS_DENY = X.getIDProvider().fromString("deny");
	
	/**
	 * A special group that everyone is part of.
	 */
	public static final XID GROUP_ALL = X.getIDProvider().fromString("all");
	
}
