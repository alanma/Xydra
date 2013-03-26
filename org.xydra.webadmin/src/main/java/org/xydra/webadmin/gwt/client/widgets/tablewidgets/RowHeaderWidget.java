package org.xydra.webadmin.gwt.client.widgets.tablewidgets;

import org.xydra.base.XAddress;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.XyAdmin;
import org.xydra.webadmin.gwt.client.util.TableController.Status;
import org.xydra.webadmin.gwt.client.widgets.editorpanel.EditorPanelPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;


public class RowHeaderWidget extends Composite {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,RowHeaderWidget> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField
	Button expandButton;
	
	@UiField(provided = true)
	EntityWidget entityWidget;
	
	private Status status;
	
	private XAddress address;
	
	public RowHeaderWidget(EditorPanelPresenter presenter, XAddress address, Status status) {
		super();
		this.address = address;
		this.status = status;
		this.entityWidget = new EntityWidget(presenter, address, new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				RowHeaderWidget.this.expandButton.click();
			}
		});
		
		initWidget(uiBinder.createAndBindUi(this));
		
		if(this.status.equals(Status.Present)) {
			this.expandButton.setText("o p e n");
		} else if(this.status.equals(Status.Opened)) {
			this.expandButton.setText("c l o s e");
		}
		
	}
	
	@UiHandler("expandButton")
	void onClickExpand(ClickEvent e) {
		if(this.status.equals(Status.Present)) {
			this.status = Status.Opened;
			this.expandButton.setText("c l o s e");
		} else if(this.status.equals(Status.Opened)) {
			this.status = Status.Present;
			this.expandButton.setText("o p e n");
		}
		Controller.getInstance().notifyTableController(this.address, this.status);
	}
}
