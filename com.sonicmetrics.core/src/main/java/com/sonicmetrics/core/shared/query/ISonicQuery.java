package com.sonicmetrics.core.shared.query;

import java.util.List;

import org.xydra.annotations.NeverNull;


/**
 * @author xamde
 * 
 */
public interface ISonicQuery {
	
	/**
	 * @return the list of key-value-constraints
	 */
	@NeverNull
	List<KeyValueConstraint> getKeyValueConstraints();
	
	/**
	 * @return the (potentially trivial) time constraint
	 */
	@NeverNull
	TimeConstraint getTimeConstraint();
	
	/**
	 * @return the limit or 0 if none defined
	 */
	int getLimit();
}
