package org.xydra.webadmin.gwt.client.widgets.dialogs;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;


public class TextAreaWidget extends Composite {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,TextAreaWidget> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	String inputString = "";
	
	@UiField
	TextBox textArea;
	
	private ButtonPanel buttonPanelReference;
	
	public TextAreaWidget(String text, ButtonPanel buttonPanelReference) {
		
		super();
		
		this.buttonPanelReference = buttonPanelReference;
		
		this.initWidget(uiBinder.createAndBindUi(this));
		
		if(text != null) {
			this.textArea.setText(text);
		} else {
			this.textArea.setText("enter Field Name");
		}
		
	}
	
	@UiHandler("textArea")
	void onKeyDown(KeyDownEvent event) {
		if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
			this.buttonPanelReference.clickOk();
	}
	
	public void selectEverything() {
		this.textArea.setSelectionRange(0, this.textArea.getText().length());
	}
	
	public String getText() {
		return this.textArea.getText();
	}
	
	public void setEnabled(boolean b) {
		this.textArea.setEnabled(b);
	}
	
}
