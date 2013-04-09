package org.xydra.webadmin.gwt.client.widgets.editorpanel;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
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
	HTMLPanel tablePanel;
	
	public ModelInformationPanel(String modelId) {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		
		this.information.setText("currently locally loaded objects in model " + modelId + ": ");
		
	}
	
	public void clear() {
		this.tablePanel.clear();
		
	}
	
	public void setData(VerticalPanel tableWidget) {
		this.tablePanel.add(tableWidget);
	}
	
}
