package org.xydra.webadmin.gwt.client.widgets.selectiontree;

import java.util.HashMap;
import java.util.Iterator;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.core.XX;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.datamodels.DataModel;
import org.xydra.webadmin.gwt.client.datamodels.RepoDataModel;
import org.xydra.webadmin.gwt.client.util.Presenter;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.dialogs.AddElementDialog;
import org.xydra.webadmin.gwt.client.widgets.selectiontree.repobranches.RepoBranchWidget;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;


public class SelectionTreePresenter extends Presenter {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(RepoBranchWidget.class);
	
	private HashMap<XId,RepoBranchWidget> repoBranches;
	
	private SelectionTree selectionTreeWidget;
	
	public SelectionTreePresenter(SelectionTree selectionTree) {
		this.selectionTreeWidget = selectionTree;
		this.repoBranches = new HashMap<XId,RepoBranchWidget>();
		
		XyAdmin.getInstance().getController().registerSelectionTreePresenter(this);
		this.buildSelectionTree();
		
	}
	
	public SelectionTreePresenter() {
		
	}
	
	private void buildSelectionTree() {
		
		DataModel dataModel = XyAdmin.getInstance().getModel();
		Iterator<RepoDataModel> repoIDIterator = dataModel.getRepoIDs();
		while(repoIDIterator.hasNext()) {
			
			RepoDataModel repo = repoIDIterator.next();
			addRepoBranch(repo);
		}
		AddRepoWidget addRepoWidget = new AddRepoWidget(this);
		this.selectionTreeWidget.mainPanel.add(addRepoWidget);
		this.selectionTreeWidget.mainPanel.setCellHorizontalAlignment(addRepoWidget,
		        HasHorizontalAlignment.ALIGN_CENTER);
		
	}
	
	private void addRepoBranch(RepoDataModel repo) {
		int position = this.selectionTreeWidget.mainPanel.getWidgetCount();
		RepoBranchWidget repoBranch = new RepoBranchWidget(XX.toAddress(repo.getId(), null, null,
		        null));
		this.selectionTreeWidget.mainPanel.insert(repoBranch, position);
		this.repoBranches.put(repo.getId(), repoBranch);
	}
	
	public void openAddElementDialog(XAddress address, String message) {
		AddElementDialog addDialog = new AddElementDialog(this, address, message);
		addDialog.show();
		addDialog.selectEverything();
		
	}
	
}
