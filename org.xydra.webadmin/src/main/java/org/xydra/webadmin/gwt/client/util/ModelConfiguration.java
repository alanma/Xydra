package org.xydra.webadmin.gwt.client.util;

import org.xydra.base.XID;
import org.xydra.webadmin.gwt.client.XyAdmin;


public class ModelConfiguration {
	
	public XyAdmin adminObject;
	public XID repoId;
	public XID modelId;
	public long revisionNumber;
	
	public ModelConfiguration(XyAdmin adminObject, XID repoId, XID modelId) {
		
		this.adminObject = adminObject;
		this.repoId = repoId;
		this.modelId = modelId;
	}
	
}
