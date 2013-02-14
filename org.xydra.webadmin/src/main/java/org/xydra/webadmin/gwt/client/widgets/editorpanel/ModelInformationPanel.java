package org.xydra.webadmin.gwt.client.widgets.editorpanel;

import org.xydra.core.change.SessionCachedModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.XyAdmin;
import org.xydra.webadmin.gwt.client.util.TableController;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
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
	ScrollPanel tablePanel;
	
	public ModelInformationPanel(SessionCachedModel result) {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		
		buildComponents(result);
		
	}
	
	private void buildComponents(SessionCachedModel result) {
		this.setTableData(result);
		
		this.information.setText("derzeit lokal geladene Objekte im model "
		        + result.getId().toString() + ": ");
	}
	
	public void setTableData(SessionCachedModel model) {
		if(!model.isEmpty()) {
			TableController tableController = new TableController();
			this.tablePanel.add(tableController.createTable(model));
			log.info("new table controller created!");
		} else {
			Label noObjectsLabel = new Label("derzeit keine Objekte vorhanden!");
			noObjectsLabel.setStyleName("noData");
			this.tablePanel.add(noObjectsLabel);
		}
	}
	
	public void notifyMe(SessionCachedModel model) {
		
	}
}
