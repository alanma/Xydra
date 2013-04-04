package org.xydra.webadmin.gwt.client.widgets.selectiontree.modelbranches;

import org.xydra.base.XAddress;

import com.google.gwt.event.dom.client.ClickHandler;


public interface IModelBranchWidget {
	
	void init(XAddress address, ClickHandler anchorClickHandler);
	
	void setStatusDeleted();
	
	void setRevisionUnknown();
	
	void delete();
	
	void setRevisionNumber(long revisionNumber);
	
}
