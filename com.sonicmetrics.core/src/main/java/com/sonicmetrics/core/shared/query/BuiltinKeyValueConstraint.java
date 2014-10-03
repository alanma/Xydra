package com.sonicmetrics.core.shared.query;

import java.io.Serializable;

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
public class BuiltinKeyValueConstraint extends KeyValueConstraint implements Serializable {

	private static final long serialVersionUID = 1L;

	public static BuiltinKeyValueConstraint keyValue(ISonicEvent.FilterProperty key, String value) {
		XyAssert.validateNotNull(key, "key");
		return new BuiltinKeyValueConstraint(key, value);
	}

	private BuiltinKeyValueConstraint(FilterProperty key, String value) {
		super(key.name(), value);
		this.keyEnum = key;
	}

	public FilterProperty getKeyEnum() {
		return this.keyEnum;
	}

	private final FilterProperty keyEnum;

}
