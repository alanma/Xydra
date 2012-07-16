package com.sonicmetrics.core.shared.query;

import java.util.Collection;

import org.xydra.annotations.NeverNull;


public interface ISonicFilter {
	
	/**
	 * @return the list of key-value-constraints
	 */
	@NeverNull
	Collection<KeyValueConstraint> getKeyValueConstraints();
	
	String getSubject();
	
	String getCategory();
	
	String getAction();
	
	String getLabel();
	
	String getSource();
	
}
