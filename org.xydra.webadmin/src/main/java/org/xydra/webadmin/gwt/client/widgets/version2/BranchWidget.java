package org.xydra.webadmin.gwt.client.widgets.version2;

import java.util.HashMap;
import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.Observable;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;


public class BranchWidget extends VerticalPanel implements Observable {
	
	private BranchTypes type;
	private XID id;
	private HashMap<XID,BranchWidget> existingBranches;
	private VerticalPanel branches;
	private XAddress address;
	
	private Button expandButton;
	
	public BranchWidget(XAddress address) {
		this.address = address;
		getIDFromAddress(address);
		
		this.buildComponents();
	}
	
	private void buildComponents() {
		HorizontalPanel buttonPanel = new HorizontalPanel();
		this.add(buttonPanel);
		
		{
			if(!this.type.equals(BranchTypes.OBJECT)) {
				this.expandButton = new Button("+");
				this.expandButton.setStyleName("treeItem", true);
				this.expandButton.addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						if(BranchWidget.this.existingBranches == null) {
							System.out.println("building branches!");
							BranchWidget.this.existingBranches = new HashMap<XID,BranchWidget>();
							System.out.println("request for "
							        + BranchWidget.this.address.toString() + " received!");
							Controller.getInstance().getIDs(BranchWidget.this.address);
							BranchWidget.this.expandButton.setText("-");
						} else {
							BranchWidget.this.branches.clear();
							BranchWidget.this.existingBranches = null;
							BranchWidget.this.expandButton.setText("+");
						}
					}
				});
				buttonPanel.add(this.expandButton);
			}
			
			Anchor anchor = new Anchor(this.id.toString());
			anchor.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					Controller.getInstance().getData(BranchWidget.this.address);
					
				}
			});
			buttonPanel.add(anchor);
			Button addButton = new Button(this.type.plusButtonText);
			addButton.addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					
					Controller.getInstance().getTempStorage().register(BranchWidget.this);
					AddDialog addDialog = new AddDialog(BranchWidget.this.type.addWidgetText);
					addDialog.show();
					addDialog.selectEverything();
				}
			});
			buttonPanel.add(addButton);
			
			String deleteButtonText = BranchWidget.this.type.deleteButtonText;
			if(deleteButtonText != null) {
				Button removeModelButton = new Button(deleteButtonText);
				removeModelButton.addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						BranchWidget.this.removeFromParent();
					}
				});
				buttonPanel.add(removeModelButton);
			}
		}
		this.branches = new VerticalPanel();
		this.add(this.branches);
	}
	
	private void setComponents(Iterator<XID> iterator) {
		System.out.println("adding components");
		
		while(iterator.hasNext()) {
			XID modelId = iterator.next();
			addBranch(modelId);
		}
	}
	
	private void addBranch(XID modelId) {
		XAddress address = buildChildAddress(modelId);
		BranchWidget newBranch = new BranchWidget(address);
		this.branches.add(newBranch);
		
		this.existingBranches.put(modelId, newBranch);
	}
	
	private XAddress buildChildAddress(XID childID) {
		
		XID repoID = this.address.getRepository();
		XID modelID = this.address.getModel();
		XID objectID = this.address.getObject();
		
		XAddress address = null;
		if(modelID == null) {
			address = XX.toAddress(repoID, childID, null, null);
		} else if(objectID == null) {
			address = XX.toAddress(repoID, modelID, childID, null);
		}
		return address;
	}
	
	// @Override
	public void notifyMe(XAddress address, Iterator<XID> iterator) {
		XID childID = address.getModel();
		System.out.println("my address: " + this.address + ", other address: " + address);
		if(this.address.equals(address)) {
			System.out.println("i contain the other address");
			this.setComponents(iterator);
		} else {
			BranchWidget branch = this.existingBranches.get(childID);
			branch.notifyMe(address, iterator);
		}
	}
	
	private void getIDFromAddress(XAddress address) {
		
		XID repoID = address.getRepository();
		XID modelID = address.getModel();
		XID objectID = address.getObject();
		
		if(modelID == null) {
			this.id = repoID;
			this.type = BranchTypes.REPO;
		} else if(objectID == null) {
			this.id = modelID;
			this.type = BranchTypes.MODEL;
		} else {
			this.id = objectID;
			this.type = BranchTypes.OBJECT;
		}
	}
	
	public void addElement(String id) {
		this.addBranch(XX.toId(id));
	}
}
