package org.xydra.webadmin.gwt.client.widgets.selectiontree;

import java.util.HashMap;
import java.util.Iterator;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.webadmin.gwt.client.EventHelper;
import org.xydra.webadmin.gwt.client.datamodels.DataModel;
import org.xydra.webadmin.gwt.client.datamodels.RepoDataModel;
import org.xydra.webadmin.gwt.client.events.RepoChangedEvent;
import org.xydra.webadmin.gwt.client.events.RepoChangedEvent.IRepoChangedEventHandler;
import org.xydra.webadmin.gwt.client.util.Presenter;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.dialogs.AddElementDialog;
import org.xydra.webadmin.gwt.client.widgets.selectiontree.repobranches.RepoBranchPresenter;
import org.xydra.webadmin.gwt.client.widgets.selectiontree.repobranches.RepoBranchWidget;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;

/**
 * Performs logic for the building {@link RepoBranchWidget}s and adding new
 * repositories. Also holds a presenter to for each {@link RepoBranchWidget}
 *
 * @author kahmann
 *
 */
public class SelectionTreePresenter extends Presenter {

	private static final Logger log = LoggerFactory.getLogger(RepoBranchWidget.class);

	private HashMap<XId, RepoBranchPresenter> repoBranches;

	private SelectionTree selectionTreeWidget;

	public SelectionTreePresenter(final SelectionTree selectionTree) {
		this.selectionTreeWidget = selectionTree;
		this.repoBranches = new HashMap<XId, RepoBranchPresenter>();

	}

	public SelectionTreePresenter() {

	}

	public void present() {

		final DataModel dataModel = XyAdmin.getInstance().getModel();
		final Iterator<RepoDataModel> repoIDIterator = dataModel.getRepoIDs();
		final AddRepoWidget addRepoWidget = new AddRepoWidget(this);
		this.selectionTreeWidget.mainPanel.add(addRepoWidget);
		this.selectionTreeWidget.mainPanel.setCellHorizontalAlignment(addRepoWidget,
				HasHorizontalAlignment.ALIGN_CENTER);
		while (repoIDIterator.hasNext()) {

			final RepoDataModel repo = repoIDIterator.next();
			addRepoBranch(repo);
		}

		EventHelper.addRepoChangeListener(Base.resolveRepository(Base.toId("newRepo")),
				new IRepoChangedEventHandler() {

					@Override
					public void onRepoChange(final RepoChangedEvent event) {
						SelectionTreePresenter.this.addRepoBranch(event.getMoreInfos());

					}
				});

		log.info("selection tree build!");
	}

	private void addRepoBranch(final RepoDataModel repo) {
		final int position = this.selectionTreeWidget.mainPanel.getWidgetCount() - 1;
		final RepoBranchPresenter repoBranchPresenter = new RepoBranchPresenter(Base.toAddress(
				repo.getId(), null, null, null));

		this.selectionTreeWidget.mainPanel.insert(repoBranchPresenter.presentWidget(), position);
		this.repoBranches.put(repo.getId(), repoBranchPresenter);
	}

	public void openAddElementDialog(final XAddress address, final String message) {
		final AddElementDialog addDialog = new AddElementDialog(this, address, message);
		addDialog.show();
		addDialog.selectEverything();

	}

	public boolean showRepository(final XAddress repoAddress) {
		final XId repoId = repoAddress.getRepository();
		boolean alreadyOpened = false;

		final RepoBranchPresenter repoBranchPresenter = this.repoBranches
				.get(repoAddress.getRepository());
		if (XyAdmin.getInstance().getModel().getRepo(repoId).knowsAllModels()) {
			alreadyOpened = repoBranchPresenter.assertExpanded();
		} else {
			repoBranchPresenter.fetchModels();
		}

		return alreadyOpened;
	}

}
