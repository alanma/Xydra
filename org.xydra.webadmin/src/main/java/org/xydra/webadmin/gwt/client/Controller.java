package org.xydra.webadmin.gwt.client;

import java.util.HashSet;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.AddressWidgetPresenter;
import org.xydra.webadmin.gwt.client.widgets.WarningWidget;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.editorpanel.EditorPanelPresenter;
import org.xydra.webadmin.gwt.client.widgets.selectiontree.SelectionTreePresenter;
import org.xydra.webadmin.gwt.shared.XyAdminServiceAsync;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.RootPanel;


public class Controller {
	
	// TODO nochmal mit Max durchsprechen, wie das Ganze funktioniert (boot
	// etc.)
	private ServiceConnection service;
	private SelectionTreePresenter selectionTreePresenter;
	private EditorPanelPresenter editorPanelPresenter;
	private WarningWidget warningWidget;
	private Set<HandlerRegistration> registrations = new HashSet<HandlerRegistration>();
	private AddressWidgetPresenter addressWidgetPresenter;
	
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	public Controller() {
	}
	
	public void addService(XyAdminServiceAsync service) {
		this.service = new ServiceConnection(service);
	}
	
	public void loadModelsObjects(XAddress address) {
		this.service.loadModelsObjects(address);
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
	
	public void fetchModelIds(XAddress address) {
		this.service.getModelIdsFromServer(address);
		
	}
	
	public void presentModel(XAddress address) {
		this.editorPanelPresenter.presentModel(address);
		log.info("now presenting model " + address.toString());
		
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
	
	public XAddress getCurrentlyOpenedModelAddress() {
		return this.editorPanelPresenter.getCurrentModelAddress();
	}
	
	public void addRegistration(HandlerRegistration handler) {
		this.registrations.add(handler);
	}
	
	public void unregistrateAllHandlers() {
		for(HandlerRegistration handler : this.registrations) {
			handler.removeHandler();
			
			handler = null;
		}
		this.registrations.clear();
		log.info("unregistrated all handlers!");
	}
	
	public void removeRegistration(HandlerRegistration handler) {
		this.registrations.remove(handler);
	}
	
	public SelectionTreePresenter getSelectionTreePresenter() {
		return this.selectionTreePresenter;
		
	}
	
	public EditorPanelPresenter getEditorPanelPresenter() {
		return this.editorPanelPresenter;
	}
	
	public AddressWidgetPresenter getAddressWidgetPresenter() {
		return this.addressWidgetPresenter;
	}
	
	public void registerAddressWidgetPresenter(AddressWidgetPresenter addressWidgetPresenter2) {
		this.addressWidgetPresenter = addressWidgetPresenter2;
	}
	
}
