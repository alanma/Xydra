package org.xydra.webadmin.gwt.client.widgets.dialogs;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;


public class ButtonPanel extends Composite {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,ButtonPanel> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	String inputString = "";
	
	@UiField
	HorizontalPanel buttonPanel;
	
	@UiField
	Button okButton;
	
	@UiField
	Button cancelButton;
	
	private Widget parentWidget;
	
	public ButtonPanel(ClickHandler handler, Widget parentWidget) {
		
		super();
		
		this.parentWidget = parentWidget;
		
		this.initWidget(uiBinder.createAndBindUi(this));
		
		this.okButton.addClickHandler(handler);
	}
	
	@UiHandler("cancelButton")
	void onClickRemove(ClickEvent event) {
		this.parentWidget.removeFromParent();
	}
	
	public void clickOk() {
		this.okButton.click();
		
	}
	
}
