package org.xydra.webadmin.gwt.client.widgets.tablewidgets;

import org.xydra.base.XAddress;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.dialogs.AddElementDialog;
import org.xydra.webadmin.gwt.client.widgets.dialogs.RemoveElementDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class RowHeaderWidget extends Composite {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,RowHeaderWidget> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField
	VerticalPanel panel;
	
	@UiField
	Label idLabel;
	
	@UiField
	Label revisionLabel;
	
	@UiField
	Button removeObjectButton;
	
	@UiField
	Button addFieldButton;
	
	private XAddress address;
	
	public RowHeaderWidget(XAddress address, long revisionNumber) {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		
		this.address = address;
		
		this.idLabel.setText(address.getObject().toString());
		
		this.revisionLabel.setText("" + revisionNumber);
		
	}
	
	@UiHandler("removeObjectButton")
	void onClickRemove(ClickEvent event) {
		
		RemoveElementDialog removeDialog = new RemoveElementDialog(RowHeaderWidget.this.address);
		removeDialog.show();
		
	}
	
	@UiHandler("addFieldButton")
	void onClickAdd(ClickEvent event) {
		AddElementDialog addDialog = new AddElementDialog(RowHeaderWidget.this.address,
		        "enter Field ID");
		addDialog.show();
		addDialog.selectEverything();
		
	}
}
