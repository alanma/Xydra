package org.xydra.webadmin.gwt.client.widgets.selectiontree;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.xydra.base.XId;
import org.xydra.base.XX;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.ViewModel;
import org.xydra.webadmin.gwt.client.datamodels.DataModel;
import org.xydra.webadmin.gwt.client.datamodels.RepoDataModel;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class SelectionTree extends Composite {
	
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,SelectionTree> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	private HashMap<XId,RepoBranchWidget> branches;
	
	@UiField
	VerticalPanel mainPanel;
	
	private SelectionTreePresenter presenter;
	
	public SelectionTree() {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		
		this.presenter = new SelectionTreePresenter();
		
		this.branches = new HashMap<XId,RepoBranchWidget>();
		
		Controller.getInstance().registerSelectionTree(this);
		this.setComponents();
		
	}
	
	public void setComponents() {
		
		DataModel dataModel = DataModel.getInstance();
		Iterator<RepoDataModel> repoIDIterator = dataModel.getRepoIDs();
		while(repoIDIterator.hasNext()) {
			
			RepoDataModel repo = repoIDIterator.next();
			addRepoBranch(repo, this.mainPanel.getWidgetCount());
		}
		AddRepoWidget addRepoWidget = new AddRepoWidget(this.presenter);
		this.mainPanel.add(addRepoWidget);
		this.mainPanel.setCellHorizontalAlignment(addRepoWidget,
		        HasHorizontalAlignment.ALIGN_CENTER);
		
	}
	
	private void addRepoBranch(RepoDataModel repo, int position) {
		RepoBranchWidget repoBranch = new RepoBranchWidget(XX.toAddress(repo.getId(), null, null,
		        null), this.presenter);
		this.mainPanel.insert(repoBranch, position);
		this.branches.put(repo.getId(), repoBranch);
	}
	
	public void build() {
		ViewModel viewModel = ViewModel.getInstance();
		Set<XId> openRepos = viewModel.getOpenRepos();
		log.info("open repos when building: " + openRepos.toString());
		
		Set<Entry<XId,RepoBranchWidget>> branchEntries = this.branches.entrySet();
		for(Entry<XId,RepoBranchWidget> branch : branchEntries) {
			if(openRepos.contains(branch.getKey())) {
				branch.getValue().assertExpanded();
			} else
				branch.getValue().assertCollapsed();
		}
		
	}
}
