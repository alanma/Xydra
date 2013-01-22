package org.xydra.webadmin.gwt.client.widgets.version2;

import org.xydra.base.rmof.XReadableModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class ModelPane extends Composite {
	
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,ModelPane> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField
	VerticalPanel mainPanel;
	
	public ModelPane(XReadableModel result) {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		
		buildComponents(result);
	}
	
	private void buildComponents(XReadableModel result) {
		
		this.mainPanel.add(new ModelControlPanel());
		this.mainPanel.add(new InformationPanel(result));
		
	}
}
