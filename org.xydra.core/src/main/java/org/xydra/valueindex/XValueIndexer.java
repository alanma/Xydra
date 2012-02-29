package org.xydra.valueindex;

import java.util.ArrayList;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.value.XAddressListValue;
import org.xydra.base.value.XAddressSetValue;
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


/*
 * TODO Document
 */

/*
 * FIXME Deal with "null" values!! (make it consistent)
 * 
 * TODO Check that given addresses are field-addresses!
 */

public abstract class XValueIndexer {
	private ValueIndex index;
	
	public XValueIndexer(ValueIndex index) {
		this.index = index;
	}
	
	public ValueIndex getIndex() {
		return this.index;
	}
	
	public void indexValue(XAddress fieldAddress, XValue value) {
		if(value == null) {
			indexString(fieldAddress, value, getIndexStringForNull());
		} else {
			switch(value.getType()) {
			case Address:
				indexAddress(fieldAddress, value, (XAddress)value);
				break;
			case AddressList:
				indexAddressArray(fieldAddress, value, ((XAddressListValue)value).contents());
				break;
			
			case AddressSet:
				indexAddressArray(fieldAddress, value, ((XAddressSetValue)value).contents());
				break;
			case AddressSortedSet:
				indexAddressArray(fieldAddress, value, ((XAddressSortedSetValue)value).contents());
				break;
			case Boolean:
				indexBoolean(fieldAddress, value, ((XBooleanValue)value).contents());
				break;
			case BooleanList:
				indexBooleanArray(fieldAddress, value, ((XBooleanListValue)value).contents());
				break;
			case ByteList:
				indexByteArray(fieldAddress, value, ((XByteListValue)value).contents());
				break;
			case Double:
				indexDouble(fieldAddress, value, ((XDoubleValue)value).contents());
				break;
			case DoubleList:
				indexDoubleArray(fieldAddress, value, ((XDoubleListValue)value).contents());
				break;
			case Id:
				indexId(fieldAddress, value, (XID)value);
				break;
			case IdList:
				indexIdArray(fieldAddress, value, ((XIDListValue)value).contents());
				break;
			case IdSet:
				indexIdArray(fieldAddress, value, ((XIDSetValue)value).contents());
				break;
			case IdSortedSet:
				indexIdArray(fieldAddress, value, ((XIDSortedSetValue)value).contents());
				break;
			case Integer:
				indexInteger(fieldAddress, value, ((XIntegerValue)value).contents());
				break;
			case IntegerList:
				indexIntegerArray(fieldAddress, value, ((XIntegerListValue)value).contents());
				break;
			case Long:
				indexLong(fieldAddress, value, ((XLongValue)value).contents());
				break;
			case LongList:
				indexLongArray(fieldAddress, value, ((XLongListValue)value).contents());
				break;
			case String:
				indexString(fieldAddress, value, ((XStringValue)value).contents());
				break;
			case StringList:
				indexStringArray(fieldAddress, value, ((XStringListValue)value).contents());
				break;
			case StringSet:
				indexStringArray(fieldAddress, value, ((XStringSetValue)value).contents());
				break;
			}
		}
	}
	
	public List<String> getIndexStrings(XValue value) {
		ArrayList<String> list = new ArrayList<String>();
		
		if(value == null) {
			list.add("null");
			return list;
		}
		
		switch(value.getType()) {
		case Address:
			list.add(getAddressIndexString((XAddress)value));
			break;
		
		case AddressList:
			for(XAddress addr : ((XAddressListValue)value).contents()) {
				list.add(getAddressIndexString(addr));
			}
			break;
		
		case AddressSet:
			for(XAddress addr : ((XAddressSetValue)value).contents()) {
				list.add(getAddressIndexString(addr));
			}
			break;
		
		case AddressSortedSet:
			for(XAddress addr : ((XAddressSortedSetValue)value).contents()) {
				list.add(getAddressIndexString(addr));
			}
			break;
		
		case Boolean:
			list.add(getBooleanIndexString(((XBooleanValue)value).contents()));
			break;
		
		case BooleanList:
			for(Boolean bool : ((XBooleanListValue)value).contents()) {
				list.add(getBooleanIndexString(bool));
			}
			break;
		
		case ByteList:
			for(Byte b : ((XByteListValue)value).contents()) {
				list.add(getByteIndexString(b));
			}
			break;
		
		case Double:
			list.add(getDoubleIndexString(((XDoubleValue)value).contents()));
			break;
		
		case DoubleList:
			for(Double d : ((XDoubleListValue)value).contents()) {
				list.add(getDoubleIndexString(d));
			}
			break;
		
		case Id:
			list.add(getIdIndexString((XID)value));
			break;
		
		case IdList:
			for(XID id : ((XIDListValue)value).contents()) {
				list.add(getIdIndexString(id));
			}
			break;
		case IdSet:
			for(XID id : ((XIDSetValue)value).contents()) {
				list.add(getIdIndexString(id));
			}
			break;
		
		case IdSortedSet:
			for(XID id : ((XIDSortedSetValue)value).contents()) {
				list.add(getIdIndexString(id));
			}
			break;
		
		case Integer:
			list.add(getIntegerIndexString(((XIntegerValue)value).contents()));
			break;
		
		case IntegerList:
			for(Integer i : ((XIntegerListValue)value).contents()) {
				list.add(getIntegerIndexString(i));
			}
			break;
		
		case Long:
			list.add(getLongIndexString(((XLongValue)value).contents()));
			break;
		
		case LongList:
			for(Long l : ((XLongListValue)value).contents()) {
				list.add(getLongIndexString(l));
			}
			break;
		
		case String:
			for(String s : getStringIndexStrings(((XStringValue)value).contents())) {
				list.add(s);
			}
			break;
		
		case StringList:
			for(String s1 : ((XStringListValue)value).contents()) {
				for(String s2 : getStringIndexStrings(s1)) {
					list.add(s2);
				}
			}
			break;
		
		case StringSet:
			for(String s1 : ((XStringSetValue)value).contents()) {
				for(String s2 : getStringIndexStrings(s1)) {
					list.add(s2);
				}
			}
			break;
		
		}
		
		return list;
	}
	
