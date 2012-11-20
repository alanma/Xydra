package com.sonicmetrics.core.shared.query;

import java.io.Serializable;

import org.xydra.annotations.RunsInGWT;
import org.xydra.sharedutils.XyAssert;


/**
 * Each defined filter is restricting the query. Null-values are treated as
 * wild-cards.
 * 
 * @author xamde
 */
@RunsInGWT(true)
public class KeyValueConstraint implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static KeyValueConstraint keyValue(String key, String value) {
		XyAssert.validateNotNull(key, "key");
		return new KeyValueConstraint(key, value);
	}
	
	protected KeyValueConstraint(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	public String getKey() {
		return this.key;
	}
	
	public String getValue() {
		return this.value;
	}
	
	private final String key;
	
	private final String value;
	
}
