package org.xydra.webadmin.gwt.client.datamodels;

import org.xydra.base.XId;


public class ModelDataModel {
	
	private XId id;
	
	public ModelDataModel(XId xid) {
		this.id = xid;
	}
	
	public XId getID() {
		return this.id;
	}
	
}
