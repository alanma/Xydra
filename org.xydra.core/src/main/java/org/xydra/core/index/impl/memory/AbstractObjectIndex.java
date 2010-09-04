package org.xydra.core.index.impl.memory;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.X;
import org.xydra.core.model.XID;
import org.xydra.core.model.XObject;
import org.xydra.core.value.XBooleanValue;
import org.xydra.core.value.XCollectionValue;
import org.xydra.core.value.XDoubleValue;
import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XIntegerValue;
import org.xydra.core.value.XLongValue;
import org.xydra.core.value.XStringValue;
import org.xydra.core.value.XValue;


@RunsInAppEngine
@RunsInGWT
@RunsInJava
public abstract class AbstractObjectIndex {
	
	private static final String CLASSNAME = "org.xydra.core.index.impl.memory.AbstractObjectIndex";
	
	protected XID fieldID;
	protected XObject indexObject;
	protected XID actor = X.getIDProvider().fromString(CLASSNAME);
	
	/**
	 * @param fieldID
	 * @param indexObjectID, common practice is to use one with ID
	 *            {objectID}"-index-"{fieldID}
	 */
	public AbstractObjectIndex(XID fieldID, XObject indexObject) {
		this.fieldID = fieldID;
		this.indexObject = indexObject;
	}
	
	/**
	 * convert value to key
	 * 
	 * @param value
	 * @return
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
		} else if(value instanceof XIDValue) {
			// trivial
			return ((XIDValue)value).contents();
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
	
}
