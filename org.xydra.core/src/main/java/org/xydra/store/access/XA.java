package org.xydra.store.access;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.base.XID;
import org.xydra.base.XX;


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
	 * The access right to allow others access.
	 */
	public final static XID ACCESS_ALLOW = XX.toId("admin");
	
	/**
	 * The access right to deny others access.
	 */
	public final static XID ACCESS_DENY = XX.toId("deny");
	
	/**
	 * The access right to read content.
	 */
	public final static XID ACCESS_READ = XX.toId("read");
	
	/**
	 * The access right to write content.
	 */
	public final static XID ACCESS_WRITE = XX.toId("write");
	
	/**
	 * A special group that everyone is part of.
	 */
	public static final XID GROUP_ALL = XX.toId("all");
	
}
