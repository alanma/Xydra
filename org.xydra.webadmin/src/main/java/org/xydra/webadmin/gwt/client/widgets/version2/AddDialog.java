package org.xydra.webadmin.gwt.client.widgets.version2;

import org.xydra.webadmin.gwt.client.Controller;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;


public class AddDialog extends DialogBox {
	
	String inputString = "";
	HorizontalPanel dialogPanel;
	TextBox textArea;
	Button okButton;
	
	public AddDialog(String text) {
		this.setPopupPosition(200, 500);
		this.dialogPanel = new HorizontalPanel();
		this.textArea = new TextBox();
		this.textArea.addKeyDownHandler(new KeyDownHandler() {
			
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
					AddDialog.this.okButton.click();
			}
			
		});
		this.textArea.setText(text);
		this.dialogPanel.add(this.textArea);
		this.add(this.dialogPanel);
		this.okButton = new Button("OK");
		
		this.okButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				AddDialog.this.removeFromParent();
				Controller.getInstance().getTempStorage()
				        .setInformation(AddDialog.this.textArea.getText());
			}
		});
		this.dialogPanel.add(this.okButton);
	}
	
	public void selectEverything() {
		this.textArea.setSelectionRange(0, this.textArea.getText().length());
	}
	
}
