package org.xydra.store.access;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.XX;
import org.xydra.core.model.XID;
import org.xydra.store.MAXTodo;


/**
 * Access management related constants.
 * 
 * @author dscharrer
 */
@RunsInAppEngine
@RunsInGWT
@RunsInJava
@MAXTodo
public class XA {
	
	/**
	 * The access right to read content.
	 */
	public final static XID ACCESS_READ = XX.toId("read");
	
	/**
	 * The access right to write content.
	 */
	public final static XID ACCESS_WRITE = XX.toId("write");
	
	/**
	 * The access right to allow others access.
	 */
	public final static XID ACCESS_ALLOW = XX.toId("admin");
	
	/**
	 * The access right to deny others access.
	 */
	public final static XID ACCESS_DENY = XX.toId("deny");
	
	/**
	 * A special group that everyone is part of.
	 */
	public static final XID GROUP_ALL = XX.toId("all");
	
}
