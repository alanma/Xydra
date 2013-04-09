package org.xydra.webadmin.gwt.client;

import org.xydra.base.XAddress;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.AddressWidget.CompoundActionCallback;
import org.xydra.webadmin.gwt.client.widgets.WarningWidget;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.editorpanel.EditorPanelPresenter;
import org.xydra.webadmin.gwt.client.widgets.selectiontree.SelectionTreePresenter;
import org.xydra.webadmin.gwt.shared.XyAdminServiceAsync;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;


public class Controller {
	
	private ServiceConnection service;
	@SuppressWarnings("unused")
	private SelectionTreePresenter selectionTreePresenter;
	private EditorPanelPresenter editorPanelPresenter;
	private WarningWidget warningWidget;
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	public Controller() {
	}
	
	public void addService(XyAdminServiceAsync service) {
		this.service = new ServiceConnection(service);
	}
	
	public void loadModelsObjects(XAddress address, CompoundActionCallback compoundActionCallback) {
		this.service.loadModelsObjects(address, compoundActionCallback);
	}
	
	public void commit(XAddress modelAddress, XCommand addModelCommand,
	        final XTransaction modelTransactions) {
		
		if(addModelCommand != null) {
			this.service.commitAddedModel(modelAddress, addModelCommand, modelTransactions);
		} else {
			this.service.commitModelTransactions(modelAddress, modelTransactions);
		}
	}
	
	public void displayError(String message) {
		this.warningWidget.display(message);
		
	}
	
	public void registerWarningWidget(WarningWidget warningWidget) {
		this.warningWidget = warningWidget;
	}
	
	public void removeModel(final XAddress address) {
		this.service.removeModel(address);
		
	}
	
	public void open(XAddress address) {
		// TODO Auto-generated method stub
		
	}
	
	public void fetchModelIds(XAddress address, CompoundActionCallback compoundPresentationCallback) {
		this.service.getModelIdsFromServer(address, compoundPresentationCallback);
		
	}
	
	public void presentModel(XAddress address) {
		this.editorPanelPresenter.presentModel(address);
		
	}
	
	public static void showWaitCursor() {
		DOM.setStyleAttribute(RootPanel.getBodyElement(), "cursor", "wait");
	}
	
	public static void showDefaultCursor() {
		DOM.setStyleAttribute(RootPanel.getBodyElement(), "cursor", "default");
	}
	
	public void registerSelectionTreePresenter(SelectionTreePresenter presenter) {
		this.selectionTreePresenter = presenter;
	}
	
	public void registerEditorPanelPresenter(EditorPanelPresenter presenter) {
		this.editorPanelPresenter = presenter;
	}
	
}
