package org.xydra.webadmin.gwt.client.widgets;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XX;
import org.xydra.gwt.editor.value.XAddressEditor;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.XyAdmin;
import org.xydra.webadmin.gwt.client.datamodels.DataModel;
import org.xydra.webadmin.gwt.client.widgets.dialogs.WarningDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;


public class AddressWidget extends Composite {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,AddressWidget> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	String inputString = "";
	
	@UiField
	HorizontalPanel mainPanel;
	
	@UiField(provided = true)
	XAddressEditor addressEditor;
	
	@UiField
	Button loadLocationButton;
	
	@UiField
	Button addElementButton;
	
	@UiField
	Button deleteElementButton;
	
	@UiField
	Button clearButton;
	
	public AddressWidget() {
		
		super();
		
		this.addressEditor = new XAddressEditor(XX.toAddress(XX.toId("repo1"), null, null, null),
		        null);
		
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@UiHandler("loadLocationButton")
	void onClickLoad(ClickEvent e) {
		// showNYIDialog();
		XAddress address = null;
		try {
			address = this.addressEditor.getValue();
			
		} catch(Exception ex) {
			showDialog(ex.getLocalizedMessage());
		}
		DataModel dataModel = Controller.getInstance().getDataModel();
		
		XId repoID = address.getRepository();
		XAddress repoAddress = XX.resolveRepository(repoID);
		loadRepository(repoAddress);
		
		if(address.getModel() != null) {
			
		}
		
	}
	
	private void loadRepository(XAddress repoAddress) {
		Controller.getInstance().getIDsFromServer(repoAddress);
	}
	
	@UiHandler("clearButton")
	void onClickClear(ClickEvent e) {
		this.addressEditor.setValue(XX.toAddress(XX.toId("repo1"), null, null, null));
	}
	
	@UiHandler("addElementButton")
	void onClickAdd(ClickEvent e) {
		showDialog("not yet Implemented");
	}
	
	@UiHandler("deleteElementButton")
	void onClickDelete(ClickEvent e) {
		showDialog("not yet Implemented");
	}
	
	void showDialog(String message) {
		WarningDialog dialog = new WarningDialog(message);
		dialog.show();
	}
}
