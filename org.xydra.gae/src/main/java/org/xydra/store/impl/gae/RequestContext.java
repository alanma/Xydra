package org.xydra.store.impl.gae;

import java.util.HashMap;
import java.util.Map;

/**
 * A context object that can be passed around during a single web request.
 * Within one request, it is considered OK to retrieve fresh data only once from
 * the back-end store.
 * 
 * @author xamde
 */
public class RequestContext {

	public Map<String, Object> cache = new HashMap<String, Object>();

}
