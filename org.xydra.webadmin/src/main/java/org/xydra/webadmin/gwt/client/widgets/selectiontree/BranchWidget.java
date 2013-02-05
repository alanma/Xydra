package org.xydra.webadmin.gwt.client.widgets.selectiontree;

import java.util.HashMap;
import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XType;
import org.xydra.base.XX;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.Observable;
import org.xydra.webadmin.gwt.client.widgets.dialogs.AddElementDialog;
import org.xydra.webadmin.gwt.client.widgets.dialogs.RemoveElementDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class BranchWidget extends Composite implements Observable {
	
	private static final Logger log = LoggerFactory.getLogger(BranchWidget.class);
	
	interface ViewUiBinder extends UiBinder<Widget,BranchWidget> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	private HashMap<XID,BranchWidget> existingBranches;
	private XAddress address;
	
	@UiField
	VerticalPanel branches;
	@UiField
	HorizontalPanel buttonPanel;
	@UiField
	Button expandButton;
	@UiField
	Anchor anchor;
	@UiField
	Button fetchModelsButton;
	@UiField
	Button addButton;
	@UiField
	Button removeModelButton;
	private boolean expanded = false;
	
	public BranchWidget(XAddress address) {
		this.address = address;
		
		this.buildComponents();
	}
	
	private void buildComponents() {
		
		initWidget(uiBinder.createAndBindUi(this));
		
		XID id = this.address.getModel();
		String plusButtonText = "add Object";
		
		if(this.address.getAddressedType().equals(XType.XREPOSITORY)) {
			id = this.address.getRepository();
			plusButtonText = "add Model";
			
			this.removeModelButton.removeFromParent();
		} else {
			this.expandButton.removeFromParent();
			this.fetchModelsButton.removeFromParent();
		}
		this.anchor.setText(id.toString());
		
		this.addButton.setText(plusButtonText);
		
	}
	
	@UiHandler("expandButton")
	void onClickExpand(ClickEvent event) {
		if(BranchWidget.this.expanded == false) {
			BranchWidget.this.expand();
		} else {
			BranchWidget.this.contract();
		}
	}
	
	@UiHandler("fetchModelsButton")
	void onClickFetch(ClickEvent event) {
		System.out.println("building branches!");
		Controller.getInstance().getIDsFromServer(BranchWidget.this.address);
	}
	
	@UiHandler("anchor")
	void onClickGet(ClickEvent event) {
		Controller.getInstance().getData(BranchWidget.this.address);
		
	}
	
	@UiHandler("addButton")
	void onClickAdd(ClickEvent event) {
		
		Controller.getInstance().getTempStorage().register(BranchWidget.this);
		AddElementDialog addDialog = new AddElementDialog(BranchWidget.this.address,
		        "enter Element name");
		addDialog.show();
		addDialog.selectEverything();
	}
	
	@UiHandler("removeModelButton")
	void onClick(ClickEvent event) {
		RemoveElementDialog removeDialog = new RemoveElementDialog(BranchWidget.this.address);
		removeDialog.show();
	}
	
	private void setComponents() {
		
		Iterator<XID> iterator = Controller.getInstance().getLocallyStoredIDs(this.address);
		
		while(iterator.hasNext()) {
			XID modelId = iterator.next();
			if(!this.existingBranches.keySet().contains(modelId)) {
				addBranch(modelId);
			}
		}
	}
	
	private void addBranch(XID modelId) {
		XAddress address = buildChildAddress(modelId);
		BranchWidget newBranch = new BranchWidget(address);
		this.branches.add(newBranch);
		
		this.existingBranches.put(modelId, newBranch);
	}
	
	private XAddress buildChildAddress(XID childID) {
		
		XAddress address = null;
		switch(this.address.getAddressedType()) {
		case XMODEL:
			address = XX.resolveObject(this.address, childID);
			break;
		case XREPOSITORY:
			address = XX.resolveModel(this.address, childID);
			break;
		default:
			break;
		}
		return address;
	}
	
	// // @Override
	// public void notifyMe(XAddress address, Iterator<XID> iterator) {
	// XID childID = address.getModel();
	// System.out.println("my address: " + this.address + ", other address: " +
	// address);
	// if(this.address.equals(address)) {
	// System.out.println("i contain the other address");
	// this.setComponents(iterator);
	// } else {
	// BranchWidget branch = this.existingBranches.get(childID);
	// branch.notifyMe(address, iterator);
	// }
	// }
	
	public void addElement(String id) {
		this.addBranch(XX.toId(id));
	}
	
	@Override
	public void notifyMe(XAddress address) {
		XID childID = address.getModel();
		if(this.address.equals(address)) {
			log.info("i am " + this.address.toString() + " and I contain the other address");
			this.contract();
			this.expand();
		} else {
			BranchWidget branch = this.existingBranches.get(childID);
			branch.notifyMe(address);
		}
		
	}
	
	private void expand() {
		BranchWidget.this.existingBranches = new HashMap<XID,BranchWidget>();
		System.out.println("request for " + BranchWidget.this.address.toString() + " received!");
		BranchWidget.this.expandButton.setText("-");
		
		this.setComponents();
		
		this.expanded = true;
		
	}
	
	private void contract() {
		BranchWidget.this.branches.clear();
		BranchWidget.this.existingBranches = null;
		BranchWidget.this.expandButton.setText("+");
		
		this.expanded = false;
	}
}
