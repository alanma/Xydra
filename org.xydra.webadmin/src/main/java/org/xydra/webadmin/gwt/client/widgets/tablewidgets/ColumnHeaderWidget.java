package org.xydra.webadmin.gwt.client.widgets.tablewidgets;

import org.xydra.base.XId;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;


/**
 * For column heads in the table
 * 
 * @author Andi
 * 
 */
public class ColumnHeaderWidget extends Composite {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,ColumnHeaderWidget> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField
	HTMLPanel panel;
	
	@UiField
	Label idLabel;
	
	public ColumnHeaderWidget(XId id) {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		
		this.idLabel.setText(id.toString());
		
	}
	
}
