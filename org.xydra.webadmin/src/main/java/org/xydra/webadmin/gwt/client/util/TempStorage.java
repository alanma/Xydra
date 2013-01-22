package org.xydra.webadmin.gwt.client.util;

import org.xydra.webadmin.gwt.client.widgets.version2.BranchWidget;


public class TempStorage {
	
	public BranchWidget branch;
	
	public void register(BranchWidget branch) {
		this.branch = branch;
	}
	
	public void setInformation(String text) {
		this.branch.addElement(text);
	}
	
}
