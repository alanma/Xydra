package org.xydra.webadmin.gwt.client.widgets.selectiontree;

import org.xydra.base.XAddress;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.util.Presenter;


public class SelectionTreePresenter extends Presenter {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(RepoBranchWidget.class);
	
	public void presentModel(XAddress address) {
		Controller.getInstance().presentModel(address);
		
	}
	
	void fetchModelsFromServer(XAddress address) {
		Controller.getInstance().fetchModelIds(address, null);
		
	}
	
}
