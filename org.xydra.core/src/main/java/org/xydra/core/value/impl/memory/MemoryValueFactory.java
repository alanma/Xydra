package org.xydra.core.value.impl.memory;

import java.util.Collection;

import org.xydra.core.model.XID;
import org.xydra.core.value.XBooleanListValue;
import org.xydra.core.value.XBooleanValue;
import org.xydra.core.value.XByteListValue;
import org.xydra.core.value.XDoubleListValue;
import org.xydra.core.value.XDoubleValue;
import org.xydra.core.value.XIDListValue;
import org.xydra.core.value.XIDSetValue;
import org.xydra.core.value.XIDSortedSetValue;
import org.xydra.core.value.XIntegerListValue;
import org.xydra.core.value.XIntegerValue;
import org.xydra.core.value.XLongListValue;
import org.xydra.core.value.XLongValue;
import org.xydra.core.value.XStringListValue;
import org.xydra.core.value.XStringSetValue;
import org.xydra.core.value.XStringValue;
import org.xydra.core.value.XValueFactory;


/**
 * An implementation of {@link XValueFactory}
 * 
 * @author voelkel
 * @author Kaidel
 * @author dscharrer
 * 
 */
public class MemoryValueFactory implements XValueFactory {
	
	public XStringValue createStringValue(String string) {
		return new MemoryStringValue(string);
	}
	
	public XBooleanValue createBooleanValue(boolean value) {
		return new MemoryBooleanValue(value);
	}
	
	public XDoubleValue createDoubleValue(double value) {
		return new MemoryDoubleValue(value);
	}
	
	public XIntegerValue createIntegerValue(int value) {
		return new MemoryIntegerValue(value);
	}
	
	public XLongValue createLongValue(long value) {
		return new MemoryLongValue(value);
	}
	
	public XBooleanListValue createBooleanListValue(boolean[] values) {
		return new MemoryBooleanListValue(values);
	}
	
	public XDoubleListValue createDoubleListValue(double[] values) {
		return new MemoryDoubleListValue(values);
	}
	
	public XIntegerListValue createIntegerListValue(int[] values) {
		return new MemoryIntegerListValue(values);
	}
	
	public XLongListValue createLongListValue(long[] values) {
		return new MemoryLongListValue(values);
	}
	
	public XStringListValue createStringListValue(String[] strings) {
		return new MemoryStringListValue(strings);
	}
	
	public XStringListValue createStringListValue(Collection<String> strings) {
		return new MemoryStringListValue(strings);
	}
	
	public XIDListValue createIDListValue(XID[] xids) {
		return new MemoryIDListValue(xids);
	}
	
	public XIDListValue createIDListValue(Collection<XID> xids) {
		return new MemoryIDListValue(xids);
	}
	
	public XBooleanListValue createBooleanListValue(Collection<Boolean> values) {
		return new MemoryBooleanListValue(values);
	}
	
	public XDoubleListValue createDoubleListValue(Collection<Double> values) {
		return new MemoryDoubleListValue(values);
	}
	
	public XIntegerListValue createIntegerListValue(Collection<Integer> values) {
		return new MemoryIntegerListValue(values);
	}
	
	public XLongListValue createLongListValue(Collection<Long> values) {
		return new MemoryLongListValue(values);
	}
	
	public XByteListValue createByteListValue(byte[] values) {
		return new MemoryByteListValue(values);
	}
	
	public XByteListValue createByteListValue(Collection<Byte> values) {
		return new MemoryByteListValue(values);
	}
	
	public XStringSetValue createStringSetValue(String[] values) {
		return new MemoryStringSetValue(values);
	}
	
	public XStringSetValue createStringSetValue(Collection<String> values) {
		return new MemoryStringSetValue(values);
	}
	
	public XIDSetValue createIDSetValue(XID[] values) {
		return new MemoryIDSetValue(values);
	}
	
	public XIDSetValue createIDSetValue(Collection<XID> values) {
		return new MemoryIDSetValue(values);
	}
	
	public XIDSortedSetValue createIDSortedSetValue(XID[] values) {
		return new MemoryIDSortedSetValue(values);
	}
	
	public XIDSortedSetValue createIDSortedSetValue(Collection<XID> values) {
		return new MemoryIDSortedSetValue(values);
	}
}
