package org.xydra.valueindex;

import java.util.ArrayList;
import java.util.Iterator;
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
import org.xydra.index.IMapSetIndex;
import org.xydra.index.query.EqualsConstraint;


/*
 * TODO Document
 */

/*
 * FIXME Deal with "null" values!! (make it consistent)
 */

public abstract class XValueIndexer {
	private IMapSetIndex<String,ValueIndexEntry> index;
	
	public XValueIndexer(IMapSetIndex<String,ValueIndexEntry> index) {
		this.index = index;
	}
	
	public IMapSetIndex<String,ValueIndexEntry> getIndex() {
		return this.index;
	}
	
	public void indexValue(XAddress objectAddress, XValue value) {
		if(value == null) {
			// TODO handle null values (consistently in the whole indexer)
			indexString(objectAddress, value, "null");
		} else {
			switch(value.getType()) {
			case Address:
				indexAddress(objectAddress, value, (XAddress)value);
				break;
			case AddressList:
				indexAddressArray(objectAddress, value, ((XAddressListValue)value).contents());
				break;
			
			case AddressSet:
				indexAddressArray(objectAddress, value, ((XAddressSetValue)value).contents());
				break;
			case AddressSortedSet:
				indexAddressArray(objectAddress, value, ((XAddressSortedSetValue)value).contents());
				break;
			case Boolean:
				indexBoolean(objectAddress, value, ((XBooleanValue)value).contents());
				break;
			case BooleanList:
				indexBooleanArray(objectAddress, value, ((XBooleanListValue)value).contents());
				break;
			case ByteList:
				indexByteArray(objectAddress, value, ((XByteListValue)value).contents());
				break;
			case Double:
				indexDouble(objectAddress, value, ((XDoubleValue)value).contents());
				break;
			case DoubleList:
				indexDoubleArray(objectAddress, value, ((XDoubleListValue)value).contents());
				break;
			case Id:
				indexId(objectAddress, value, (XID)value);
				break;
			case IdList:
				indexIdArray(objectAddress, value, ((XIDListValue)value).contents());
				break;
			case IdSet:
				indexIdArray(objectAddress, value, ((XIDSetValue)value).contents());
				break;
			case IdSortedSet:
				indexIdArray(objectAddress, value, ((XIDSortedSetValue)value).contents());
				break;
			case Integer:
				indexInteger(objectAddress, value, ((XIntegerValue)value).contents());
				break;
			case IntegerList:
				indexIntegerArray(objectAddress, value, ((XIntegerListValue)value).contents());
				break;
			case Long:
				indexLong(objectAddress, value, ((XLongValue)value).contents());
				break;
			case LongList:
				indexLongArray(objectAddress, value, ((XLongListValue)value).contents());
				break;
			case String:
				indexString(objectAddress, value, ((XStringValue)value).contents());
				break;
			case StringList:
				indexStringArray(objectAddress, value, ((XStringListValue)value).contents());
				break;
			case StringSet:
				indexStringArray(objectAddress, value, ((XStringSetValue)value).contents());
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
	
	public void deIndexValue(XAddress address, XValue value) {
		if(value == null) {
			// TODO handle null values
		} else {
			switch(value.getType()) {
			case Address:
				deIndexAddress(address, value, (XAddress)value);
				break;
			case AddressList:
				deIndexAddressArray(address, value, ((XAddressListValue)value).contents());
				break;
			case AddressSet:
				deIndexAddressArray(address, value, ((XAddressSetValue)value).contents());
				break;
			case AddressSortedSet:
				deIndexAddressArray(address, value, ((XAddressSortedSetValue)value).contents());
				break;
			case Boolean:
				deIndexBoolean(address, value, ((XBooleanValue)value).contents());
				break;
			case BooleanList:
				deIndexBooleanArray(address, value, ((XBooleanListValue)value).contents());
				break;
			case ByteList:
				deIndexByteArray(address, value, ((XByteListValue)value).contents());
				break;
			case Double:
				deIndexDouble(address, value, ((XDoubleValue)value).contents());
				break;
			case DoubleList:
				deIndexDoubleArray(address, value, ((XDoubleListValue)value).contents());
				break;
			case Id:
				deIndexId(address, value, (XID)value);
				break;
			case IdList:
				deIndexIdArray(address, value, ((XIDListValue)value).contents());
				break;
			case IdSet:
				deIndexIdArray(address, value, ((XIDSetValue)value).contents());
				break;
			case IdSortedSet:
				deIndexIdArray(address, value, ((XIDSortedSetValue)value).contents());
				break;
			case Integer:
				deIndexInteger(address, value, ((XIntegerValue)value).contents());
				break;
			case IntegerList:
				deIndexIntegerArray(address, value, ((XIntegerListValue)value).contents());
				break;
			case Long:
				deIndexLong(address, value, ((XLongValue)value).contents());
				break;
			case LongList:
				deIndexLongArray(address, value, ((XLongListValue)value).contents());
				break;
			case String:
				deIndexString(address, value, ((XStringValue)value).contents());
				break;
			case StringList:
				deIndexStringArray(address, value, ((XStringListValue)value).contents());
				break;
			case StringSet:
				deIndexStringArray(address, value, ((XStringSetValue)value).contents());
				break;
			}
		}
	}
	
	public void indexStringArray(XAddress address, XValue value, String[] strings) {
		for(String str : strings) {
			indexString(address, value, str);
		}
	}
	
	public void deIndexStringArray(XAddress address, XValue value, String[] strings) {
		for(String str : strings) {
			deIndexString(address, value, str);
		}
	}
	
	public void indexLongArray(XAddress address, XValue value, long[] longs) {
		for(long l : longs) {
			indexLong(address, value, l);
		}
	}
	
	public void deIndexLongArray(XAddress address, XValue value, long[] longs) {
		for(long l : longs) {
			deIndexLong(address, value, l);
		}
	}
	
	public void indexLong(XAddress address, XValue value, Long l) {
		this.incrementIndexEntry(getLongIndexString(l), address, value);
	}
	
	public void deIndexLong(XAddress address, XValue value, Long l) {
		this.decrementIndexEntry(getLongIndexString(l), address, value);
	}
	
	public void indexIntegerArray(XAddress address, XValue value, int[] integers) {
		for(Integer i : integers) {
			indexInteger(address, value, i);
		}
	}
	
	public void deIndexIntegerArray(XAddress address, XValue value, int[] integers) {
		for(Integer i : integers) {
			deIndexInteger(address, value, i);
		}
	}
	
	public void indexInteger(XAddress address, XValue value, int integer) {
		this.incrementIndexEntry(getIntegerIndexString(integer), address, value);
	}
	
	public void deIndexInteger(XAddress address, XValue value, int integer) {
		this.decrementIndexEntry(getIntegerIndexString(integer), address, value);
	}
	
	public void indexIdArray(XAddress address, XValue value, XID[] ids) {
		for(XID id : ids) {
			indexId(address, value, id);
		}
	}
	
	public void deIndexIdArray(XAddress address, XValue value, XID[] ids) {
		for(XID id : ids) {
			deIndexId(address, value, id);
		}
	}
	
	public void indexDoubleArray(XAddress address, XValue value, double[] doubles) {
		for(Double d : doubles) {
			indexDouble(address, value, d);
		}
	}
	
	public void deIndexDoubleArray(XAddress address, XValue value, double[] doubles) {
		for(Double d : doubles) {
			deIndexDouble(address, value, d);
		}
	}
	
	public void indexDouble(XAddress address, XValue value, double d) {
		this.incrementIndexEntry(getDoubleIndexString(d), address, value);
	}
	
	public void deIndexDouble(XAddress address, XValue value, double d) {
		this.decrementIndexEntry(getDoubleIndexString(d), address, value);
	}
	
	public void indexByteArray(XAddress address, XValue value, byte[] bytes) {
		for(Byte b : bytes) {
			indexByte(address, value, b);
		}
	}
	
	public void deIndexByteArray(XAddress address, XValue value, byte[] bytes) {
		for(Byte b : bytes) {
			deIndexByte(address, value, b);
		}
	}
	
	public void indexByte(XAddress address, XValue value, byte b) {
		this.incrementIndexEntry(getByteIndexString(b), address, value);
	}
	
	public void deIndexByte(XAddress address, XValue value, byte b) {
		this.decrementIndexEntry(getByteIndexString(b), address, value);
	}
	
	public void indexBooleanArray(XAddress address, XValue value, boolean[] bools) {
		for(Boolean b : bools) {
			indexBoolean(address, value, b);
		}
	}
	
	public void deIndexBooleanArray(XAddress address, XValue value, boolean[] bools) {
		for(Boolean b : bools) {
			deIndexBoolean(address, value, b);
		}
	}
	
	public void indexBoolean(XAddress address, XValue value, boolean bool) {
		this.incrementIndexEntry(getBooleanIndexString(bool), address, value);
	}
	
	public void deIndexBoolean(XAddress address, XValue value, boolean bool) {
		this.decrementIndexEntry(getBooleanIndexString(bool), address, value);
	}
	
	public void indexString(XAddress address, XValue value, String string) {
		for(String word : getStringIndexStrings(string)) {
			this.incrementIndexEntry(word, address, value);
		}
	}
	
	public void deIndexString(XAddress address, XValue value, String s) {
		for(String word : getStringIndexStrings(s)) {
			this.decrementIndexEntry(word, address, value);
		}
	}
	
	public void indexId(XAddress address, XValue value, XID id) {
		this.incrementIndexEntry(getIdIndexString(id), address, value);
	}
	
	public void deIndexId(XAddress address, XValue value, XID id) {
		this.decrementIndexEntry(getIdIndexString(id), address, value);
	}
	
	public void indexAddressArray(XAddress objectAddress, XValue value, XAddress[] addresses) {
		for(XAddress adr : addresses) {
			indexAddress(objectAddress, value, adr);
		}
	}
	
	public void deIndexAddressArray(XAddress address, XValue value, XAddress[] addresses) {
		for(XAddress adr : addresses) {
			deIndexAddress(address, value, adr);
		}
	}
	
	public void indexAddress(XAddress objectAddress, XValue value, XAddress address) {
		String key = getAddressIndexString(address);
		
		this.incrementIndexEntry(key, objectAddress, address);
	}
	
	public void deIndexAddress(XAddress objectAddress, XValue value, XAddress address) {
		this.decrementIndexEntry(getAddressIndexString(address), objectAddress, value);
	}
	
	private void incrementIndexEntry(String key, XAddress address, XValue value) {
		EqualsConstraint<String> constraint = new EqualsConstraint<String>(key);
		Iterator<ValueIndexEntry> iterator = this.index.constraintIterator(constraint);
		
		boolean found = false;
		while(!found && iterator.hasNext()) {
			ValueIndexEntry triple = iterator.next();
			
			if(triple.equalAddressAndValue(address, value)) {
				found = true;
				triple.incrementCounter();
			}
		}
		
		if(!found) {
			// no entry found -> add one
			ValueIndexEntry entry = new ValueIndexEntry(address, value, 1);
			this.index.index(key, entry);
		}
	}
	
	private void decrementIndexEntry(String key, XAddress address, XValue value) {
		EqualsConstraint<String> constraint = new EqualsConstraint<String>(key);
		Iterator<ValueIndexEntry> iterator = this.index.constraintIterator(constraint);
		
		boolean found = false;
		while(!found && iterator.hasNext()) {
			ValueIndexEntry triple = iterator.next();
			
			if(triple.equalAddressAndValue(address, value)) {
				found = true;
				triple.decrementCounter();
				
				if(triple.getCounter() == 0) {
					this.index.deIndex(key, triple);
				}
			}
		}
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
