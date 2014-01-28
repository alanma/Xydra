package org.xydra.xgae.gaeutils;

import com.google.appengine.api.taskqueue.QueueConstants;


/**
 * Limits defined by GAE infrastructure.
 * 
 * @author xamde
 */
public interface GaeConstants {
	
	/** 100 kilobytes, see also QueueConstants.maxTaskSizeBytes(); */
	public static final long MAX_GAE_TASk_PAYLOAD_SIZE = QueueConstants.maxPushTaskSizeBytes();
	
	/** 60 seconds. Adapted to latest GAE release as of 2011-11-22 */
	public static final long GAE_WEB_REQUEST_TIMEOUT = 60 * 1000;
	
	/**
	 * 10 minutes. Stated on
	 * http://code.google.com/intl/en/appengine/docs/java/urlfetch/overview.html
	 * as of 2012-01-16
	 */
	public static final long GAE_TASK_QUEUE_REQUEST_TIMEOUT = 10 * 60 * 1000;
	
	public static final long SimultaneousAsynchronousURLFetchCalls = 10;
	
	public static final long DATASTORE_MAX_ENTITY_SIZE_BYTES = 1024 * 1024;
	
}
