package org.xydra.webadmin.gwt.client.widgets.selectiontree;

import java.util.HashMap;
import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XX;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.ViewModel;
import org.xydra.webadmin.gwt.client.datamodels.DataModel;
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


public class RepoBranchWidget extends Composite {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(RepoBranchWidget.class);
	
	interface ViewUiBinder extends UiBinder<Widget,RepoBranchWidget> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	private HashMap<XId,ModelBranchWidget> existingBranches;
	XAddress address;
	
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
	
	private SelectionTreePresenter presenter;
	
	public RepoBranchWidget(XAddress address, SelectionTreePresenter presenter) {
		this.address = address;
		this.presenter = presenter;
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
		updateViewModel();
		Controller.getInstance().present();
	}
	
	@UiHandler("fetchModelsButton")
	void onClickFetch(ClickEvent event) {
		
		this.presenter.fetchModelsFromServer(this.address);
		RepoBranchWidget.this.collapse();
		updateViewModel();
		
	}
	
	private void updateViewModel() {
		if(this.expanded) {
			ViewModel.getInstance().closeLocation(this.address);
		} else {
			ViewModel.getInstance().openLocation(this.address);
		}
	}
	
	@UiHandler("anchor")
	void onClickGet(ClickEvent event) {
		this.expandButton.click();
		
	}
	
	@UiHandler("addButton")
	void onClickAdd(ClickEvent event) {
		
		AddElementDialog addDialog = new AddElementDialog(this.presenter,
		        RepoBranchWidget.this.address, "enter Element name");
		addDialog.show();
		addDialog.selectEverything();
	}
	
	private void setComponents() {
		
		Iterator<XId> iterator = DataModel.getInstance().getLocallyStoredModelIDs(this.address);
		
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
		ModelBranchWidget newBranch = new ModelBranchWidget(address, presenter);
		this.branches.add(newBranch);
		DataModel.getInstance().getRepo(this.address.getRepository()).getModel(modelId)
		        .getRevisionNumber();
		
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
	
	public void addElement(String id) {
		this.addBranch(XX.toId(id));
	}
	
	// public void notifyMe(XAddress address) {
	//
	// if(address.getAddressedType().equals(XType.XREPOSITORY)) {
	// this.collapse();
	// this.expand();
	// } else {
	// ModelBranchWidget modelBranch =
	// this.existingBranches.get(address.getModel());
	// modelBranch.notifyMe(address);
	// }
	//
	// this.collapse();
	// this.expand();
	//
	// }
	
	private void expand() {
		RepoBranchWidget.this.existingBranches = new HashMap<XId,ModelBranchWidget>();
		this.setComponents();
		RepoBranchWidget.this.expandButton.setText("-");
		this.expanded = true;
		
	}
	
	private void collapse() {
		RepoBranchWidget.this.branches.clear();
		RepoBranchWidget.this.existingBranches = null;
		RepoBranchWidget.this.expandButton.setText("+");
		
		this.expanded = false;
	}
	
	public void openModel(XAddress address2) {
		this.fetchModelsButton.click();
		this.existingBranches.get(address2.getModel()).open(address2);
	}
	
	public void assertExpanded() {
		this.collapse();
		this.expand();
		
	}
	
	public void assertCollapsed() {
		if(!this.expanded) {
			// nothing
		} else
			this.collapse();
	}
	
}