	public void deIndexValue(XAddress fieldAddress, XValue value) {
		if(value == null) {
			deIndexString(fieldAddress, value, getIndexStringForNull());
		} else {
			switch(value.getType()) {
			case Address:
				deIndexAddress(fieldAddress, value, (XAddress)value);
				break;
			case AddressList:
				deIndexAddressArray(fieldAddress, value, ((XAddressListValue)value).contents());
				break;
			case AddressSet:
				deIndexAddressArray(fieldAddress, value, ((XAddressSetValue)value).contents());
				break;
			case AddressSortedSet:
				deIndexAddressArray(fieldAddress, value, ((XAddressSortedSetValue)value).contents());
				break;
			case Boolean:
				deIndexBoolean(fieldAddress, value, ((XBooleanValue)value).contents());
				break;
			case BooleanList:
				deIndexBooleanArray(fieldAddress, value, ((XBooleanListValue)value).contents());
				break;
			case ByteList:
				deIndexByteArray(fieldAddress, value, ((XByteListValue)value).contents());
				break;
			case Double:
				deIndexDouble(fieldAddress, value, ((XDoubleValue)value).contents());
				break;
			case DoubleList:
				deIndexDoubleArray(fieldAddress, value, ((XDoubleListValue)value).contents());
				break;
			case Id:
				deIndexId(fieldAddress, value, (XID)value);
				break;
			case IdList:
				deIndexIdArray(fieldAddress, value, ((XIDListValue)value).contents());
				break;
			case IdSet:
				deIndexIdArray(fieldAddress, value, ((XIDSetValue)value).contents());
				break;
			case IdSortedSet:
				deIndexIdArray(fieldAddress, value, ((XIDSortedSetValue)value).contents());
				break;
			case Integer:
				deIndexInteger(fieldAddress, value, ((XIntegerValue)value).contents());
				break;
			case IntegerList:
				deIndexIntegerArray(fieldAddress, value, ((XIntegerListValue)value).contents());
				break;
			case Long:
				deIndexLong(fieldAddress, value, ((XLongValue)value).contents());
				break;
			case LongList:
				deIndexLongArray(fieldAddress, value, ((XLongListValue)value).contents());
				break;
			case String:
				deIndexString(fieldAddress, value, ((XStringValue)value).contents());
				break;
			case StringList:
				deIndexStringArray(fieldAddress, value, ((XStringListValue)value).contents());
				break;
			case StringSet:
				deIndexStringArray(fieldAddress, value, ((XStringSetValue)value).contents());
				break;
			}
		}
	}
	
	public void indexStringArray(XAddress fieldAddress, XValue value, String[] strings) {
		for(String str : strings) {
			indexString(fieldAddress, value, str);
		}
	}
	
	public void deIndexStringArray(XAddress fieldAddress, XValue value, String[] strings) {
		for(String str : strings) {
			deIndexString(fieldAddress, value, str);
		}
	}
	
	public void indexLongArray(XAddress fieldAddress, XValue value, long[] longs) {
		for(long l : longs) {
			indexLong(fieldAddress, value, l);
		}
	}
	
	public void deIndexLongArray(XAddress fieldAddress, XValue value, long[] longs) {
		for(long l : longs) {
			deIndexLong(fieldAddress, value, l);
		}
	}
	
	public void indexLong(XAddress fieldAddress, XValue value, Long l) {
		String key = getLongIndexString(l);
		this.index.index(key, fieldAddress, value);
	}
	
	public void deIndexLong(XAddress fieldAddress, XValue value, Long l) {
		String key = getLongIndexString(l);
		this.index.deIndex(key, fieldAddress, value);
	}
	
	public void indexIntegerArray(XAddress fieldAddress, XValue value, int[] integers) {
		for(Integer i : integers) {
			indexInteger(fieldAddress, value, i);
		}
	}
	
