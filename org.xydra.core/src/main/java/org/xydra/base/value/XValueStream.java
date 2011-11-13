package org.xydra.base.value;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.core.serialize.json.JsonSerializer;
import org.xydra.core.serialize.xml.XmlSerializer;


/**
 * A stream of XValue as occruing in serialisation formats.
 * 
 * Currently not used by XML serialisation ({@link XmlSerializer}) nor by JOSN (
 * {@link JsonSerializer}).
 * 
 * Currently only used for serialising values <em>to</em> CSV format. Not used
 * for parsing yet.
 * 
 * Unclear if this interface will be needed/supported in the long-term.
 * 
 * @author xamde
 */
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
