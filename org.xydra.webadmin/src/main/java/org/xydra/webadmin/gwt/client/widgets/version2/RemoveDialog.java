package org.xydra.webadmin.gwt.client.widgets.version2;

import org.xydra.base.XAddress;
import org.xydra.webadmin.gwt.client.Controller;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;


public class RemoveDialog extends DialogBox {
	
	HorizontalPanel dialogPanel;
	Button okButton;
	Button wrongButton;
	
	public RemoveDialog(final XAddress address) {
		this.setPopupPosition(200, 500);
		this.dialogPanel = new HorizontalPanel();
		this.dialogPanel.add(new Label("Are you sure you want to delete the item "
		        + address.toString() + "?"));
		this.add(this.dialogPanel);
		this.okButton = new Button("OK");
		
		this.okButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				RemoveDialog.this.removeFromParent();
				Controller.getInstance().getTempStorage().remove(address);
			}
		});
		
		this.wrongButton = new Button("abort");
		
		this.wrongButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				RemoveDialog.this.removeFromParent();
				
			}
		});
		this.dialogPanel.add(this.okButton);
		this.dialogPanel.add(this.wrongButton);
	}
	
}
