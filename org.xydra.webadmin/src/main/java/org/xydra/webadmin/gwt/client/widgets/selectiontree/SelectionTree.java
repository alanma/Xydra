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
import org.xydra.webadmin.gwt.client.XyAdmin;
import org.xydra.webadmin.gwt.client.datamodels.DataModel;
import org.xydra.webadmin.gwt.client.datamodels.RepoDataModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class SelectionTree extends Composite implements Observable {
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,SelectionTree> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	private HashMap<XId,BranchWidget> branches;
	
	@UiField
	VerticalPanel mainPanel;
	
	public SelectionTree() {
		super();
		initWidget(uiBinder.createAndBindUi(this));
		
		this.branches = new HashMap<XId,BranchWidget>();
		
		Controller.getInstance().registerSelectionTree(this);
		this.setComponents();
		
	}
	
	public void setComponents() {
		
		DataModel dataModel = Controller.getInstance().getDataModel();
		Iterator<RepoDataModel> repoIDIterator = dataModel.getRepoIDs();
		while(repoIDIterator.hasNext()) {
			
			RepoDataModel repo = repoIDIterator.next();
			BranchWidget repoBranch = new BranchWidget(XX.toAddress(repo.getId(), null, null, null));
			this.mainPanel.add(repoBranch);
			this.branches.put(repo.getId(), repoBranch);
		}
	}
	
	@Override
	public void notifyMe(XAddress address) {
		
		XId repoId = address.getRepository();
		this.branches.get(repoId).notifyMe(address);
	}
}
