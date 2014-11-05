package org.xydra.webadmin.gwt.client;

import java.util.HashSet;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.AddressWidgetPresenter;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.editorpanel.EditorPanelPresenter;
import org.xydra.webadmin.gwt.client.widgets.selectiontree.SelectionTreePresenter;
import org.xydra.webadmin.gwt.shared.XyAdminServiceAsync;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Unit that holds some regularly needed objects and has the ability to start /
 * stop processes.
 * 
 * @author kahmann
 * 
 */
public class Controller {

	private ServiceConnection service;
	private SelectionTreePresenter selectionTreePresenter;
	private EditorPanelPresenter editorPanelPresenter;
	private Set<HandlerRegistration> registrations = new HashSet<HandlerRegistration>();
	private AddressWidgetPresenter addressWidgetPresenter;

	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);

	public Controller(XyAdminServiceAsync service2, SelectionTreePresenter selectionTreePresenter2,
			EditorPanelPresenter editorPanelPresenter2,
			AddressWidgetPresenter addressWidgetPresenter2) {

		this.service = new ServiceConnection(service2);
		this.selectionTreePresenter = selectionTreePresenter2;
		this.editorPanelPresenter = editorPanelPresenter2;
		this.addressWidgetPresenter = addressWidgetPresenter2;
	}

	public void startPresenting() {
		this.selectionTreePresenter.present();
		this.editorPanelPresenter.present();
		this.addressWidgetPresenter.present();
	}

	public void loadModelsObjects(XAddress address) {
		this.service.loadModelsObjects(address);
	}

	public void commit(XAddress modelAddress, XCommand addModelCommand,
			final XTransaction modelTransactions) {

		if (addModelCommand != null) {
			this.service.commitAddedModel(modelAddress, addModelCommand, modelTransactions);
		} else {
			this.service.commitModelTransactions(modelAddress, modelTransactions);
		}
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
		RootPanel.getBodyElement().getStyle().setProperty("cursor", "wait");
	}

	public static void showDefaultCursor() {
		RootPanel.getBodyElement().getStyle().setProperty("cursor", "default");
	}

	public XAddress getCurrentlyOpenedModelAddress() {
		return this.editorPanelPresenter.getCurrentModelAddress();
	}

	public void addRegistration(HandlerRegistration handler) {
		this.registrations.add(handler);
	}

	public void unregistrateAllHandlers() {
		for (HandlerRegistration handler : this.registrations) {
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

}
