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
	 * Access to read content.
	 */
	public final static XID ACCESS_READ = X.getIDProvider().fromString("read");
	
	/**
	 * Access to write content.
	 */
	public final static XID ACCESS_WRITE = X.getIDProvider().fromString("write");
	
	/**
	 * Access to change allow others access.
	 */
	public final static XID ACCESS_ALLOW = X.getIDProvider().fromString("allow_access");
	
	/**
	 * Access to change allow others deny.
	 */
	public final static XID ACCESS_DENY = X.getIDProvider().fromString("deny");
	
	/**
	 * A special group that contains everyone.
	 */
	public static final XID GROUP_ALL = X.getIDProvider().fromString("all");
	
}
