package org.xydra.webadmin.gwt.client.widgets;

import org.xydra.base.XID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;


public class ObjectWidget extends Composite {
	
	interface ViewUiBinder extends UiBinder<Widget,ObjectWidget> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@SuppressWarnings("unused")
	private XID objectID;
	@SuppressWarnings("unused")
	private long revisionNumber;
	
	@UiField
	public FlowPanel infoPanel;
	@UiField
	public Label idLabel;
	@UiField
	public Label revisionLabel;
	@UiField
	public FlowPanel fieldPanel;
	
	public ObjectWidget(XID objectID, long revisionNumber) {
		super();
		
		initWidget(uiBinder.createAndBindUi(this));
		
		this.objectID = objectID;
		this.revisionNumber = revisionNumber;
		
		this.idLabel.setText(objectID.toString());
		this.revisionLabel.setText("Revision: " + revisionNumber);
		
	}
	
	public void add(Widget widget) {
		this.fieldPanel.add(widget);
	}
	
}
