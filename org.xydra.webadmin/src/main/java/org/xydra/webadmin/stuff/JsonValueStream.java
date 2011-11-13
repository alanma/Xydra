package org.xydra.webadmin.stuff;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.json.JSONWriter;


/**
 * TODO this class has never been tested/used yet.
 * 
 * @author xamde
 * 
 */
public class JsonValueStream extends AbstractValueStream {
	
	@Override
	public void address(XAddress address) {
		this.buf.append("{address:" + encode(address.toString()) + "}");
	}
	
	@Override
	public void xid(XID a) {
		this.buf.append("{xid:" + encode(a.toString()) + "}");
	}
	
	@Override
	public String encode(String s) {
		return JSONWriter.quote(s);
	}
	
}
