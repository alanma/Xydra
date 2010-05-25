package org.xydra.core.value.impl.memory;

import org.xydra.core.model.XID;
import org.xydra.core.value.XBooleanListValue;
import org.xydra.core.value.XBooleanValue;
import org.xydra.core.value.XDoubleListValue;
import org.xydra.core.value.XDoubleValue;
import org.xydra.core.value.XIDListValue;
import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XIntegerListValue;
import org.xydra.core.value.XIntegerValue;
import org.xydra.core.value.XLongListValue;
import org.xydra.core.value.XLongValue;
import org.xydra.core.value.XStringListValue;
import org.xydra.core.value.XStringValue;
import org.xydra.core.value.XValueFactory;


/**
 * An implementation of {@link XValueFactory}
 * 
 * @author voelkel
 * @author Kaidel
 * 
 */
public class MemoryValueFactory implements XValueFactory {
	
	public XStringValue createStringValue(String string) {
		return new MemoryStringValue(string);
	}
	
	public XIDValue createIDValue(XID id) {
		return new MemoryIDValue(id);
	}
	
	public XIDListValue createIDListValue(XID[] xids) {
		return new MemoryIDListValue(xids);
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
	
	public XBooleanListValue createBooleanListValue(Boolean[] values) {
		return new MemoryBooleanListValue(values);
	}
	
	public XDoubleListValue createDoubleListValue(Double[] values) {
		return new MemoryDoubleListValue(values);
	}
	
	public XIntegerListValue createIntegerListValue(Integer[] values) {
		return new MemoryIntegerListValue(values);
	}
	
	public XLongListValue createLongListValue(Long[] values) {
		return new MemoryLongListValue(values);
	}
}
