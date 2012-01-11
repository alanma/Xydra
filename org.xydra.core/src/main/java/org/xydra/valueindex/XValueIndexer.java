package org.xydra.valueindex;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.value.XAddressSortedSetValue;
import org.xydra.base.value.XBooleanListValue;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XByteListValue;
import org.xydra.base.value.XDoubleListValue;
import org.xydra.base.value.XDoubleValue;
import org.xydra.base.value.XIDListValue;
import org.xydra.base.value.XIDSetValue;
import org.xydra.base.value.XIDSortedSetValue;
import org.xydra.base.value.XIntegerListValue;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XLongListValue;
import org.xydra.base.value.XLongValue;
import org.xydra.base.value.XStringListValue;
import org.xydra.base.value.XStringSetValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XValue;
import org.xydra.index.IMapSetIndex;


/*
 * TODO Document
 */

public abstract class XValueIndexer {
	private IMapSetIndex<String,XAddress> index;
	
	public XValueIndexer(IMapSetIndex<String,XAddress> index) {
		this.index = index;
	}
	
	public void indexValue(XAddress address, XValue value) {
		switch(value.getType()) {
		case Address:
			indexAddress(address, (XAddress)value);
			break;
		case AddressList: // Fall-Through
		case AddressSet: // Fall-Through
		case AddressSortedSet:
			indexAddressArray(address, ((XAddressSortedSetValue)value).contents());
			break;
		case Boolean:
			indexBoolean(address, ((XBooleanValue)value).contents());
			break;
		case BooleanList:
			indexBooleanArray(address, ((XBooleanListValue)value).contents());
			break;
		case ByteList:
			indexByteArray(address, ((XByteListValue)value).contents());
			break;
		case Double:
			indexDouble(address, ((XDoubleValue)value).contents());
			break;
		case DoubleList:
			indexDoubleArray(address, ((XDoubleListValue)value).contents());
			break;
		case Id:
			indexId(address, (XID)value);
			break;
		case IdList:
			indexIdArray(address, ((XIDListValue)value).contents());
			break;
		case IdSet:
			indexIdArray(address, ((XIDSetValue)value).contents());
			break;
		case IdSortedSet:
			indexIdArray(address, ((XIDSortedSetValue)value).contents());
			break;
		case Integer:
			indexInteger(address, ((XIntegerValue)value).contents());
			break;
		case IntegerList:
			indexIntegerArray(address, ((XIntegerListValue)value).contents());
			break;
		case Long:
			indexLong(address, ((XLongValue)value).contents());
			break;
		case LongList:
			indexLongArray(address, ((XLongListValue)value).contents());
			break;
		case String:
			indexString(address, ((XStringValue)value).contents());
			break;
		case StringList:
			indexStringArray(address, ((XStringListValue)value).contents());
			break;
		case StringSet:
			indexStringArray(address, ((XStringSetValue)value).contents());
			break;
		}
	}
	
	public void deIndexValue(XAddress address, XValue value) {
		switch(value.getType()) {
		case Address:
			deIndexAddress(address, (XAddress)value);
			break;
		case AddressList: // Fall-Through
		case AddressSet: // Fall-Through
		case AddressSortedSet:
			deIndexAddressArray(address, ((XAddressSortedSetValue)value).contents());
			break;
		case Boolean:
			deIndexBoolean(address, ((XBooleanValue)value).contents());
			break;
		case BooleanList:
			deIndexBooleanArray(address, ((XBooleanListValue)value).contents());
			break;
		case ByteList:
			deIndexByteArray(address, ((XByteListValue)value).contents());
			break;
		case Double:
			deIndexDouble(address, ((XDoubleValue)value).contents());
			break;
		case DoubleList:
			deIndexDoubleArray(address, ((XDoubleListValue)value).contents());
			break;
		case Id:
			deIndexId(address, (XID)value);
			break;
		case IdList:
			deIndexIdArray(address, ((XIDListValue)value).contents());
			break;
		case IdSet:
			deIndexIdArray(address, ((XIDSetValue)value).contents());
			break;
		case IdSortedSet:
			deIndexIdArray(address, ((XIDSortedSetValue)value).contents());
			break;
		case Integer:
			deIndexInteger(address, ((XIntegerValue)value).contents());
			break;
		case IntegerList:
			deIndexIntegerArray(address, ((XIntegerListValue)value).contents());
			break;
		case Long:
			deIndexLong(address, ((XLongValue)value).contents());
			break;
		case LongList:
			deIndexLongArray(address, ((XLongListValue)value).contents());
			break;
		case String:
			deIndexString(address, ((XStringValue)value).contents());
			break;
		case StringList:
			deIndexStringArray(address, ((XStringListValue)value).contents());
			break;
		case StringSet:
			deIndexStringArray(address, ((XStringSetValue)value).contents());
			break;
		}
	}
	
