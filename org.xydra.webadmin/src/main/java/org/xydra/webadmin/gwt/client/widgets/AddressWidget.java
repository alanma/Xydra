package org.xydra.webadmin.gwt.client.widgets;

import org.xydra.base.XX;
import org.xydra.gwt.editor.value.XAddressEditor;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.dialogs.NYIDialog;

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
		// Controller.getInstance().getDataModel().loadEntity(this.addressEditor.getValue());
		showNYIDialog();
	}
	
	@UiHandler("clearButton")
	void onClickClear(ClickEvent e) {
		this.addressEditor.setValue(XX.toAddress(XX.toId("repo1"), null, null, null));
	}
	
	@UiHandler("addElementButton")
	void onClickAdd(ClickEvent e) {
		showNYIDialog();
	}
	
	@UiHandler("deleteElementButton")
	void onClickDelete(ClickEvent e) {
		showNYIDialog();
	}
	
	void showNYIDialog() {
		NYIDialog dialog = new NYIDialog();
		dialog.show();
	}
}
