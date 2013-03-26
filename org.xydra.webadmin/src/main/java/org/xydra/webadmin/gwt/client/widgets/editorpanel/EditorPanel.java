package org.xydra.webadmin.gwt.client.widgets.editorpanel;

import org.xydra.core.change.SessionCachedModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class EditorPanel extends Composite {
	
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,EditorPanel> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField
	VerticalPanel mainPanel;
	
	private EditorPanelPresenter presenter;
	
	public EditorPanel() {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		Controller.getInstance().registerEditorPanel(this);
		
		this.presenter = new EditorPanelPresenter();
	}
	
	public void notifyMe(SessionCachedModel result) {
		log.info("editorPanel notified!");
		this.mainPanel.clear();
		
		this.mainPanel.add(new ModelControlPanel(this.presenter));
		this.mainPanel.add(new ModelInformationPanel(this.presenter, result));
		
	}
}
