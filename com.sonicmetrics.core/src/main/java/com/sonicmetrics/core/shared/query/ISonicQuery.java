package com.sonicmetrics.core.shared.query;

import org.xydra.annotations.NeverNull;


/**
 * @author xamde
 * 
 */
public interface ISonicQuery extends ISonicFilter {
	
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
