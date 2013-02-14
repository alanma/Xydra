package org.xydra.webadmin.gwt.client.widgets.dialogs;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
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


public class NYIDialog extends DialogBox {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,NYIDialog> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	String inputString = "";
	
	@UiField
	VerticalPanel mainPanel;
	
	@UiField
	Label infoText;
	
	@UiField(provided = true)
	ButtonPanel buttonPanel;
	
	public NYIDialog() {
		
		super();
		
		ClickHandler okHandler = new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				NYIDialog.this.removeFromParent();
			}
		};
		
		this.buttonPanel = new ButtonPanel(okHandler, this);
		
		setWidget(uiBinder.createAndBindUi(this));
		
		this.setStyleName("dialogStyle");
		this.setText("add Element");
		this.getElement().setId("removeDialog");
		
		this.center();
	}
	
}
