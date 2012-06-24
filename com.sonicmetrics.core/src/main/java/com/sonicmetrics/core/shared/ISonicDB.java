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
	
}
