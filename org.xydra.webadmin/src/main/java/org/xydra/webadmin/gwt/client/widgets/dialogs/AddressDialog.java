package org.xydra.webadmin.gwt.client.widgets.dialogs;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class AddressDialog extends DialogBox {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AddressDialog.class);
	
	interface ViewUiBinder extends UiBinder<Widget,AddressDialog> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	String inputString = "";
	
	@UiField
	VerticalPanel mainPanel;
	
	@UiField
	TextArea textArea;
	
	@UiField
	Button closeButton;
	
	public AddressDialog(String entityIdText, String addressText) {
		
		super();
		
		setWidget(uiBinder.createAndBindUi(this));
		this.textArea.addKeyDownHandler(new KeyDownHandler() {
			
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
					AddressDialog.this.close();
			}
			
		});
		
		this.setStyleName("dialogStyle");
		
		this.setText("address for " + entityIdText);
		// this.textArea.setReadOnly(true);
		this.textArea.setVisibleLines(1);
		this.textArea.setText(addressText);
		this.center();
	}
	
	public void selectEverything() {
		this.textArea.setSelectionRange(0, this.textArea.getText().length());
	}
	
	@UiHandler("closeButton")
	void onClickClose(ClickEvent event) {
		close();
	}
	
	private void close() {
		this.removeFromParent();
	}
	
}
