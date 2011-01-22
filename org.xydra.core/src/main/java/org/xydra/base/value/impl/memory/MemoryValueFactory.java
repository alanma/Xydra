package org.xydra.base.value.impl.memory;

import java.util.Collection;

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
import org.xydra.base.value.XValueFactory;


/**
 * An implementation of {@link XValueFactory}
 * 
 * @author voelkel
 * @author Kaidel
 * @author dscharrer
 * 
 */
public class MemoryValueFactory implements XValueFactory {
	
	public XAddressListValue createAddressListValue(Collection<XAddress> values) {
		return new MemoryAddressListValue(values);
	}
	
	public XAddressListValue createAddressListValue(XAddress[] values) {
		return new MemoryAddressListValue(values);
	}
	
	public XAddressSetValue createAddressSetValue(Collection<XAddress> values) {
		return new MemoryAddressSetValue(values);
	}
	
	public XAddressSetValue createAddressSetValue(XAddress[] values) {
		return new MemoryAddressSetValue(values);
	}
	
	public XAddressSortedSetValue createAddressSortedSetValue(Collection<XAddress> values) {
		return new MemoryAddressSortedSetValue(values);
	}
	
	public XAddressSortedSetValue createAddressSortedSetValue(XAddress[] values) {
		return new MemoryAddressSortedSetValue(values);
	}
	
	public XBooleanListValue createBooleanListValue(boolean[] values) {
		return new MemoryBooleanListValue(values);
	}
	
	public XBooleanListValue createBooleanListValue(Collection<Boolean> values) {
		return new MemoryBooleanListValue(values);
	}
	
	public XBooleanValue createBooleanValue(boolean value) {
		return new MemoryBooleanValue(value);
	}
	
	public XByteListValue createByteListValue(byte[] values) {
		return new MemoryByteListValue(values);
	}
	
	public XByteListValue createByteListValue(Collection<Byte> values) {
		return new MemoryByteListValue(values);
	}
	
	public XDoubleListValue createDoubleListValue(Collection<Double> values) {
		return new MemoryDoubleListValue(values);
	}
	
	public XDoubleListValue createDoubleListValue(double[] values) {
		return new MemoryDoubleListValue(values);
	}
	
	public XDoubleValue createDoubleValue(double value) {
		return new MemoryDoubleValue(value);
	}
	
	public XIDListValue createIDListValue(Collection<XID> xids) {
		return new MemoryIDListValue(xids);
	}
	
	public XIDListValue createIDListValue(XID[] xids) {
		return new MemoryIDListValue(xids);
	}
	
	public XIDSetValue createIDSetValue(Collection<XID> values) {
		return new MemoryIDSetValue(values);
	}
	
	public XIDSetValue createIDSetValue(XID[] values) {
		return new MemoryIDSetValue(values);
	}
	
	public XIDSortedSetValue createIDSortedSetValue(Collection<XID> values) {
		return new MemoryIDSortedSetValue(values);
	}
	
	public XIDSortedSetValue createIDSortedSetValue(XID[] values) {
		return new MemoryIDSortedSetValue(values);
	}
	
	public XIntegerListValue createIntegerListValue(Collection<Integer> values) {
		return new MemoryIntegerListValue(values);
	}
	
	public XIntegerListValue createIntegerListValue(int[] values) {
		return new MemoryIntegerListValue(values);
	}
	
	public XIntegerValue createIntegerValue(int value) {
		return new MemoryIntegerValue(value);
	}
	
	public XLongListValue createLongListValue(Collection<Long> values) {
		return new MemoryLongListValue(values);
	}
	
	public XLongListValue createLongListValue(long[] values) {
		return new MemoryLongListValue(values);
	}
	
	public XLongValue createLongValue(long value) {
		return new MemoryLongValue(value);
	}
	
	public XStringListValue createStringListValue(Collection<String> strings) {
		return new MemoryStringListValue(strings);
	}
	
	public XStringListValue createStringListValue(String[] strings) {
		return new MemoryStringListValue(strings);
	}
	
	public XStringSetValue createStringSetValue(Collection<String> values) {
		return new MemoryStringSetValue(values);
	}
	
	public XStringSetValue createStringSetValue(String[] values) {
		return new MemoryStringSetValue(values);
	}
	
	public XStringValue createStringValue(String string) {
		return new MemoryStringValue(string);
	}
}
