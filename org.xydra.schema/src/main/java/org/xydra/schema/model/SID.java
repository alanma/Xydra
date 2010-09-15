package org.xydra.schema.model;

import org.xydra.core.model.XID;


public class SID extends SValue {
	
	public XID value;
	
	public SID(XID value) {
		super();
		this.value = value;
	}
	
	public void toSyntax(StringBuffer buf) {
		buf.append(this.value.toURI());
	}
	
}
