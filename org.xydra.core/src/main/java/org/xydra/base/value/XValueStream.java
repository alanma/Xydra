package org.xydra.base.value;

import org.xydra.base.XAddress;
import org.xydra.base.XID;


public interface XValueStream {
	
	void startValue();
	
	void endValue();
	
	void startCollection(ValueType type);
	
	void endCollection();
	
	void javaNull();
	
	void address(XAddress address);
	
	void javaBoolean(Boolean a);
	
	void javaDouble(Double a);
	
	void javaInteger(Integer a);
	
	void javaLong(Long a);
	
	void javaString(String a);
	
	void xid(XID a);
	
}
