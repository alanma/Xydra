package org.xydra.base.value.impl.memory;

import org.xydra.base.value.ValueType;
import org.xydra.base.value.XNullValue;

public class MemoryNullValue implements XNullValue {

	public static final MemoryNullValue NULL = new MemoryNullValue();

	private static final long serialVersionUID = -3084818835971553882L;

	@Override
	public ValueType getType() {
		return ValueType.Null;
	}

}
