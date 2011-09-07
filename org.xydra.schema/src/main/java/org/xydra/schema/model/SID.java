package org.xydra.schema.model;

import org.xydra.base.XID;


public class SID extends SValue {
	
	public XID value;
	
	public SID(XID value) {
		super();
		this.value = value;
	}
	
	@Override
    public void toSyntax(StringBuffer buf) {
		buf.append(this.value.toString());
	}
	
}
