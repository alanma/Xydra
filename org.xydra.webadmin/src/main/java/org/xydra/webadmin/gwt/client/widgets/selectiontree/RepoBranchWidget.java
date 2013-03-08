package org.xydra.webadmin.gwt.client.widgets.selectiontree;

import java.util.HashMap;
import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XX;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.Observable;
import org.xydra.webadmin.gwt.client.widgets.dialogs.AddElementDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class RepoBranchWidget extends Composite implements Observable {
	
	private static final Logger log = LoggerFactory.getLogger(RepoBranchWidget.class);
	
	interface ViewUiBinder extends UiBinder<Widget,RepoBranchWidget> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	private HashMap<XId,ModelBranchWidget> existingBranches;
	private XAddress address;
	
	@UiField
	VerticalPanel mainPanel;
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
	private boolean expanded = false;
	
	public RepoBranchWidget(XAddress address) {
		this.address = address;
		
		this.buildComponents();
	}
	
	private void buildComponents() {
		
		initWidget(uiBinder.createAndBindUi(this));
		
		XId id = this.address.getRepository();
		String plusButtonText = "add Model";
		
		this.mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		this.mainPanel.addStyleName("repoBranchBorder");
		
		this.anchor.setText(id.toString());
		
		this.addButton.setText(plusButtonText);
		
		this.mainPanel
		        .setCellHorizontalAlignment(this.branches, HasHorizontalAlignment.ALIGN_RIGHT);
		
	}
	
	@UiHandler("expandButton")
	void onClickExpand(ClickEvent event) {
		if(RepoBranchWidget.this.expanded == false) {
			RepoBranchWidget.this.expand();
		} else {
			RepoBranchWidget.this.contract();
		}
	}
	
	@UiHandler("fetchModelsButton")
	void onClickFetch(ClickEvent event) {
		System.out.println("building branches!");
		Controller.getInstance().getIDsFromServer(RepoBranchWidget.this.address);
	}
	
	@UiHandler("anchor")
	void onClickGet(ClickEvent event) {
		Controller.getInstance().getData(RepoBranchWidget.this.address);
		
	}
	
	@UiHandler("addButton")
	void onClickAdd(ClickEvent event) {
		
		Controller.getInstance().getTempStorage().register(RepoBranchWidget.this);
		AddElementDialog addDialog = new AddElementDialog(RepoBranchWidget.this.address,
		        "enter Element name");
		addDialog.show();
		addDialog.selectEverything();
	}
	
	private void setComponents() {
		
		Iterator<XId> iterator = Controller.getInstance().getLocallyStoredIDs(this.address);
		
		while(iterator.hasNext()) {
			XId modelId = iterator.next();
			if(!this.existingBranches.keySet().contains(modelId)) {
				addBranch(modelId);
			}
		}
	}
	
	private void addBranch(XId modelId) {
		if(this.branches.getWidgetCount() == 0) {
			this.buttonPanel.getElement().setAttribute("style",
			        "border-bottom: 1px solid #009; margin-bottom: 5px");
		}
		XAddress address = buildChildAddress(modelId);
		ModelBranchWidget newBranch = new ModelBranchWidget(address);
		this.branches.add(newBranch);
		Controller.getInstance().getDataModel().getRepo(this.address.getRepository())
		        .getModel(modelId).getRevisionNumber();
		
		this.existingBranches.put(modelId, newBranch);
	}
	
	private XAddress buildChildAddress(XId childID) {
		
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
		log.info("i am " + this.address.toString() + " and I contain the other address");
		this.contract();
		this.expand();
		
	}
	
	private void expand() {
		RepoBranchWidget.this.existingBranches = new HashMap<XId,ModelBranchWidget>();
		System.out
		        .println("request for " + RepoBranchWidget.this.address.toString() + " received!");
		RepoBranchWidget.this.expandButton.setText("-");
		
		this.setComponents();
		
		this.expanded = true;
		
	}
	
	private void contract() {
		RepoBranchWidget.this.branches.clear();
		RepoBranchWidget.this.existingBranches = null;
		RepoBranchWidget.this.expandButton.setText("+");
		
		this.expanded = false;
	}
}
