package org.xydra.core.index.impl.memory;

import org.xydra.annotations.NeverNull;
import org.xydra.base.Base;
import org.xydra.base.BaseRuntime;
import org.xydra.base.XId;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XCollectionValue;
import org.xydra.base.value.XDoubleValue;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XLongValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XValue;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.sharedutils.XyAssert;


/**
 * Utility class for creating and managing indexes, like an index from email
 * addresses to users. Not to be confused with the basic xydra.index package.
 *
 * @author xamde
 */
public class IndexUtils {

	/**
	 * Convert non-collection XValue to a XId to be used as a object or model
	 * Id.
	 *
	 * Currently handles {@link XStringValue}, {@link XDoubleValue},
	 * {@link XIntegerValue}, {@link XBooleanValue}, {@link XLongValue},
	 * {@link XId}.
	 *
	 * @param value The value to transform into an {@link XId}.
	 * @return an XId parsed from an encoded XValue
	 */
	public static XId valueToXId(@NeverNull final XValue value) {
		XyAssert.xyAssert(value != null);
		assert value != null;
		String key;
		if(value instanceof XStringValue) {
			return stringToXId(((XStringValue)value).contents());
		} else if(value instanceof XDoubleValue) {
			key = "" + ((XDoubleValue)value).contents();
			key = "a" + key.replace('.', '-');
		} else if(value instanceof XIntegerValue) {
			key = "a" + ((XIntegerValue)value).contents();
		} else if(value instanceof XBooleanValue) {
			key = "" + ((XBooleanValue)value).contents();
		} else if(value instanceof XLongValue) {
			key = "a" + ((XLongValue)value).contents();
		} else if(value instanceof XId) {
			// trivial
			return (XId)value;
		} else {
			// collection types
			assert value instanceof XCollectionValue<?> : "Support for indexing type "
			        + value.getClass().getName() + " has not been implemented yet";
			throw new RuntimeException("Indexing collection types such as "
			        + value.getClass().getName() + " is not supported.");
		}
		final XId xid = BaseRuntime.getIDProvider().fromString(key);
		return xid;
	}

	/**
	 * Create a hash XId from given string
	 *
	 * @param s any string
	 * @return a valid XId
	 */
	public static XId stringToXId(final String s) {
		String key = "" + s.hashCode();
		if(key.startsWith("-")) {
			// like 'minus'
			key = "m" + key.substring(1);
		} else {
			// like 'plus'
			key = "p" + key;
		}
		return Base.toId(key);
	}

}
