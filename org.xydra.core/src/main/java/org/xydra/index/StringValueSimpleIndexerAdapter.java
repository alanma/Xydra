package org.xydra.index;

import org.xydra.base.XAddress;
import org.xydra.base.XID;


// TODO Document

public abstract class StringValueSimpleIndexerAdapter extends XValueIndexer {
	public StringValueSimpleIndexerAdapter(IMapSetIndex<String,XAddress> index) {
		super(index);
	}
	
	// ---- Methods returning the index strings ----
	
	/*
	 * TODO Indexing is rather naive at the moment...
	 */

	public String getLongIndexString(Long value) {
		return "" + value;
	}
	
	public String getIntegerIndexString(Integer value) {
		return "" + value;
	}
	
	public String getDoubleIndexString(Double value) {
		return "" + value;
	}
	
	public String getByteIndexString(Byte value) {
		return "" + value;
	}
	
	public String getBooleanIndexString(Boolean value) {
		return "" + value;
	}
	
	public String getIdIndexString(XID value) {
		return "" + value.toString();
	}
	
	public String getAddressIndexString(XAddress value) {
		/*
		 * TODO Maybe index the single IDs too?
		 */
		return "" + value.toString();
	}
}
