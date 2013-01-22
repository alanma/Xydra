package org.xydra.webadmin.gwt.client.widgets.version2;

import org.xydra.base.XID;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;


public class InformationPanel extends Composite {
	
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,InformationPanel> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField
	Label information;
	
	@UiField
	HorizontalPanel tablePanel;
	
	public InformationPanel(XReadableModel result) {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		
		buildComponents(result);
	}
	
	private void buildComponents(XReadableModel result) {
		this.setTableData(result);
	}
	
	public void setTableData(XReadableModel model) {
		TableGenerator tableGenerator = new TableGenerator();
		for(XID xid : model) {
			FieldRow row = new FieldRow(xid, model.getObject(xid));
			tableGenerator.add(row);
		}
		this.tablePanel.add(tableGenerator.createTable());
		
	}
	
	public void notifyMe(XReadableModel model) {
		
	}
}
