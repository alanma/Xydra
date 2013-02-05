package org.xydra.webadmin.gwt.client.widgets.editorpanel;

import org.xydra.base.XID;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.XyAdmin;
import org.xydra.webadmin.gwt.client.datatypes.FieldRow;
import org.xydra.webadmin.gwt.client.util.TableGenerator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;


public class ModelInformationPanel extends Composite {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,ModelInformationPanel> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField
	Label information;
	
	@UiField
	HorizontalPanel tablePanel;
	
	public ModelInformationPanel(XReadableModel result) {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		
		buildComponents(result);
	}
	
	private void buildComponents(XReadableModel result) {
		this.setTableData(result);
		
		this.information.setText("derzeit lokal geladene Objekte im model "
		        + result.getId().toString() + ": ");
	}
	
	public void setTableData(XReadableModel model) {
		if(!model.isEmpty()) {
			TableGenerator tableGenerator = new TableGenerator();
			for(XID xid : model) {
				FieldRow row = new FieldRow(xid, model.getObject(xid));
				tableGenerator.add(row);
			}
			this.tablePanel.add(tableGenerator.createTable());
		} else {
			Label noObjectsLabel = new Label("derzeit keine Objekte vorhanden!");
			noObjectsLabel.setStyleName("noData");
			this.tablePanel.add(noObjectsLabel);
		}
	}
	
	public void notifyMe(XReadableModel model) {
		
	}
}
