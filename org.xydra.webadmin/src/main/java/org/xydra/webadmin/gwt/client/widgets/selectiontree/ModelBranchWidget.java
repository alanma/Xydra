package org.xydra.webadmin.gwt.client.widgets.selectiontree;

import org.xydra.base.XAddress;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.EventHelper;
import org.xydra.webadmin.gwt.client.events.EntityStatus;
import org.xydra.webadmin.gwt.client.events.ModelChangedEvent;
import org.xydra.webadmin.gwt.client.events.ModelChangedEvent.IModelChangedEventHandler;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
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
	private SelectionTreePresenter presenter;
	
	public ModelBranchWidget(XAddress address) {
		
		this.address = address;
		// this.presenter = presenter;
		buildComponents(address);
		initWidget(uiBinder.createAndBindUi(this));
		
		checkStatus();
		
		EventHelper.addModelChangeListener(address, new IModelChangedEventHandler() {
			
			@Override
			public void onModelChange(ModelChangedEvent event) {
				ModelBranchWidget.this.processChanges(event.getModelAddress(), event.getStatus());
				
			}
		});
	}
	
	protected void processChanges(XAddress modelAddress, EntityStatus status) {
		switch(status) {
		case CHANGED:
			this.buildComponents(modelAddress);
			break;
		case DELETED:
			this.removeFromParent();
			break;
		case INDEXED:
			long modelsRevisionNumber = this.presenter.getModelsRevisionNumber(this.address);
			this.entityWidget.setRevisionNumber(modelsRevisionNumber);
			break;
		case EXTENDED:
			this.presenter.presentModel(this.address);
			break;
		default:
			break;
		
		}
		
	}
	
	public void notifyMe(XAddress address) {
		buildComponents(address);
	}
	
	private void buildComponents(XAddress address) {
		this.entityWidget = new EntityWidget(this.presenter, this.address, new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				// ModelBranchWidget.this.presenter.presentModel(ModelBranchWidget.this.address);
				ModelBranchWidget.this.presenter.presentModel(ModelBranchWidget.this.address);
			}
		});
		
		checkStatus();
		
		this.entityWidget.setDeleteModelDialog();
	}
	
	private void checkStatus() {
		if(XyAdmin.getInstance().getModel().getRepo(this.address.getRepository())
		        .isNotExisting(this.address.getModel())) {
			this.entityWidget.setStatusDeleted();
		}
		if(!XyAdmin.getInstance().getModel().getRepo(this.address.getRepository())
		        .isAddedModel(this.address.getModel())) {
			if(!XyAdmin.getInstance().getModel().getRepo(this.address.getRepository())
			        .getModel(this.address.getModel()).knowsAllObjects()) {
				
				this.entityWidget.setRevisionUnknown();
			}
		}
	}
	
	public void open(XAddress address2) {
		// Controller.getInstance().getData(ModelBranchWidget.this.address);
		
	}
}
