package com.sonicmetrics.core.shared;

import org.xydra.annotations.RunsInGWT;

import com.sonicmetrics.core.shared.query.ISonicListener;
import com.sonicmetrics.core.shared.query.ISonicQuery;
import com.sonicmetrics.core.shared.query.ISonicQueryResult;
import com.sonicmetrics.core.shared.query.SonicMetadataResult;


/**
 * @author xamde
 * 
 */
@RunsInGWT(true)
public interface ISonicDB extends ISonicListener {
	
	/**
	 * @param sonicQuery
	 * @return the result of executing the query
	 */
	ISonicQueryResult query(ISonicQuery sonicQuery);
	
	/**
	 * Deletes all events matching the query
	 * 
	 * @param sonicQuery
	 */
	void delete(ISonicQuery sonicQuery);
	
	public SonicMetadataResult search(String keyword);
	
	/**
	 * @return the current time as used by this database
	 */
	long getCurrentTime();
	
	/**
	 * Set a key string as it is used in the backend.
	 * 
	 * @param sonicEvent without a key
	 */
	void setKey(ISonicEvent sonicEvent);
	
	/**
	 * Method returns before event is persisted/sent.
	 * 
	 * @param sonicEvent to be received, may have a key defined which was
	 *            obtained via {@link #setKey(ISonicEvent)} in the same
	 *            implementation
	 */
	void receiveEventAsync(ISonicEvent sonicEvent);
	
}
