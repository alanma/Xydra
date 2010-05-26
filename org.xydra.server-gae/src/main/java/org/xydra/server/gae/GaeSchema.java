package org.xydra.server.gae;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;


/**
 * The mapping from Java constants to String identifiers for 'kind names' and
 * 'property names' in GAE {@link Entity}s.
 * 
 * @author voelkel
 */
public class GaeSchema {
	
	/** A kind of {@link Entity}, GAE kind */
	public static final String XREPOSITORY = "xrepository";
	/** A kind of {@link Entity}, GAE kind */
	public static final String XMODEL = "xmodel";
	/** A kind of {@link Entity}, GAE kind */
	public static final String XOBJECT = "xobject";
	/** A kind of {@link Entity}, GAE kind */
	public static final String XFIELD = "xfield";
	
	/** A property name, GAE type List<String> */
	public static final String PROP_CHILD_IDS = "childIDs";
	/** A property name, GAE type {@link Text} */
	public static final String PROP_VALUE = "value";
	/** A property name, GAE type {@link Long} */
	public static final String PROP_REVISION_NUMBER = "revisionNumber";
	/** A property name, GAE type String */
	public static final String PROP_PARENT_ADDRESS = "parentAddress";
	
}
