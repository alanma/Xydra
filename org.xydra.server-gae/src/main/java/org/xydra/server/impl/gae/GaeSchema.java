package org.xydra.server.impl.gae;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;


/**
 * The mapping from Java constants to String identifiers for 'kind names' and
 * 'property names' in GAE {@link Entity}s.
 * 
 * @author voelkel
 */
public class GaeSchema {
	
	/** A property name, GAE type List<String> */
	public static final String PROP_CHILD_IDS = "childIDs";
	/** A property name, GAE type {@link Text} */
	public static final String PROP_VALUE = "value";
	/** A property name, GAE type {@link Long} */
	public static final String PROP_REVISION_NUMBER = "revisionNumber";
	
}
