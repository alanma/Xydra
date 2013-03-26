package org.xydra.webadmin.gwt.client.widgets.selectiontree;

import org.xydra.base.XX;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.dialogs.AddElementDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class AddRepoWidget extends Composite {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AddRepoWidget.class);
	
	interface ViewUiBinder extends UiBinder<Widget,AddRepoWidget> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField
	VerticalPanel mainPanel;
	@UiField
	Button addRepoButton;
	
	private SelectionTreePresenter presenter;
	
	public AddRepoWidget(SelectionTreePresenter presenter) {
		
		this.presenter = presenter;
		this.buildComponents();
	}
	
	private void buildComponents() {
		
		initWidget(uiBinder.createAndBindUi(this));
		
	}
	
	@UiHandler("addRepoButton")
	void onClickAdd(ClickEvent event) {
		
		AddElementDialog addDialog = new AddElementDialog(this.presenter, XX.toAddress("/noRepo"),
		        "enter Repository ID");
		addDialog.show();
		addDialog.selectEverything();
	}
}
