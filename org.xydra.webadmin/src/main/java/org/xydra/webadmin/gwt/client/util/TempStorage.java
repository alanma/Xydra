package org.xydra.webadmin.gwt.client.util;

import org.xydra.base.XAddress;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.datamodels.DataModel;
import org.xydra.webadmin.gwt.client.widgets.version2.BranchWidget;


public class TempStorage {
	
	private static final Logger log = LoggerFactory.getLogger(DataModel.class);
	
	public BranchWidget branch;
	
	public void register(BranchWidget branch) {
		this.branch = branch;
	}
	
	public void setInformation(XAddress address, String text) {
		if(address.getAddressedType().equals(XType.XREPOSITORY)) {
			Controller.getInstance().getDataModel()
			        .addModel(address.getRepository(), XX.toId(text));
			log.info("model " + text + " added!");
			
		} else {
			Controller.getInstance().getDataModel().addObject(address, XX.toId(text));
			if(address.equals(Controller.getInstance().getSelectedModelAddress())) {
				Controller.getInstance().updateEditorPanel();
			}
		}
		this.branch = null;
		Controller.getInstance().notifySelectionTree(address);
	}
	
	public void remove(XAddress address) {
		Controller.getInstance().getDataModel().removeItem(address);
		XAddress reducedAddress = XX.toAddress(address.getRepository(), null, null, null);
		Controller.getInstance().notifySelectionTree(reducedAddress);
		log.info("inspecting address " + address.toString() + " which has object: "
		        + address.getObject().toString());
		if(address.getObject() != null) {
			log.info("updating Editor panel!");
			Controller.getInstance().updateEditorPanel();
		}
		
	}
}
