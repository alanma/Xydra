package org.xydra.webadmin.gwt.client.widgets.editorpanel;

import org.xydra.base.XAddress;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.webadmin.gwt.client.util.Presenter;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.dialogs.CommittingDialog;
import org.xydra.webadmin.gwt.client.widgets.dialogs.ConfirmationDialog;


public class EditorPanelPresenter extends Presenter {
	
	private IEditorPanel editorPanel;
	private XAddress currentModelAddress;
	
	public EditorPanelPresenter(IEditorPanel editorPanel) {
		this.editorPanel = editorPanel;
		
		init();
	}
	
	private void init() {
		this.editorPanel.init();
		XyAdmin.getInstance().getController().registerEditorPanelPresenter(this);
	}
	
	public void presentModel(XAddress address) {
		this.currentModelAddress = address;
		
		this.buildModelView();
	}
	
	public void buildModelView() {
		
		SessionCachedModel selectedModel = getCurrentModel();
		
		// this.editorPanel.
		// this.mainPanel.clear();
		// do we need this???
		// this.mainPanel.add(new ModelControlPanel(this.presenter));
		// final ModelInformationPanel modelInformationPanel = new
		// ModelInformationPanel(this);
		// this.mainPanel.add(modelInformationPanel);
		
		// EventHelper.addModelChangeListener(this.currentModelAddress,
		// new IModelChangedEventHandler() {
		//
		// @Override
		// public void onModelChange(ModelChangedEvent event) {
		// if(event.getStatus().equals(EntityStatus.DELETED)) {
		// resetView();
		//
		// } else if(event.getStatus().equals(EntityStatus.EXTENDED)) {
		// EditorPanelPresenter.this.presentNewInformation();
		// }
		// }
		//
		// });
		// }
		//
		// private void resetView() {
		// this.mainPanel.clear();
		// this.mainPanel.add(new Label("choose model via selection tree"));
		//
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
	
	public void presentNewInformation() {
		// modelInformationPanel.setTableData(getCurrentModel());
		
	}
	
	private SessionCachedModel getCurrentModel() {
		SessionCachedModel selectedModel = XyAdmin.getInstance().getModel()
		        .getRepo(this.currentModelAddress.getRepository())
		        .getModel(this.currentModelAddress.getModel());
		return selectedModel;
	}
	
}