	public void indexStringArray(XAddress address, String[] values) {
		for(String str : values) {
			indexString(address, str);
		}
	}
	
	public void deIndexStringArray(XAddress address, String[] values) {
		for(String str : values) {
			deIndexString(address, str);
		}
	}
	
	public void indexLongArray(XAddress address, long[] values) {
		for(long l : values) {
			indexLong(address, l);
		}
	}
	
	public void deIndexLongArray(XAddress address, long[] values) {
		for(long l : values) {
			deIndexLong(address, l);
		}
	}
	
	public void indexLong(XAddress address, Long value) {
		this.index.index(getLongIndexString(value), address);
	}
	
	public void deIndexLong(XAddress address, Long value) {
		this.index.deIndex(getLongIndexString(value), address);
	}
	
	public void indexIntegerArray(XAddress address, int[] values) {
		for(Integer i : values) {
			indexInteger(address, i);
		}
	}
	
	public void deIndexIntegerArray(XAddress address, int[] values) {
		for(Integer i : values) {
			deIndexInteger(address, i);
		}
	}
	
	public void indexInteger(XAddress address, int value) {
		this.index.index(getIntegerIndexString(value), address);
	}
	
	public void deIndexInteger(XAddress address, int value) {
		this.index.deIndex(getIntegerIndexString(value), address);
	}
	
	public void indexIdArray(XAddress address, XID[] values) {
		for(XID id : values) {
			indexId(address, id);
		}
	}
	
	public void deIndexIdArray(XAddress address, XID[] values) {
		for(XID id : values) {
			deIndexId(address, id);
		}
	}
	
	public void indexDoubleArray(XAddress address, double[] values) {
		for(Double d : values) {
			indexDouble(address, d);
		}
	}
	
	public void deIndexDoubleArray(XAddress address, double[] values) {
		for(Double d : values) {
			deIndexDouble(address, d);
		}
	}
	
	public void indexDouble(XAddress address, double value) {
		this.index.index(getDoubleIndexString(value), address);
	}
	
	public void deIndexDouble(XAddress address, double value) {
		this.index.index(getDoubleIndexString(value), address);
	}
	
	public void indexByteArray(XAddress address, byte[] values) {
		for(Byte b : values) {
			indexByte(address, b);
		}
	}
	
	public void deIndexByteArray(XAddress address, byte[] values) {
		for(Byte b : values) {
			deIndexByte(address, b);
		}
	}
	
	public void indexByte(XAddress address, byte value) {
		this.index.index(getByteIndexString(value), address);
	}
	
	public void deIndexByte(XAddress address, byte value) {
		this.index.deIndex(getByteIndexString(value), address);
	}
	
	public void indexBooleanArray(XAddress address, boolean[] values) {
		for(Boolean b : values) {
			indexBoolean(address, b);
		}
	}
	
	public void deIndexBooleanArray(XAddress address, boolean[] values) {
		for(Boolean b : values) {
			deIndexBoolean(address, b);
		}
	}
	
	public void indexBoolean(XAddress address, boolean value) {
		this.index.index(getBooleanIndexString(value), address);
	}
	
	public void deIndexBoolean(XAddress address, boolean value) {
		this.index.deIndex(getBooleanIndexString(value), address);
	}
	
	public void indexString(XAddress address, String value) {
		for(String word : getStringIndexStrings(value)) {
			this.index.index(word, address);
		}
	}
	
	public void deIndexString(XAddress address, String value) {
		for(String word : getStringIndexStrings(value)) {
			this.index.deIndex(word, address);
		}
	}
	
	public void indexId(XAddress address, XID id) {
		this.index.index(getIdIndexString(id), address);
	}
	
	public void deIndexId(XAddress address, XID id) {
		this.index.deIndex(getIdIndexString(id), address);
	}
	
	public void indexAddressArray(XAddress address, XAddress[] values) {
		for(XAddress adr : values) {
			indexAddress(address, adr);
		}
	}
	
	public void deIndexAddressArray(XAddress address, XAddress[] values) {
		for(XAddress adr : values) {
			deIndexAddress(address, adr);
		}
	}
	
	public void indexAddress(XAddress address, XAddress value) {
		this.index.index(getAddressIndexString(value), address);
	}
	
	public void deIndexAddress(XAddress address, XAddress value) {
		this.index.deIndex(getAddressIndexString(value), address);
	}
	
	// ---- Methods returning the index strings ----
	
	public abstract String getLongIndexString(Long value);
	
	public abstract String getIntegerIndexString(Integer value);
	
	public abstract String getDoubleIndexString(Double value);
	
	public abstract String getByteIndexString(Byte value);
	
	public abstract String getBooleanIndexString(Boolean value);
	
	public abstract String[] getStringIndexStrings(String value);
	
	public abstract String getIdIndexString(XID value);
	
	public abstract String getAddressIndexString(XAddress value);
}
