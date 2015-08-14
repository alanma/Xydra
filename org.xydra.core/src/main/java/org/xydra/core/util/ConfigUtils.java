package org.xydra.core.util;

import org.xydra.conf.ConfigException;
import org.xydra.conf.IConfig;

public class ConfigUtils {

	/**
	 * @param booleanString can be null
	 * @return true if string is not null and equals true (case ignored).
	 */
	public static boolean isTrue(final String booleanString) {
		return booleanString != null && (

		booleanString.equalsIgnoreCase("true")

		|| booleanString.equalsIgnoreCase("on")

		|| booleanString.equalsIgnoreCase("yes")

		);
	}

	/**
	 * @param conf
	 * @param key
	 * @return true if a value is set and expresses "true"
	 */
	public static boolean isTrue(final IConfig conf, final String key) {
		final Object o = conf.tryToGet(key);
		if (o == null) {
			return false;
		}
		if (o instanceof Boolean) {
			return (Boolean) o;
		}
		if (o instanceof String) {
			return isTrue((String) o);
		}
		throw new ConfigException("Expected null,String or boolean but found type " + o.getClass().getName());
	}

}
