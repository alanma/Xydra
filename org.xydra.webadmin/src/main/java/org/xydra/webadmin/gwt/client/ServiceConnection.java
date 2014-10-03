package org.xydra.webadmin.gwt.client;

import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommand;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XReadableModel;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.webadmin.gwt.client.events.CommittingEvent.CommitStatus;
import org.xydra.webadmin.gwt.client.events.EntityStatus;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.dialogs.WarningDialog;
import org.xydra.webadmin.gwt.shared.XyAdminServiceAsync;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The only class that talks to the server
 * 
 * @author andi
 */
public class ServiceConnection {

	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);

	private XyAdminServiceAsync service;

	public ServiceConnection(XyAdminServiceAsync service) {
		this.service = service;
	}

	public void getModelIdsFromServer(final XAddress address) {

		Controller.showWaitCursor();
		final XId repoId = address.getRepository();
		this.service.getModelIds(repoId, new AsyncCallback<Set<XId>>() {

			@Override
			public void onSuccess(Set<XId> result) {
				log.info("Server said: " + result);

				if (result.isEmpty()) {
					log.error("no models found!");
					WarningDialog dialog = new WarningDialog("no models found!");
					dialog.show();
				} else {
					for (XId modelID : result) {
						XyAdmin.getInstance().getModel().getRepo(repoId).indexModel(modelID);

					}
					XyAdmin.getInstance().getModel().getRepo(repoId).setKnowsAllModels();
					log.info("indexed " + result.size()
							+ " models, now firing RepoChangedEvent: Indexed!");
					EventHelper.fireRepoChangeEvent(XX.toAddress(repoId, null, null, null),
							EntityStatus.INDEXED, null);

				}
				Controller.showDefaultCursor();
			}

			@Override
			public void onFailure(Throwable caught) {

				log.warn("Error", caught);
			}
		});

	}

	public void loadModelsObjects(final XAddress address) {
		this.service.getModelSnapshot(address.getRepository(), address.getModel(),
				new AsyncCallback<XReadableModel>() {

					@Override
					public void onSuccess(XReadableModel result) {
						String info = "dummy";
						if (result == null) {
							@SuppressWarnings("unused")
							WarningDialog d = new WarningDialog(
									"model doesn't exist on repository!");
							XyAdmin.getInstance().getModel().getRepo(address.getRepository())
									.addDeletedModel(address.getModel());
							info = "removed";
						} else {
							XyAdmin.getInstance().getModel().getRepo(address.getRepository())
									.getModel(result.getId()).indexModel(result);

						}
						log.info("loaded model " + address.toString()
								+ " from repository, now firing ModelChangedEvent: INDEXED");
						EventHelper.fireModelChangedEvent(address, EntityStatus.INDEXED,
								XX.toId(info));
						Controller.showDefaultCursor();
					}

					@Override
					public void onFailure(Throwable caught) {
						log.warn("Error", caught);
						Controller.showDefaultCursor();
					}

				});

	}

	public void commitAddedModel(final XAddress modelAddress, XCommand addModelCommand,
			final XTransaction modelTransactions) {
		this.service.executeCommand(modelAddress.getRepository(), addModelCommand,
				new AsyncCallback<Long>() {

					@SuppressWarnings("unused")
					String resultString = "";

					@Override
					public void onSuccess(Long result) {

						CommitStatus status = CommitStatus.SUCCESSANDPROCEED;

						if (modelTransactions == null)
							status = CommitStatus.SUCCESS;

						EventHelper.fireCommitEvent(modelAddress, status, result);

						commitModelTransactions(modelAddress, modelTransactions);
					}

					@Override
					public void onFailure(Throwable caught) {

					}
				});
	}

	public void commitModelTransactions(final XAddress modelAddress, XTransaction modelTransactions) {

		Controller.showWaitCursor();

		if (modelTransactions != null) {
			this.service.executeCommand(modelAddress.getRepository(), modelTransactions,
					new AsyncCallback<Long>() {

						@Override
						public void onSuccess(Long result) {

							EventHelper.fireCommitEvent(modelAddress, CommitStatus.SUCCESS, result);

						}

						@Override
						public void onFailure(Throwable caught) {
							EventHelper.fireCommitEvent(modelAddress, CommitStatus.FAILED, -1l);
						}
					});

		}
	}

	public void removeModel(final XAddress address) {
		XRepositoryCommand command = X.getCommandFactory().createForcedRemoveModelCommand(address);
		log.info("hi, " + address.toString());
		this.service.executeCommand(address.getRepository(), command, new AsyncCallback<Long>() {

			@SuppressWarnings("unused")
			String resultString = "";

			@Override
			public void onSuccess(Long result) {

				EventHelper.fireCommitEvent(address, CommitStatus.SUCCESS, result);
			}

			@Override
			public void onFailure(Throwable caught) {
				EventHelper.fireCommitEvent(address, CommitStatus.FAILED, -1l);

			}
		});

	}
}
