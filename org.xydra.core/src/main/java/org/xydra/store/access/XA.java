package org.xydra.store.access;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XId;
import org.xydra.core.XX;


/**
 * Access management related constants.
 *
 * @author dscharrer
 */
@RunsInAppEngine(true)
@RunsInGWT(true)
@RequiresAppEngine(false)
public class XA {

	/**
	 * The access right to allow others access.
	 */
	public final static XId ACCESS_ALLOW = XX.toId("admin");

	/**
	 * The access right to deny others access.
	 */
	public final static XId ACCESS_DENY = XX.toId("deny");

	/**
	 * The access right to read content.
	 */
	public final static XId ACCESS_READ = XX.toId("read");

	/**
	 * The access right to write content.
	 */
	public final static XId ACCESS_WRITE = XX.toId("write");

	/**
	 * A special group that everyone is part of.
	 */
	public static final XId GROUP_ALL = XX.toId("all");

}
