package org.xydra.index;

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


/*
 * TODO Document
 */

public class XValueIndexer {
	
	public static void indexValue(IMapSetIndex<String,XAddress> index, XAddress address,
	        XValue value) {
		switch(value.getType()) {
		case Address:
			indexAddress(index, address, (XAddress)value);
			break;
		case AddressList: // Fall-Through
		case AddressSet: // Fall-Through
		case AddressSortedSet:
			indexAddressArray(index, address, ((XAddressSortedSetValue)value).contents());
			break;
		case Boolean:
			indexBoolean(index, address, ((XBooleanValue)value).contents());
			break;
		case BooleanList:
			indexBooleanArray(index, address, ((XBooleanListValue)value).contents());
			break;
		case ByteList:
			indexByteArray(index, address, ((XByteListValue)value).contents());
			break;
		case Double:
			indexDouble(index, address, ((XDoubleValue)value).contents());
			break;
		case DoubleList:
			indexDoubleArray(index, address, ((XDoubleListValue)value).contents());
			break;
		case Id:
			indexId(index, address, (XID)value);
			break;
		case IdList:
			indexIdArray(index, address, ((XIDListValue)value).contents());
			break;
		case IdSet:
			indexIdArray(index, address, ((XIDSetValue)value).contents());
			break;
		case IdSortedSet:
			indexIdArray(index, address, ((XIDSortedSetValue)value).contents());
			break;
		case Integer:
			indexInteger(index, address, ((XIntegerValue)value).contents());
			break;
		case IntegerList:
			indexIntegerArray(index, address, ((XIntegerListValue)value).contents());
			break;
		case Long:
			indexLong(index, address, ((XLongValue)value).contents());
			break;
		case LongList:
			indexLongArray(index, address, ((XLongListValue)value).contents());
			break;
		case String:
			indexString(index, address, ((XStringValue)value).contents());
			break;
		case StringList:
			indexStringArray(index, address, ((XStringListValue)value).contents());
			break;
		case StringSet:
			indexStringArray(index, address, ((XStringSetValue)value).contents());
			break;
		}
	}
	
	public static void deIndexValue(IMapSetIndex<String,XAddress> index, XAddress address,
	        XValue value) {
		switch(value.getType()) {
		case Address:
			deIndexAddress(index, address, (XAddress)value);
			break;
		case AddressList: // Fall-Through
		case AddressSet: // Fall-Through
		case AddressSortedSet:
			deIndexAddressArray(index, address, ((XAddressSortedSetValue)value).contents());
			break;
		case Boolean:
			deIndexBoolean(index, address, ((XBooleanValue)value).contents());
			break;
		case BooleanList:
			deIndexBooleanArray(index, address, ((XBooleanListValue)value).contents());
			break;
		case ByteList:
			deIndexByteArray(index, address, ((XByteListValue)value).contents());
			break;
		case Double:
			deIndexDouble(index, address, ((XDoubleValue)value).contents());
			break;
		case DoubleList:
			deIndexDoubleArray(index, address, ((XDoubleListValue)value).contents());
			break;
		case Id:
			deIndexId(index, address, (XID)value);
			break;
		case IdList:
			deIndexIdArray(index, address, ((XIDListValue)value).contents());
			break;
		case IdSet:
			deIndexIdArray(index, address, ((XIDSetValue)value).contents());
			break;
		case IdSortedSet:
			deIndexIdArray(index, address, ((XIDSortedSetValue)value).contents());
			break;
		case Integer:
			deIndexInteger(index, address, ((XIntegerValue)value).contents());
			break;
		case IntegerList:
			deIndexIntegerArray(index, address, ((XIntegerListValue)value).contents());
			break;
		case Long:
			deIndexLong(index, address, ((XLongValue)value).contents());
			break;
		case LongList:
			deIndexLongArray(index, address, ((XLongListValue)value).contents());
			break;
		case String:
			deIndexString(index, address, ((XStringValue)value).contents());
			break;
		case StringList:
			deIndexStringArray(index, address, ((XStringListValue)value).contents());
			break;
		case StringSet:
			deIndexStringArray(index, address, ((XStringSetValue)value).contents());
			break;
		}
	}
	
	public static void indexStringArray(IMapSetIndex<String,XAddress> index, XAddress address,
	        String[] values) {
		for(String str : values) {
			indexString(index, address, str);
		}
	}
	
	public static void deIndexStringArray(IMapSetIndex<String,XAddress> index, XAddress address,
	        String[] values) {
		for(String str : values) {
			deIndexString(index, address, str);
		}
	}
	
	public static void indexLongArray(IMapSetIndex<String,XAddress> index, XAddress address,
	        long[] values) {
		for(long l : values) {
			indexLong(index, address, l);
		}
	}
	
	public static void deIndexLongArray(IMapSetIndex<String,XAddress> index, XAddress address,
	        long[] values) {
		for(long l : values) {
			deIndexLong(index, address, l);
		}
	}
	
	public static void indexLong(IMapSetIndex<String,XAddress> index, XAddress address, Long value) {
		index.index(getLongIndexString(value), address);
	}
	
	public static void deIndexLong(IMapSetIndex<String,XAddress> index, XAddress address, Long value) {
		index.deIndex(getLongIndexString(value), address);
	}
	
	public static void indexIntegerArray(IMapSetIndex<String,XAddress> index, XAddress address,
	        int[] values) {
		for(Integer i : values) {
			indexInteger(index, address, i);
		}
	}
	
	public static void deIndexIntegerArray(IMapSetIndex<String,XAddress> index, XAddress address,
	        int[] values) {
		for(Integer i : values) {
			deIndexInteger(index, address, i);
		}
	}
	
