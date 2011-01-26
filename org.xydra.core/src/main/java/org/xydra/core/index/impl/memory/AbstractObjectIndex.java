package org.xydra.core.index.impl.memory;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RequiresAppEngine;
import org.xydra.base.X;
import org.xydra.base.XID;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XCollectionValue;
import org.xydra.base.value.XDoubleValue;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XLongValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XValue;
import org.xydra.store.NamingUtils;


@RunsInAppEngine(true)
@RunsInGWT(true)
@RequiresAppEngine(false)
public abstract class AbstractObjectIndex {
	
	private static final String CLASSNAME = "org.xydra.core.index.impl.memory.AbstractObjectIndex";
	
	/**
	 * convert value to key
	 * 
	 * @param value
	 * @return an XID parsed from an encoded XValue
	 */
	public static XID valueToXID(XValue value) {
		String key;
		if(value instanceof XStringValue) {
			key = "" + ((XStringValue)value).contents().hashCode();
			if(key.startsWith("-")) {
				key = "m" + key.substring(1);
			} else {
				key = "p" + key;
			}
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
	
	protected XID actor = X.getIDProvider().fromString(CLASSNAME);
	protected XID fieldId;
	
	protected XWritableObject indexObject;
	
	/**
	 * @param fieldId
	 * @param indexObject see {@link NamingUtils#getIndexModelId(XID, String)}
	 *            to obtain a suitable XID for your index object
	 */
	public AbstractObjectIndex(XID fieldId, XWritableObject indexObject) {
		this.fieldId = fieldId;
		this.indexObject = indexObject;
	}
	
}
