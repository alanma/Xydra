package org.xydra.store.impl.rest;

import org.xydra.store.GetEventsRequest;


/**
 * URL suffixes and argument names used in the XydraStore REST API.
 * 
 * @author dscharrer
 * 
 */
public interface XydraStoreRestInterface {
	
	static final String URL_REPOSITORY_ID = "repository/id";
	static final String URL_SNAPSHOTS = "snapshots";
	static final String URL_REVISIONS = "revisions";
	static final String URL_MODEL_IDS = "repository/models";
	static final String URL_EVENTS = "events";
	static final String URL_EXECUTE = "execute";
	static final String URL_LOGIN = "login";
	static final String URL_PING = "ping";
	
	/** Cookie / argument name for passing the id of the actor. */
	static final String ARG_ACTOR_ID = "actorId";
	
	/** Cookie / argument name for passing the password hash of the actor. */
	static final String ARG_PASSWORD_HASH = "passwordHash";
	
	/** Address argument for events, snapshots and revisions requests. */
	static final String ARG_ADDRESS = "address";
	
	/**
	 * Begin revision argument for events requests.
	 * 
	 * @see GetEventsRequest
	 */
	static final String ARG_END_REVISION = "endRevision";
	
	/**
	 * End revision argument for events requests.
	 * 
	 * @see GetEventsRequest
	 */
	static final String ARG_BEGIN_REVISION = "beginRevision";
	
	static final String DEFAULT_CONTENT_TYPE = "application/xml";
	static final String DEFAULT_JSON_CONTENT_TYPE = "application/json";
	
	/** Argument name for the JSON callback. */
	static final String ARG_CALLBACK = "callback";
	
	/**
	 * Argument to set the expected content type, overwriting the HTTP Accept
	 * header.
	 */
	static final String ARG_ACCEPT = "accept";
	
}
