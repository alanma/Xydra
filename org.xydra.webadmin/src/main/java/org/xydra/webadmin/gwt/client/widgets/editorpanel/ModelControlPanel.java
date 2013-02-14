package org.xydra.webadmin.gwt.client.widgets.editorpanel;

import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.dialogs.CommittingDialog;
import org.xydra.webadmin.gwt.client.widgets.dialogs.ConfirmationDialog;
import org.xydra.webadmin.gwt.client.widgets.dialogs.NYIDialog;

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
	
	public ModelControlPanel() {
		super();
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@UiHandler("loadAllIDsButton")
	void onClickFetchIDs(ClickEvent event) {
		// Controller.getInstance().loadCurrentModelsIDs();
		NYIDialog dialog = new NYIDialog();
		dialog.show();
	}
	
	@UiHandler("loadAllObjectsButton")
	void onClickFetchObjects(ClickEvent event) {
		Controller.getInstance().loadCurrentModelsObjects();
		
	}
	
	@UiHandler("commitModelChangesButton")
	public void onClickCommit(ClickEvent event) {
		CommittingDialog committingDialog = new CommittingDialog();
		committingDialog.show();
		
	}
	
	@UiHandler("discardModelChangesButton")
	public void onClickDiscard(ClickEvent event) {
		
		ConfirmationDialog dialog = new ConfirmationDialog("discard all Changes");
		
	}
}
