package com.sonicmetrics.core.shared.query;

import org.xydra.annotations.RunsInGWT;
import org.xydra.sharedutils.XyAssert;

import com.sonicmetrics.core.shared.ISonicEvent;
import com.sonicmetrics.core.shared.ISonicEvent.IndexedProperty;


/**
 * Each defined filter is restricting the query. Null-values are treated as
 * wild-cards.
 * 
 * @author xamde
 */
@RunsInGWT(true)
public class KeyValueConstraint {
	
	public static KeyValueConstraint keyValue(ISonicEvent.IndexedProperty key, String value) {
		XyAssert.validateNotNull(key, "key");
		XyAssert.validateNotNull(value, "value for key='" + key + "'");
		return new KeyValueConstraint(key, value);
	}
	
	private KeyValueConstraint(IndexedProperty key, String value) {
		this.key = key;
		this.value = value;
	}
	
	public final IndexedProperty key;
	
	public final String value;
	
}
