package org.xydra.webadmin.gwt.client.util;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.Observable;
import org.xydra.webadmin.gwt.client.datamodels.DataModel;
import org.xydra.webadmin.gwt.client.widgets.dialogs.WarningDialog;
import org.xydra.webadmin.gwt.client.widgets.selectiontree.RepoBranchWidget;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;


public class TempStorage {
	
	private static final Logger log = LoggerFactory.getLogger(DataModel.class);
	
	public RepoBranchWidget branch;
	
	private Observable dialog;
	
	private XAddress desiredAddress;
	
	public void register(RepoBranchWidget branch) {
		this.branch = branch;
		showWaitCursor();
	}
	
	public void register(Observable committingDialog) {
		this.dialog = committingDialog;
		showWaitCursor();
		
	}
	
	public void setInformation(XAddress address, String text) {
		XAddress newAddress = address;
		XType addressedType = address.getAddressedType();
		if(addressedType.equals(XType.XREPOSITORY)) {
			if(address.getRepository().equals(XX.toId("noRepo"))) {
				XId repoId = XX.toId(text);
				newAddress = XX.toAddress(repoId, null, null, null);
				Controller.getInstance().getDataModel().addRepoID(repoId);
			} else {
				Controller.getInstance().getDataModel()
				        .addModel(address.getRepository(), XX.toId(text));
				log.info("model " + text + " added!");
			}
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
		Controller.getInstance().notifySelectionTree(newAddress);
	}
	
	public void remove(XAddress address) {
		Controller.getInstance().getDataModel().removeItem(address);
		XAddress reducedAddress = XX.toAddress(address.getRepository(), null, null, null);
		Controller.getInstance().notifySelectionTree(reducedAddress);
		showDefaultCursor();
	}
	
	public void notifyDialog(String message) {
		this.dialog.notifyMe(message);
		showDefaultCursor();
	}
	
	public void allowDialogClose() {
		this.dialog.addCloseOKButton();
		
	}
	
	public void remove(XAddress address, Boolean removeFromRepo) {
		remove(address);
		if(removeFromRepo) {
			log.info("selected Delete model");
			Controller.getInstance().removeModel(address);
		}
		
	}
	
	public static void showWaitCursor() {
		DOM.setStyleAttribute(RootPanel.getBodyElement(), "cursor", "wait");
	}
	
	public static void showDefaultCursor() {
		DOM.setStyleAttribute(RootPanel.getBodyElement(), "cursor", "default");
	}
	
	public void proceed() {
		
		XWritableObject xObject = Controller.getInstance().getDataModel()
		        .getRepo(this.desiredAddress.getRepository())
		        .getModel(this.desiredAddress.getModel())
		        .getObject(this.desiredAddress.getObject());
		if(this.desiredAddress.getObject() != null) {
			if(xObject == null) {
				new WarningDialog("" + this.desiredAddress.toString() + " does not exist!");
				
			} else {
				Controller.getInstance().notifyTableController(this.desiredAddress,
				        TableController.Status.Opened);
			}
		}
		if(this.desiredAddress.getField() != null) {
			if(xObject.getField(this.desiredAddress.getField()) == null) {
				new WarningDialog("" + this.desiredAddress.toString() + " does not exist!");
			}
			Controller.getInstance().getTableController().scrollToField(this.desiredAddress);
		}
		
		this.desiredAddress = null;
		
	}
	
	// private void expandAndLoadModel() {
	// Controller.getInstance().getSelectionTree().expandEntity(this.desiredAddress);
	// // Controller.getInstance().loadCurrentModelsObjects();
	// }
	
	public void register(XAddress address) {
		this.desiredAddress = address;
	}
}
