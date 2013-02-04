package org.xydra.webadmin.gwt.client.widgets.version2;

import org.xydra.base.XAddress;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class EmptyFieldWidget extends Composite {
	
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,EmptyFieldWidget> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField
	VerticalPanel mainPanel;
	
	@UiField
	Button addButton;
	
	private XAddress address;
	
	public EmptyFieldWidget(XAddress address) {
		super();
		this.address = address;
		initWidget(uiBinder.createAndBindUi(this));
		
		this.addButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				AddEditorDialog addDialog = new AddEditorDialog(EmptyFieldWidget.this.address);
				addDialog.show();
				
			}
		});
	}
	
}
