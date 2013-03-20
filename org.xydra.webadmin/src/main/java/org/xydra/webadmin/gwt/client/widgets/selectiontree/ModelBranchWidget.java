package org.xydra.webadmin.gwt.client.widgets.selectiontree;

import org.xydra.base.XAddress;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.widgets.tablewidgets.EntityWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;


public class ModelBranchWidget extends Composite {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ModelBranchWidget.class);
	
	interface ViewUiBinder extends UiBinder<Widget,ModelBranchWidget> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	private XAddress address;
	
	@UiField(provided = true)
	EntityWidget entityWidget;
	
	public ModelBranchWidget(XAddress address) {
		
		this.address = address;
		buildComponents(address);
		initWidget(uiBinder.createAndBindUi(this));
		
	}
	
	public void notifyMe(XAddress address) {
		buildComponents(address);
	}
	
	private void buildComponents(XAddress address) {
		this.entityWidget = new EntityWidget(address, new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				Controller.getInstance().getData(ModelBranchWidget.this.address);
			}
		});
		
		if(Controller.getInstance().getDataModel().getRepo(address.getRepository())
		        .isNotExisting(address.getModel())) {
			this.entityWidget.setStatusDeleted();
		}
		if(!Controller.getInstance().getDataModel().getRepo(address.getRepository())
		        .isAddedModel(address.getModel())) {
			if(!Controller.getInstance().getDataModel().getRepo(address.getRepository())
			        .getModel(address.getModel()).knowsAllObjects()) {
				
				this.entityWidget.setRevisionUnknown();
			}
		}
		
		this.entityWidget.setDeleteModelDialog();
	}
	
	public void open(XAddress address2) {
		// Controller.getInstance().getData(ModelBranchWidget.this.address);
		
	}
}
