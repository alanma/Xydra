package org.xydra.valueindex;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.index.IMapSetIndex;


// TODO Document

public abstract class StringValueSimpleIndexerAdapter extends XValueIndexer {
	public StringValueSimpleIndexerAdapter(IMapSetIndex<String,AddressValueCounterTriple> index) {
		super(index);
	}
	
	// ---- Methods returning the index strings ----
	
	/*
	 * TODO Indexing is rather naive at the moment...
	 */

	@Override
	public String getLongIndexString(Long value) {
		return "" + value;
	}
	
	@Override
	public String getIntegerIndexString(Integer value) {
		return "" + value;
	}
	
	@Override
	public String getDoubleIndexString(Double value) {
		return "" + value;
	}
	
	@Override
	public String getByteIndexString(Byte value) {
		return "" + value;
	}
	
	@Override
	public String getBooleanIndexString(Boolean value) {
		return "" + value;
	}
	
	@Override
	public String getIdIndexString(XID value) {
		return "" + value.toString();
	}
	
	@Override
	public String getAddressIndexString(XAddress value) {
		/*
		 * TODO Maybe index the single IDs too?
		 */
		return "" + value.toString();
	}
}
