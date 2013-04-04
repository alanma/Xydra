package org.xydra.webadmin.gwt.client.widgets.dialogs;

import org.xydra.base.XAddress;
import org.xydra.base.value.XValue;
import org.xydra.core.XX;
import org.xydra.gwt.editor.value.XIdEditor;
import org.xydra.gwt.editor.value.XValueEditor.EditListener;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.util.Presenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class AddElementDialog extends DialogBox {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AddElementDialog.class);
	
	interface ViewUiBinder extends UiBinder<Widget,AddElementDialog> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	String inputString = "";
	
	@UiField
	VerticalPanel mainPanel;
	
	@UiField(provided = true)
	XIdEditor textArea;
	
	@UiField
	Label errorLabel;
	
	@UiField(provided = true)
	ButtonPanel buttonPanel;
	
	private XAddress address;
	
	public AddElementDialog(final Presenter presenter, XAddress address, String text) {
		
		super();
		
		this.address = address;
		
		ClickHandler okHandler = new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				String value = "";
				try {
					value = AddElementDialog.this.textArea.getValue().toString();
					Presenter.processUserInput(AddElementDialog.this.address, value);
					AddElementDialog.this.removeFromParent();
				} catch(Exception e) {
					
					AddElementDialog.this.errorLabel.setText(e.getMessage());
				}
				
			}
		};
		
		this.buttonPanel = new ButtonPanel(okHandler, this);
		this.textArea = new XIdEditor(XX.toId("newID"), new EditListener() {
			
			@Override
			public void newValue(XValue value) {
				// nothing
				
			}
		});
		this.textArea.getTextBox().addKeyDownHandler(new KeyDownHandler() {
			
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
					AddElementDialog.this.buttonPanel.clickOk();
			}
			
		});
		setWidget(uiBinder.createAndBindUi(this));
		
		this.setStyleName("dialogStyle");
		
		String[] addressedSuccessors = { "nothing", "Object", "Field", "Model" };
		String dialogTitle = addressedSuccessors[address.getAddressedType().ordinal()];
		if(address.getRepository().equals(XX.toId("noRepo")))
			dialogTitle = "Repository";
		this.setText("add " + dialogTitle);
		this.center();
	}
	
	public void selectEverything() {
		this.textArea.selectEverything();
	}
	
}
