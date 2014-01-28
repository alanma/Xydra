package org.xydra.webadmin.gwt.client.widgets.dialogs;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.editorpanel.EditorPanelPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class ConfirmationDialog extends DialogBox {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,ConfirmationDialog> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	String inputString = "";
	
	@UiField
	VerticalPanel mainPanel;
	
	@UiField(provided = true)
	ButtonPanel buttonPanel;
	
	public ConfirmationDialog(final EditorPanelPresenter editorPanelPresenter, String text) {
		
		super();
		
		this.setText(text);
		
		ClickHandler okHandler = new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				editorPanelPresenter.discardChanges();
				ConfirmationDialog.this.removeFromParent();
			}
		};
		
		this.buttonPanel = new ButtonPanel(okHandler, this);
		
		setWidget(uiBinder.createAndBindUi(this));
		
		this.setStyleName("dialogStyle");
		this.center();
	}
	
}
