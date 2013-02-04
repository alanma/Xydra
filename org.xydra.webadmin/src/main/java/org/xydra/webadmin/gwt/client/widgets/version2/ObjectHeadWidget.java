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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class ObjectHeadWidget extends Composite {
	
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,ObjectHeadWidget> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField
	VerticalPanel panel;
	
	@UiField
	Label idLabel;
	
	@UiField
	Label revisionLabel;
	
	@UiField
	Button removeObjectButton;
	
	@UiField
	Button addFieldButton;
	
	private XAddress address;
	
	public ObjectHeadWidget(XAddress address, long revisionNumber) {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		
		this.address = address;
		
		this.idLabel.setText(address.getObject().toString());
		
		this.revisionLabel.setText("" + revisionNumber);
		
		this.removeObjectButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				RemoveDialog removeDialog = new RemoveDialog(ObjectHeadWidget.this.address);
				removeDialog.show();
				
			}
		});
		
		this.addFieldButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				AddEditorDialog addDialog = new AddEditorDialog(ObjectHeadWidget.this.address);
				addDialog.show();
				
			}
		});
		
	}
}
