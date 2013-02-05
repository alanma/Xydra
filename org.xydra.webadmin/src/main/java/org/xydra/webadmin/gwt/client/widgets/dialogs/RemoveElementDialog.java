package org.xydra.webadmin.gwt.client.widgets.dialogs;

import org.xydra.base.XAddress;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class RemoveElementDialog extends DialogBox {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,RemoveElementDialog> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	String inputString = "";
	
	@UiField
	VerticalPanel mainPanel;
	
	@UiField
	Label infoText;
	
	@UiField(provided = true)
	ButtonPanel buttonPanel;
	
	private XAddress address;
	
	public RemoveElementDialog(XAddress address) {
		
		super();
		
		this.address = address;
		
		ClickHandler okHandler = new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				RemoveElementDialog.this.removeFromParent();
				Controller.getInstance().getTempStorage().remove(RemoveElementDialog.this.address);
			}
		};
		
		this.buttonPanel = new ButtonPanel(okHandler, this);
		
		setWidget(uiBinder.createAndBindUi(this));
		
		this.infoText.setText("Are you sure you want to delete the item " + address.toString()
		        + "?");
		
		this.setStyleName("dialogStyle");
		this.setText("add Element");
		this.getElement().setId("removeDialog");
		
		this.center();
	}
	
}
