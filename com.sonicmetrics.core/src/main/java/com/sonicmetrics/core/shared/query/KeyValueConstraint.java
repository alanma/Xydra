package com.sonicmetrics.core.shared.query;

import org.xydra.annotations.RunsInGWT;
import org.xydra.sharedutils.XyAssert;

import com.sonicmetrics.core.shared.ISonicEvent;
import com.sonicmetrics.core.shared.ISonicPotentialEvent.FilterProperty;


/**
 * Each defined filter is restricting the query. Null-values are treated as
 * wild-cards.
 * 
 * @author xamde
 */
@RunsInGWT(true)
public class KeyValueConstraint {
	
	public static KeyValueConstraint keyValue(ISonicEvent.FilterProperty key, String value) {
		XyAssert.validateNotNull(key, "key");
		return new KeyValueConstraint(key, value);
	}
	
	private KeyValueConstraint(FilterProperty key, String value) {
		this.key = key;
		this.value = value;
	}
	
	public final FilterProperty key;
	
	public final String value;
	
}
