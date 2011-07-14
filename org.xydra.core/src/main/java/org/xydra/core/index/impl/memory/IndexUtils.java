package org.xydra.core.index.impl.memory;

import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XCollectionValue;
import org.xydra.base.value.XDoubleValue;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XLongValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XValue;


/**
 * Utility class for creating and managing indexes, like an index from email
 * addresses to users. Not to be confused with the basic xydra.index package.
 * 
 * @author xamde
 */
public class IndexUtils {
	
	/**
	 * Convert non-collection XValue to a XID to be used as a object or model
	 * Id.
	 * 
	 * Currently handles {@link XStringValue}, {@link XDoubleValue},
	 * {@link XIntegerValue}, {@link XBooleanValue}, {@link XLongValue},
	 * {@link XID}.
	 * 
	 * @param value The value to transform into an {@link XID}. May never be
	 *            null.
	 * @return an XID parsed from an encoded XValue
	 */
	public static XID valueToXID(XValue value) {
		assert value != null;
		String key;
		if(value instanceof XStringValue) {
			return stringToXID(((XStringValue)value).contents());
		} else if(value instanceof XDoubleValue) {
			key = "" + ((XDoubleValue)value).contents();
			key = "a" + key.replace('.', '-');
		} else if(value instanceof XIntegerValue) {
			key = "a" + ((XIntegerValue)value).contents();
		} else if(value instanceof XBooleanValue) {
			key = "" + ((XBooleanValue)value).contents();
		} else if(value instanceof XLongValue) {
			key = "a" + ((XLongValue)value).contents();
		} else if(value instanceof XID) {
			// trivial
			return ((XID)value);
		} else {
			// collection types
			assert (value instanceof XCollectionValue<?>) : "Support for indexing type "
			        + value.getClass().getName() + " has not been implemented yet";
			throw new RuntimeException("Indexing collection types such as "
			        + value.getClass().getName() + " is not supported.");
		}
		XID xid = X.getIDProvider().fromString(key);
		return xid;
	}
	
	/**
	 * Create a hash XID from given string
	 * 
	 * @param s any string
	 * @return a valid XID
	 */
	public static XID stringToXID(String s) {
		String key = "" + s.hashCode();
		if(key.startsWith("-")) {
			// like 'minus'
			key = "m" + key.substring(1);
		} else {
			// like 'plus'
			key = "p" + key;
		}
		return XX.toId(key);
	}
	
}
