package org.xydra.webadmin.gwt.client.widgets.selectiontree.repobranches;

import org.xydra.webadmin.gwt.client.widgets.selectiontree.ModelBranchWidget;


public interface IRepoBranchWidget {
	
	abstract void init();
	
	abstract void setExpandButtonText(String string);
	
	abstract void clearBranches();
	
	abstract void setAnchorText(String string);
	
	abstract void addBranch(ModelBranchWidget newBranch);
	
}
