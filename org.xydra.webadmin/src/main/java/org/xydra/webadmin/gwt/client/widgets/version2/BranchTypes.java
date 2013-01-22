package org.xydra.webadmin.gwt.client.widgets.version2;

import org.xydra.base.XAddress;
import org.xydra.base.XID;


public enum BranchTypes {
	
	REPO("add Model", null, "enter new model name"), MODEL("add Object", "delete Model",
	        "enter new object name"), OBJECT("add Field", "remove Object", "enter new field name");
	
	String plusButtonText;
	String deleteButtonText;
	String addWidgetText;
	
	private BranchTypes(String addButtonText, String removeButtonText, String addWidgetText) {
		this.plusButtonText = addButtonText;
		this.deleteButtonText = removeButtonText;
		this.addWidgetText = addWidgetText;
	}
	
	public static BranchTypes getBranchFromAddress(XAddress address) {
		
		XID repoId = address.getRepository();
		XID modelId = address.getModel();
		XID objectId = address.getObject();
		if(modelId == null) {
			
			return REPO;
		} else if(objectId == null) {
			return MODEL;
			
		}
		
		return null;
	}
}
