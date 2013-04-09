package org.xydra.webadmin.gwt.client.widgets.editorpanel;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;


public class ModelControlPanel extends Composite {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,ModelControlPanel> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField
	HorizontalPanel mainPanel;
	
	@UiField
	Button loadAllObjectsButton;
	
	@UiField
	Button loadAllIDsButton;
	
	@UiField
	Button commitModelChangesButton;
	
	@UiField
	Button discardModelChangesButton;
	
	@UiField
	Button expandAllButton;
	
	private EditorPanelPresenter presenter;
	
	public ModelControlPanel(EditorPanelPresenter presenter) {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		this.presenter = presenter;
	}
	
	@UiHandler("loadAllIDsButton")
	void onClickFetchIDs(ClickEvent event) {
		this.presenter.handleFetchIDs();
	}
	
	@UiHandler("loadAllObjectsButton")
	void onClickFetchObjects(ClickEvent event) {
		this.presenter.loadModelsObjectsFromPersistence();
		
	}
	
	@UiHandler("commitModelChangesButton")
	public void onClickCommit(ClickEvent event) {
		this.presenter.openCommitDialog(this);
		
	}
	
	@UiHandler("discardModelChangesButton")
	public void onClickDiscard(ClickEvent event) {
		
		this.presenter.openDdiscardChangesDialog();
		
	}
	
	@UiHandler("expandAllButton")
	public void onClickExpand(ClickEvent event) {
		
		String expandButtonText = this.expandAllButton.getText();
		if(expandButtonText.equals("expand all objects")) {
			
			this.expandAllButton.setText("close all objects");
		} else {
			
			this.expandAllButton.setText("expand all objects");
		}
		
		this.presenter.expandAll(expandButtonText);
		
	}
}
