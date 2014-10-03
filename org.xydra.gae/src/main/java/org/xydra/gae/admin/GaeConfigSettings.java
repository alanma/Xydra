package org.xydra.gae.admin;

/**
 * Manages configuration values specific to GAE aspects.
 * 
 * @author xamde
 * 
 */
public class GaeConfigSettings {

	public static final String PROP_ASSERT = "assert";

	public static final String PROP_USEMEMCACHE = "usememcache";

	public static final String CLEAR_LOCAL_VM_CACHE = "clearvmcache";

	public static final String CLEAR_LOCAL_VM_CACHE_LAST_EXECUTED = "clearvmcache_executed";

	/** only used for incoming web request to clear memcache immediately */
	public static final String PROP_CLEARMEMCACHE_NOW = "clearmemcache";

	public static final String PROCESS_CONFIG_NOW = "processconfignow";

}
