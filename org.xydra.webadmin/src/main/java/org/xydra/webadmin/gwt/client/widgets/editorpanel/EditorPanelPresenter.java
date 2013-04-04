package org.xydra.webadmin.gwt.client.widgets.editorpanel;

import org.xydra.base.XAddress;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.webadmin.gwt.client.util.Presenter;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.dialogs.CommittingDialog;
import org.xydra.webadmin.gwt.client.widgets.dialogs.ConfirmationDialog;


public class EditorPanelPresenter extends Presenter {
	
	private EditorPanel editorPanel;
	private XAddress currentModelAddress;
	
	public EditorPanelPresenter(EditorPanel editorPanel) {
		this.editorPanel = editorPanel;
	}
	
	public void presentModel(XAddress address) {
		this.currentModelAddress = address;
		
		this.editorPanel.buildModelView(getCurrentModel());
	}
	
	public void loadModelsObjectsFromPersistence() {
		XyAdmin.getInstance().getController().loadModelsObjects(this.currentModelAddress, null);
	}
	
	public void handleFetchIDs() {
		// TODO Auto-generated method stub
		
	}
	
	void commit(ModelControlPanel modelControlPanel) {
		CommittingDialog committingDialog = new CommittingDialog(this);
		committingDialog.show();
	}
	
	@SuppressWarnings("unused")
	void discardChanges() {
		new ConfirmationDialog("discard all Changes");
	}
	
	public void expandAll() {
		// XAddress currentAddress =
		// XyAdmin.getInstance().getController().getSelectedModelAddress();
		// SessionCachedModel model = Controller.getCurrentlySelectedModel();
		//
		// if(this.expandAllButton.getText().equals("expand all objects")) {
		//
		// for(XId objectId : model) {
		// XyAdmin.getInstance()
		// .getController()
		// .notifyTableController(XX.resolveObject(currentAddress, objectId),
		// TableController.Status.Opened);
		// }
		//
		// this.expandAllButton.setText("close all objects");
		// } else {
		//
		// for(XId objectId : model) {
		// XyAdmin.getInstance()
		// .getController()
		// .notifyTableController(XX.resolveObject(currentAddress, objectId),
		// TableController.Status.Present);
		// }
		//
		// this.expandAllButton.setText("expand all objects");
		//
		// }
	}
	
	public XAddress getCurrentModelAddress() {
		return this.currentModelAddress;
	}
	
	public void presentNewInformation(ModelInformationPanel modelInformationPanel) {
		modelInformationPanel.setTableData(getCurrentModel());
		
	}
	
	private SessionCachedModel getCurrentModel() {
		SessionCachedModel selectedModel = XyAdmin.getInstance().getModel()
		        .getRepo(this.currentModelAddress.getRepository())
		        .getModel(this.currentModelAddress.getModel());
		return selectedModel;
	}
	
}
