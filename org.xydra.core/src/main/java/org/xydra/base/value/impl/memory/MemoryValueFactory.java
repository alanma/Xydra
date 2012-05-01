package org.xydra.base.value.impl.memory;

import java.util.Collection;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.value.XAddressListValue;
import org.xydra.base.value.XAddressSetValue;
import org.xydra.base.value.XAddressSortedSetValue;
import org.xydra.base.value.XBinaryValue;
import org.xydra.base.value.XBooleanListValue;
import org.xydra.base.value.XBooleanValue;
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
	
	@Override
    public XAddressListValue createAddressListValue(Collection<XAddress> values) {
		return new MemoryAddressListValue(values);
	}
	
	@Override
    public XAddressListValue createAddressListValue(XAddress[] values) {
		return new MemoryAddressListValue(values);
	}
	
	@Override
    public XAddressSetValue createAddressSetValue(Collection<XAddress> values) {
		return new MemoryAddressSetValue(values);
	}
	
	@Override
    public XAddressSetValue createAddressSetValue(XAddress[] values) {
		return new MemoryAddressSetValue(values);
	}
	
	@Override
    public XAddressSortedSetValue createAddressSortedSetValue(Collection<XAddress> values) {
		return new MemoryAddressSortedSetValue(values);
	}
	
	@Override
    public XAddressSortedSetValue createAddressSortedSetValue(XAddress[] values) {
		return new MemoryAddressSortedSetValue(values);
	}
	
	@Override
    public XBooleanListValue createBooleanListValue(boolean[] values) {
		return new MemoryBooleanListValue(values);
	}
	
	@Override
    public XBooleanListValue createBooleanListValue(Collection<Boolean> values) {
		return new MemoryBooleanListValue(values);
	}
	
	@Override
    public XBooleanValue createBooleanValue(boolean value) {
		return new MemoryBooleanValue(value);
	}
	
	@Override
    public XBinaryValue createBinaryValue(byte[] values) {
		return new MemoryBinaryValue(values);
	}
	
	@Override
    public XBinaryValue createBinaryValue(Collection<Byte> values) {
		return new MemoryBinaryValue(values);
	}
	
	@Override
    public XDoubleListValue createDoubleListValue(Collection<Double> values) {
		return new MemoryDoubleListValue(values);
	}
	
	@Override
    public XDoubleListValue createDoubleListValue(double[] values) {
		return new MemoryDoubleListValue(values);
	}
	
	@Override
    public XDoubleValue createDoubleValue(double value) {
		return new MemoryDoubleValue(value);
	}
	
	@Override
    public XIDListValue createIDListValue(Collection<XID> xids) {
		return new MemoryIDListValue(xids);
	}
	
	@Override
    public XIDListValue createIDListValue(XID[] xids) {
		return new MemoryIDListValue(xids);
	}
	
	@Override
    public XIDSetValue createIDSetValue(Collection<XID> values) {
		return new MemoryIDSetValue(values);
	}
	
	@Override
    public XIDSetValue createIDSetValue(XID[] values) {
		return new MemoryIDSetValue(values);
	}
	
	@Override
    public XIDSortedSetValue createIDSortedSetValue(Collection<XID> values) {
		return new MemoryIDSortedSetValue(values);
	}
	
	@Override
    public XIDSortedSetValue createIDSortedSetValue(XID[] values) {
		return new MemoryIDSortedSetValue(values);
	}
	
	@Override
    public XIntegerListValue createIntegerListValue(Collection<Integer> values) {
		return new MemoryIntegerListValue(values);
	}
	
	@Override
    public XIntegerListValue createIntegerListValue(int[] values) {
		return new MemoryIntegerListValue(values);
	}
	
	@Override
    public XIntegerValue createIntegerValue(int value) {
		return new MemoryIntegerValue(value);
	}
	
	@Override
    public XLongListValue createLongListValue(Collection<Long> values) {
		return new MemoryLongListValue(values);
	}
	
	@Override
    public XLongListValue createLongListValue(long[] values) {
		return new MemoryLongListValue(values);
	}
	
	@Override
    public XLongValue createLongValue(long value) {
		return new MemoryLongValue(value);
	}
	
	@Override
    public XStringListValue createStringListValue(Collection<String> strings) {
		return new MemoryStringListValue(strings);
	}
	
	@Override
    public XStringListValue createStringListValue(String[] strings) {
		return new MemoryStringListValue(strings);
	}
	
	@Override
    public XStringSetValue createStringSetValue(Collection<String> values) {
		return new MemoryStringSetValue(values);
	}
	
	@Override
    public XStringSetValue createStringSetValue(String[] values) {
		return new MemoryStringSetValue(values);
	}
	
	@Override
    public XStringValue createStringValue(String string) {
		return new MemoryStringValue(string);
	}
}
