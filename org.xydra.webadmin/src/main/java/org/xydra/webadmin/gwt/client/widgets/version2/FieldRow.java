package org.xydra.webadmin.gwt.client.widgets.version2;

import org.xydra.base.XID;
import org.xydra.base.rmof.XReadableObject;


public class FieldRow {
	
	private XReadableObject object;
	private XID id;
	
	public FieldRow(XID id, XReadableObject object) {
		this.id = id;
		this.object = object;
		
	}
	
	public XReadableObject getObject() {
		return this.object;
	}
	
	public XID getID() {
		return this.id;
	}
}
