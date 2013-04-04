package org.xydra.webadmin.gwt.client.widgets.selectiontree.repobranches;

import java.util.HashMap;
import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.core.XX;
import org.xydra.webadmin.gwt.client.EventHelper;
import org.xydra.webadmin.gwt.client.events.EntityStatus;
import org.xydra.webadmin.gwt.client.events.RepoChangedEvent;
import org.xydra.webadmin.gwt.client.events.RepoChangedEvent.IRepoChangedEventHandler;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.selectiontree.ModelBranchWidget;
import org.xydra.webadmin.gwt.client.widgets.selectiontree.SelectionTreePresenter;


public class RepoBranchPresenter extends SelectionTreePresenter {
	
	private XAddress repoAddress;
	private HashMap<XId,ModelBranchWidget> existingBranches;
	private IRepoBranchWidget widget;
	boolean expanded = false;
	
	public RepoBranchPresenter(XAddress address, RepoBranchWidget repoBranchWidget) {
		this.repoAddress = address;
		this.widget = repoBranchWidget;
		build();
		
		EventHelper.addRepoChangeListener(address, new IRepoChangedEventHandler() {
			
			public void onRepoChange(RepoChangedEvent event) {
				processRepoDataChanges(event.getStatus());
				
			}
			
		});
		
	}
	
	private void processRepoDataChanges(EntityStatus status) {
		if(status.equals(EntityStatus.REGISTERED)) {
			this.collapse();
			this.expand();
		}
		
	}
	
	private void build() {
		
		this.widget.init();
		
		XId id = this.repoAddress.getRepository();
		this.widget.setAnchorText(id.toString());
		
		Iterator<XId> iterator = XyAdmin.getInstance().getModel()
		        .getLocallyStoredModelIDs(this.repoAddress);
		
		buildModelBranches(iterator);
		
	}
	
	private void buildModelBranches(Iterator<XId> iterator) {
		
		while(iterator.hasNext()) {
			XId modelId = iterator.next();
			if(!this.existingBranches.keySet().contains(modelId)) {
				
				XAddress modelAddress = XX.resolveModel(this.repoAddress, modelId);
				ModelBranchWidget newBranch = new ModelBranchWidget(modelAddress);
				this.widget.addBranch(newBranch);
				// XyAdmin.getInstance().getModel().getRepo(this.repoAddress.getRepository())
				// .getModel(modelId).getRevisionNumber();
				
				this.existingBranches.put(modelId, newBranch);
			}
		}
	}
	
	void handleExpand(IRepoBranchWidget repoBranchWidget) {
		updateViewModel();
		if(this.expanded) {
			collapse();
		} else {
			expand();
		}
	}
	
	void expand() {
		this.existingBranches = new HashMap<XId,ModelBranchWidget>();
		this.build();
		this.widget.setExpandButtonText("-");
		this.expanded = true;
		
	}
	
	void collapse() {
		this.widget.clearBranches();
		this.existingBranches = null;
		this.widget.setExpandButtonText("+");
		
		this.expanded = false;
	}
	
	void fetchModels() {
		XyAdmin.getInstance().getController().fetchModelIds(this.repoAddress, null);
		this.collapse();
		updateViewModel();
	}
	
	void updateViewModel() {
		if(this.expanded) {
			XyAdmin.getInstance().getViewModel().closeLocation(this.repoAddress);
		} else {
			XyAdmin.getInstance().getViewModel().openLocation(this.repoAddress);
		}
	}
	
	public void openAddElementDialog(String string) {
		super.openAddElementDialog(this.repoAddress, string);
		
	}
}
