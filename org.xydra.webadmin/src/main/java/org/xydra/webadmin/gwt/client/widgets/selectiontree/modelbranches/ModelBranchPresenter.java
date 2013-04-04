package org.xydra.webadmin.gwt.client.widgets.selectiontree.modelbranches;

import org.xydra.base.XAddress;
import org.xydra.webadmin.gwt.client.EventHelper;
import org.xydra.webadmin.gwt.client.events.EntityStatus;
import org.xydra.webadmin.gwt.client.events.ModelChangedEvent;
import org.xydra.webadmin.gwt.client.events.ModelChangedEvent.IModelChangedEventHandler;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.selectiontree.SelectionTreePresenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;


public class ModelBranchPresenter extends SelectionTreePresenter {
	
	private XAddress modelAddress;
	private IModelBranchWidget widget;
	boolean expanded = false;
	
	public ModelBranchPresenter(XAddress address, IModelBranchWidget modelBranchWidget) {
		this.modelAddress = address;
		this.widget = modelBranchWidget;
		buildWidget();
		
		EventHelper.addModelChangeListener(address, new IModelChangedEventHandler() {
			
			@Override
			public void onModelChange(ModelChangedEvent event) {
				ModelBranchPresenter.this.processChanges(event.getModelAddress(), event.getStatus());
				
			}
		});
	}
	
	protected void processChanges(XAddress modelAddress, EntityStatus status) {
		switch(status) {
		case CHANGED:
			// TODO check, when this happens
			break;
		case DELETED:
			this.widget.delete();
			break;
		case INDEXED:
			long revisionNumber = XyAdmin.getInstance().getModel()
			        .getRepo(modelAddress.getRepository()).getModel(modelAddress.getModel())
			        .getRevisionNumber();
			this.widget.setRevisionNumber(revisionNumber);
			break;
		case EXTENDED:
			this.presentModel();
			break;
		default:
			break;
		
		}
		
	}
	
	private void buildWidget() {
		
		ClickHandler anchorClickHandler = new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				ModelBranchPresenter.this.presentModel();
			}
		};
		
		this.widget.init(this.modelAddress, anchorClickHandler);
		this.checkStatus();
		
	}
	
	void updateViewModel() {
		if(this.expanded) {
			XyAdmin.getInstance().getViewModel().closeLocation(this.modelAddress);
		} else {
			XyAdmin.getInstance().getViewModel().openLocation(this.modelAddress);
		}
	}
	
	public void openAddElementDialog(String string) {
		super.openAddElementDialog(this.modelAddress, string);
		
	}
	
	private void checkStatus() {
		if(XyAdmin.getInstance().getModel().getRepo(this.modelAddress.getRepository())
		        .isNotExisting(this.modelAddress.getModel())) {
			this.widget.setStatusDeleted();
		}
		if(!XyAdmin.getInstance().getModel().getRepo(this.modelAddress.getRepository())
		        .isAddedModel(this.modelAddress.getModel())) {
			if(!XyAdmin.getInstance().getModel().getRepo(this.modelAddress.getRepository())
			        .getModel(this.modelAddress.getModel()).knowsAllObjects()) {
				
				this.widget.setRevisionUnknown();
			}
		}
	}
	
	public void presentModel() {
		XyAdmin.getInstance().getController().presentModel(this.modelAddress);
		
	}
	
}