	public static void indexInteger(IMapSetIndex<String,XAddress> index, XAddress address, int value) {
		index.index(getIntegerIndexString(value), address);
	}
	
	public static void deIndexInteger(IMapSetIndex<String,XAddress> index, XAddress address,
	        int value) {
		index.deIndex(getIntegerIndexString(value), address);
	}
	
	public static void indexIdArray(IMapSetIndex<String,XAddress> index, XAddress address,
	        XID[] values) {
		for(XID id : values) {
			indexId(index, address, id);
		}
	}
	
	public static void deIndexIdArray(IMapSetIndex<String,XAddress> index, XAddress address,
	        XID[] values) {
		for(XID id : values) {
			deIndexId(index, address, id);
		}
	}
	
	public static void indexDoubleArray(IMapSetIndex<String,XAddress> index, XAddress address,
	        double[] values) {
		for(Double d : values) {
			indexDouble(index, address, d);
		}
	}
	
	public static void deIndexDoubleArray(IMapSetIndex<String,XAddress> index, XAddress address,
	        double[] values) {
		for(Double d : values) {
			deIndexDouble(index, address, d);
		}
	}
	
	public static void indexDouble(IMapSetIndex<String,XAddress> index, XAddress address,
	        double value) {
		index.index(getDoubleIndexString(value), address);
	}
	
	public static void deIndexDouble(IMapSetIndex<String,XAddress> index, XAddress address,
	        double value) {
		index.index(getDoubleIndexString(value), address);
	}
	
	public static void indexByteArray(IMapSetIndex<String,XAddress> index, XAddress address,
	        byte[] values) {
		for(Byte b : values) {
			indexByte(index, address, b);
		}
	}
	
	public static void deIndexByteArray(IMapSetIndex<String,XAddress> index, XAddress address,
	        byte[] values) {
		for(Byte b : values) {
			deIndexByte(index, address, b);
		}
	}
	
	public static void indexByte(IMapSetIndex<String,XAddress> index, XAddress address, byte value) {
		index.index(getByteIndexString(value), address);
	}
	
	public static void deIndexByte(IMapSetIndex<String,XAddress> index, XAddress address, byte value) {
		index.deIndex(getByteIndexString(value), address);
	}
	
	public static void indexBooleanArray(IMapSetIndex<String,XAddress> index, XAddress address,
	        boolean[] values) {
		for(Boolean b : values) {
			indexBoolean(index, address, b);
		}
	}
	
	public static void deIndexBooleanArray(IMapSetIndex<String,XAddress> index, XAddress address,
	        boolean[] values) {
		for(Boolean b : values) {
			deIndexBoolean(index, address, b);
		}
	}
	
	public static void indexBoolean(IMapSetIndex<String,XAddress> index, XAddress address,
	        boolean value) {
		index.index(getBooleanIndexString(value), address);
	}
	
	public static void deIndexBoolean(IMapSetIndex<String,XAddress> index, XAddress address,
	        boolean value) {
		index.deIndex(getBooleanIndexString(value), address);
	}
	
	public static void indexString(IMapSetIndex<String,XAddress> index, XAddress address,
	        String value) {
		for(String word : getStringIndexStrings(value)) {
			index.index(word, address);
		}
	}
	
	public static void deIndexString(IMapSetIndex<String,XAddress> index, XAddress address,
	        String value) {
		for(String word : getStringIndexStrings(value)) {
			index.deIndex(word, address);
		}
	}
	
	public static void indexId(IMapSetIndex<String,XAddress> index, XAddress address, XID id) {
		index.index(getIdIndexString(id), address);
	}
	
	public static void deIndexId(IMapSetIndex<String,XAddress> index, XAddress address, XID id) {
		index.deIndex(getIdIndexString(id), address);
	}
	
	public static void indexAddressArray(IMapSetIndex<String,XAddress> index, XAddress address,
	        XAddress[] values) {
		for(XAddress adr : values) {
			indexAddress(index, address, adr);
		}
	}
	
	public static void deIndexAddressArray(IMapSetIndex<String,XAddress> index, XAddress address,
	        XAddress[] values) {
		for(XAddress adr : values) {
			deIndexAddress(index, address, adr);
		}
	}
	
	public static void indexAddress(IMapSetIndex<String,XAddress> index, XAddress address,
	        XAddress value) {
		index.index(getAddressIndexString(value), address);
	}
	
	public static void deIndexAddress(IMapSetIndex<String,XAddress> index, XAddress address,
	        XAddress value) {
		index.deIndex(getAddressIndexString(value), address);
	}
	
	// ---- Methods returning the index strings ----
	
	/*
	 * TODO Indexing is rather naive at the moment...
	 */

	public static String getLongIndexString(Long value) {
		return "" + value;
	}
	
	public static String getIntegerIndexString(Integer value) {
		return "" + value;
	}
	
	public static String getDoubleIndexString(Double value) {
		return "" + value;
	}
	
	public static String getByteIndexString(Byte value) {
		return "" + value;
	}
	
	public static String getBooleanIndexString(Boolean value) {
		return "" + value;
	}
	
	public static String[] getStringIndexStrings(String value) {
		// TODO How to deal with punctuation marks etc? (is this the right
		// regex?)
		String[] words = value.split("\\W]");
		String[] indexes = new String[words.length];
		
		for(int i = 0; i < indexes.length; i++) {
			indexes[i] = words[i].toLowerCase();
		}
		
		return indexes;
	}
	
	public static String getIdIndexString(XID value) {
		return "" + value.toString();
	}
	
	public static String getAddressIndexString(XAddress value) {
		/*
		 * TODO Maybe index the single IDs too?
		 */
		return "" + value.toString();
	}
}
