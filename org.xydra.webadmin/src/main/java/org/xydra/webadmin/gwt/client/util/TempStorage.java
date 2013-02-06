package org.xydra.webadmin.gwt.client.util;

import org.xydra.base.XAddress;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.datamodels.DataModel;
import org.xydra.webadmin.gwt.client.widgets.dialogs.CommittingDialog;
import org.xydra.webadmin.gwt.client.widgets.selectiontree.BranchWidget;


public class TempStorage {
	
	private static final Logger log = LoggerFactory.getLogger(DataModel.class);
	
	public BranchWidget branch;
	
	private CommittingDialog committingDialog;
	
	public void register(BranchWidget branch) {
		this.branch = branch;
	}
	
	public void setInformation(XAddress address, String text) {
		XType addressedType = address.getAddressedType();
		if(addressedType.equals(XType.XREPOSITORY)) {
			Controller.getInstance().getDataModel()
			        .addModel(address.getRepository(), XX.toId(text));
			log.info("model " + text + " added!");
			
		} else if(addressedType.equals(XType.XMODEL)) {
			Controller.getInstance().getDataModel().addObject(address, XX.toId(text));
		} else if(addressedType.equals(XType.XOBJECT)) {
			XAddress fieldAddress = XX.resolveField(address, XX.toId(text));
			Controller.getInstance().getDataModel().addField(fieldAddress, null);
			// if(address.equals(Controller.getInstance().getSelectedModelAddress()))
			// {
			// Controller.getInstance().updateEditorPanel();
			// }
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
	
	public void register(CommittingDialog committingDialog) {
		this.committingDialog = committingDialog;
		
	}
	
	public void notifyDialog(String message) {
		this.committingDialog.notifyMe(message);
	}
}
