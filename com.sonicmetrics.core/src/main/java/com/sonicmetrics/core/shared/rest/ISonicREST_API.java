package com.sonicmetrics.core.shared.rest;

import org.xydra.annotations.RunsInGWT;


/**
 * Defines shared keys that are use in the REST API and in the JSON
 * serialisation.
 * 
 * @author xamde
 */
@RunsInGWT(true)
public interface ISonicREST_API {
	
	/** Parameter name in URL and databases for unique key */
	public static final String KEY = "key";
	
	/**
	 * Parameter name in URL and databases for subject, about which subject is
	 * the event
	 */
	public static final String SUBJECT = "subject";
	
	/**
	 * Parameter name in URL and databases for category, like in Google
	 * Analytics
	 */
	public static final String CATEGORY = "category";
	
	/** Parameter name in URL and databases for action, like in Google Analytics */
	public static final String ACTION = "action";
	
	/** Parameter name in URL and databases for label, like in Google Analytics */
	public static final String LABEL = "label";
	
	/**
	 * Parameter name in URL and databases for time-stamp
	 */
	public static final String WHEN = "when";
	
	/**
	 * Parameter name in URL and databases for the source/sender/origin.
	 */
	public static final String SOURCE = "source";
	
	/**
	 * Parameter name in URL and databases for uniqueId.
	 */
	public static final String UNIQUEID = "uniqueid";
	
	/** Parameter name in URL for API key. Not stored in DB */
	public static final String APIKEY = "apikey";
	
	/** Support JSON-P */
	public static final String CALLBACK = "callb∆ack";
	
	/** Parameter names used in URLs, alphabetical order */
	public static final String[] RESERVED_KEYS = { ACTION, APIKEY, CALLBACK, CATEGORY, KEY, LABEL,
	        SOURCE, SUBJECT, UNIQUEID, WHEN };
	
}
