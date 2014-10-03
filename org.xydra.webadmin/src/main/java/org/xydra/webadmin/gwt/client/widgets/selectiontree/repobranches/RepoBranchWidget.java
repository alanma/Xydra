package org.xydra.webadmin.gwt.client.widgets.selectiontree.repobranches;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.selectiontree.modelbranches.ModelBranchWidget;

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


public class RepoBranchWidget extends Composite implements IRepoBranchWidget {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(RepoBranchWidget.class);
	
	interface ViewUiBinder extends UiBinder<Widget,RepoBranchWidget> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
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
	
	private RepoBranchPresenter presenter;
	
	public RepoBranchWidget(RepoBranchPresenter repoBranchPresenter) {
		this.presenter = repoBranchPresenter;
	}
	
	@UiHandler("expandButton")
	void onClickExpand(ClickEvent event) {
		this.presenter.handleExpand(this);
	}
	
	@UiHandler("fetchModelsButton")
	void onClickFetch(ClickEvent event) {
		
		this.presenter.fetchModels();
		
	}
	
	@UiHandler("anchor")
	void onClickGet(ClickEvent event) {
		this.expandButton.click();
		
	}
	
	@UiHandler("addButton")
	void onClickAdd(ClickEvent event) {
		
		this.presenter.openAddElementDialog("enter Element name");
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.webadmin.gwt.client.widgets.selectiontree.repobranches.
	 * IRepoBranchWidget2#init()
	 */
	@Override
	public void init() {
		initWidget(uiBinder.createAndBindUi(this));
		this.mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		this.mainPanel.addStyleName("repoBranchBorder");
		String plusButtonText = "Add Model";
		this.addButton.setText(plusButtonText);
		
		this.mainPanel
		        .setCellHorizontalAlignment(this.branches, HasHorizontalAlignment.ALIGN_RIGHT);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.webadmin.gwt.client.widgets.selectiontree.repobranches.
	 * IRepoBranchWidget2#setExpandButtonText(java.lang.String)
	 */
	@Override
	public void setExpandButtonText(String string) {
		RepoBranchWidget.this.expandButton.setText(string);
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xydra.webadmin.gwt.client.widgets.selectiontree.repobranches.
	 * IRepoBranchWidget2#clearBranches()
	 */
	@Override
	public void clearBranches() {
		RepoBranchWidget.this.branches.clear();
		
	}
	
	@Override
	public void setAnchorText(String anchorText) {
		this.anchor.setText(anchorText);
		
	}
	
	@Override
	public void addBranch(ModelBranchWidget newBranch) {
		if(this.branches.getWidgetCount() == 0) {
			this.buttonPanel.getElement().setAttribute("style",
			        "border-bottom: 1px solid #009; margin-bottom: 5px");
		}
		this.branches.add(newBranch);
		
	}
	
	@Override
	public RepoBranchWidget asWidget() {
		return this;
	}
	
	@Override
	public void deActivateFetchChilds() {
		this.fetchModelsButton.setEnabled(false);
	}
	
}