	public void deIndexIntegerArray(XAddress fieldAddress, XValue value, int[] integers) {
		for(Integer i : integers) {
			deIndexInteger(fieldAddress, value, i);
		}
	}
	
	public void indexInteger(XAddress fieldAddress, XValue value, int integer) {
		String key = getIntegerIndexString(integer);
		this.index.index(key, fieldAddress, value);
	}
	
	public void deIndexInteger(XAddress fieldAddress, XValue value, int integer) {
		String key = getIntegerIndexString(integer);
		this.index.deIndex(key, fieldAddress, value);
	}
	
	public void indexIdArray(XAddress fieldAddress, XValue value, XID[] ids) {
		for(XID id : ids) {
			indexId(fieldAddress, value, id);
		}
	}
	
	public void deIndexIdArray(XAddress fieldAddress, XValue value, XID[] ids) {
		for(XID id : ids) {
			deIndexId(fieldAddress, value, id);
		}
	}
	
	public void indexDoubleArray(XAddress fieldAddress, XValue value, double[] doubles) {
		for(Double d : doubles) {
			indexDouble(fieldAddress, value, d);
		}
	}
	
	public void deIndexDoubleArray(XAddress fieldAddress, XValue value, double[] doubles) {
		for(Double d : doubles) {
			deIndexDouble(fieldAddress, value, d);
		}
	}
	
	public void indexDouble(XAddress fieldAddress, XValue value, double d) {
		String key = getDoubleIndexString(d);
		
		this.index.index(key, fieldAddress, value);
	}
	
	public void deIndexDouble(XAddress fieldAddress, XValue value, double d) {
		String key = getDoubleIndexString(d);
		this.index.deIndex(key, fieldAddress, value);
	}
	
	public void indexByteArray(XAddress fieldAddress, XValue value, byte[] bytes) {
		for(Byte b : bytes) {
			indexByte(fieldAddress, value, b);
		}
	}
	
	public void deIndexByteArray(XAddress fieldAddress, XValue value, byte[] bytes) {
		for(Byte b : bytes) {
			deIndexByte(fieldAddress, value, b);
		}
	}
	
	public void indexByte(XAddress fieldAddress, XValue value, byte b) {
		String key = getByteIndexString(b);
		this.index.index(key, fieldAddress, value);
	}
	
	public void deIndexByte(XAddress fieldAddress, XValue value, byte b) {
		String key = getByteIndexString(b);
		this.index.deIndex(key, fieldAddress, value);
	}
	
	public void indexBooleanArray(XAddress fieldAddress, XValue value, boolean[] bools) {
		for(Boolean b : bools) {
			indexBoolean(fieldAddress, value, b);
		}
	}
	
	public void deIndexBooleanArray(XAddress fieldAddress, XValue value, boolean[] bools) {
		for(Boolean b : bools) {
			deIndexBoolean(fieldAddress, value, b);
		}
	}
	
	public void indexBoolean(XAddress fieldAddress, XValue value, boolean bool) {
		String key = getBooleanIndexString(bool);
		this.index.index(key, fieldAddress, value);
	}
	
	public void deIndexBoolean(XAddress fieldAddress, XValue value, boolean bool) {
		String key = getBooleanIndexString(bool);
		this.index.deIndex(key, fieldAddress, value);
	}
	
	public void indexString(XAddress fieldAddress, XValue value, String string) {
		for(String key : getStringIndexStrings(string)) {
			this.index.index(key, fieldAddress, value);
		}
	}
	
	public void deIndexString(XAddress fieldAddress, XValue value, String s) {
		for(String key : getStringIndexStrings(s)) {
			this.index.deIndex(key, fieldAddress, value);
		}
	}
	
	public void indexId(XAddress fieldAddress, XValue value, XID id) {
		String key = getIdIndexString(id);
		this.index.index(key, fieldAddress, value);
	}
	
	public void deIndexId(XAddress fieldAddress, XValue value, XID id) {
		String key = getIdIndexString(id);
		this.index.deIndex(key, fieldAddress, value);
	}
	
	public void indexAddressArray(XAddress fieldAddress, XValue value, XAddress[] addresses) {
		for(XAddress adr : addresses) {
			indexAddress(fieldAddress, value, adr);
		}
	}
	
	public void deIndexAddressArray(XAddress fieldAddress, XValue value, XAddress[] addresses) {
		for(XAddress adr : addresses) {
			deIndexAddress(fieldAddress, value, adr);
		}
	}
	
	public void indexAddress(XAddress fieldAddress, XValue value, XAddress address) {
		String key = getAddressIndexString(address);
		this.index.index(key, fieldAddress, value);
	}
	
	public void deIndexAddress(XAddress fieldAddress, XValue value, XAddress address) {
		String key = getAddressIndexString(address);
		this.index.deIndex(key, fieldAddress, value);
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
	
	public abstract String getIndexStringForNull();
}
