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
 * TODO Indexing is rather naive at the moment...
 * 
 * TODO Implement deIndex methods
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
	
	private static void indexStringArray(IMapSetIndex<String,XAddress> index, XAddress address,
	        String[] values) {
		for(String str : values) {
			indexString(index, address, str);
		}
	}
	
	private static void indexLongArray(IMapSetIndex<String,XAddress> index, XAddress address,
	        long[] values) {
		for(long l : values) {
			indexLong(index, address, l);
		}
	}
	
	private static void indexLong(IMapSetIndex<String,XAddress> index, XAddress address, Long value) {
		index.index("" + value, address);
	}
	
	private static void indexIntegerArray(IMapSetIndex<String,XAddress> index, XAddress address,
	        int[] values) {
		for(Integer i : values) {
			indexInteger(index, address, i);
		}
	}
	
	private static void indexInteger(IMapSetIndex<String,XAddress> index, XAddress address,
	        int value) {
		index.index("" + value, address);
	}
	
	private static void indexIdArray(IMapSetIndex<String,XAddress> index, XAddress address,
	        XID[] values) {
		for(XID id : values) {
			indexId(index, address, id);
		}
	}
	
	private static void indexDoubleArray(IMapSetIndex<String,XAddress> index, XAddress address,
	        double[] values) {
		for(Double d : values) {
			indexDouble(index, address, d);
		}
	}
	
	private static void indexDouble(IMapSetIndex<String,XAddress> index, XAddress address,
	        double value) {
		index.index("" + value, address);
	}
	
	private static void indexByteArray(IMapSetIndex<String,XAddress> index, XAddress address,
	        byte[] values) {
		for(Byte b : values) {
			indexByte(index, address, b);
		}
	}
	
	private static void indexByte(IMapSetIndex<String,XAddress> index, XAddress address, byte value) {
		index.index("" + value, address);
	}
	
	private static void indexBooleanArray(IMapSetIndex<String,XAddress> index, XAddress address,
	        boolean[] values) {
		for(Boolean b : values) {
			indexBoolean(index, address, b);
		}
	}
	
	private static void indexBoolean(IMapSetIndex<String,XAddress> index, XAddress address,
	        boolean value) {
		index.index("" + value, address);
	}
	
	public static void indexStringSetValue(IMapSetIndex<String,XAddress> index, XAddress address,
	        XStringSetValue value) {
		for(String valueStr : value.contents()) {
			indexString(index, address, valueStr);
		}
	}
	
	private static void indexString(IMapSetIndex<String,XAddress> index, XAddress address,
	        String valueStr) {
		// TODO How to deal with punctuation marks etc? (is this the right
		// regex?)
		String[] words = valueStr.split("\\W]");
		
		for(String word : words) {
			index.index(word.toLowerCase(), address);
		}
	}
	
	private static void indexId(IMapSetIndex<String,XAddress> index, XAddress address, XID id) {
		index.index("" + id.toString(), address);
	}
	
	public static void indexAddressArray(IMapSetIndex<String,XAddress> index, XAddress address,
	        XAddress[] values) {
		for(XAddress adr : values) {
			indexAddress(index, address, adr);
		}
		
	}
	
	public static void indexAddress(IMapSetIndex<String,XAddress> index, XAddress address,
	        XAddress value) {
		/*
		 * TODO Maybe index the single IDs too?
		 */
		index.index("" + value.toString(), address);
		
	}
	
}
