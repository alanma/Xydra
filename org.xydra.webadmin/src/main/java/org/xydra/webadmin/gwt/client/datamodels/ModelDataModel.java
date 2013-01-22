package org.xydra.webadmin.gwt.client.datamodels;

import org.xydra.base.XID;


public class ModelDataModel {
	
	private XID id;
	
	public ModelDataModel(XID xid) {
		this.id = xid;
	}
	
	public XID getID() {
		return this.id;
	}
	
}
