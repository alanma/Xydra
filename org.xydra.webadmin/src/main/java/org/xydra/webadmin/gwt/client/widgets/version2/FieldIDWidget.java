package org.xydra.webadmin.gwt.client.widgets.version2;

import org.xydra.base.XID;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class FieldIDWidget extends Composite {
	
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,FieldIDWidget> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField
	VerticalPanel panel;
	
	@UiField
	Label idLabel;
	
	public FieldIDWidget(XID id) {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		
		this.idLabel.setText(id.toString());
		
	}
	
}
