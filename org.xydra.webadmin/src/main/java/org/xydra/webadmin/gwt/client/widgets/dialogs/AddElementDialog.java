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
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class AddElementDialog extends DialogBox {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,AddElementDialog> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	String inputString = "";
	
	@UiField
	VerticalPanel mainPanel;
	
	@UiField(provided = true)
	TextAreaWidget textArea;
	
	@UiField(provided = true)
	ButtonPanel buttonPanel;
	
	private XAddress address;
	
	public AddElementDialog(XAddress address, String text) {
		
		super();
		
		this.address = address;
		
		ClickHandler okHandler = new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				AddElementDialog.this.removeFromParent();
				Controller
				        .getInstance()
				        .getTempStorage()
				        .setInformation(AddElementDialog.this.address,
				                AddElementDialog.this.textArea.getText());
			}
		};
		
		this.buttonPanel = new ButtonPanel(okHandler, this);
		this.textArea = new TextAreaWidget(text, this.buttonPanel);
		
		setWidget(uiBinder.createAndBindUi(this));
		
		this.setStyleName("dialogStyle");
		this.setText("add Element");
		this.center();
	}
	
	public void selectEverything() {
		this.textArea.selectEverything();
	}
	
}
