package org.xydra.webadmin.gwt.client.widgets.editorpanel;

import org.xydra.core.change.SessionCachedModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.util.TableController;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class ModelInformationPanel extends Composite {
	
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,ModelInformationPanel> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField
	Label information;
	
	@UiField
	HTMLPanel tablePanel;
	
	private EditorPanelPresenter presenter;
	
	public ModelInformationPanel(EditorPanelPresenter presenter, SessionCachedModel result) {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		this.presenter = presenter;
		buildComponents(result);
		
	}
	
	private void buildComponents(SessionCachedModel result) {
		this.setTableData(result);
		
		this.information.setText("currently locally loaded objects in model "
		        + result.getId().toString() + ": ");
	}
	
	public void setTableData(SessionCachedModel model) {
		if(!model.isEmpty()) {
			TableController tableController = new TableController(this.presenter);
			this.tablePanel.add(tableController.createTable(model));
			log.info("new table controller created!");
		} else {
			Label noObjectsLabel = new Label("no Objects present!");
			VerticalPanel dummyPanel = new VerticalPanel();
			dummyPanel.setStyleName("noData");
			dummyPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			dummyPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
			dummyPanel.add(noObjectsLabel);
			this.tablePanel.add(dummyPanel);
		}
	}
	
	public void notifyMe(SessionCachedModel model) {
		
	}
}
