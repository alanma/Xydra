package org.xydra.webadmin.gwt.client.widgets.editorpanel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.change.XCommandUtils;
import org.xydra.base.change.XRepositoryCommand;
import org.xydra.base.change.XTransaction;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.util.DumpUtilsBase.XidComparator;
import org.xydra.base.value.XValue;
import org.xydra.core.X;
import org.xydra.core.XX;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.core.model.delta.IFieldDiff;
import org.xydra.core.model.delta.IObjectDiff;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.EventHelper;
import org.xydra.webadmin.gwt.client.events.CommittingEvent;
import org.xydra.webadmin.gwt.client.events.CommittingEvent.ICommitEventHandler;
import org.xydra.webadmin.gwt.client.events.EntityStatus;
import org.xydra.webadmin.gwt.client.events.ModelChangedEvent;
import org.xydra.webadmin.gwt.client.events.ModelChangedEvent.IModelChangedEventHandler;
import org.xydra.webadmin.gwt.client.util.Presenter;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.dialogs.CommittingDialog;
import org.xydra.webadmin.gwt.client.widgets.dialogs.ConfirmationDialog;
import org.xydra.webadmin.gwt.client.widgets.dialogs.ObjectChangesPanel;
import org.xydra.webadmin.gwt.client.widgets.dialogs.WarningDialog;
import org.xydra.webadmin.gwt.client.widgets.editorpanel.tablewidgets.TablePresenter;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Performs logic for {@link EditorPanel}s. So it:
 * 
 * <ul>
 * <li>processes the presentation of models via starting the
 * {@link TablePresenter}
 * <li>fulfills requests given via the {@link ModelControlPanel}
 * <li>has Listeners to {@link ModelChangedEvent}s and {@link CommittingEvent}s
 * </ul>
 * 
 * The listeners react, when
 * <ul>
 * <li>a model with the appropriate address is indexed (fetched from the server
 * and locally indexed),
 * <li>the model is removed
 * </ul>
 * 
 * @author kahmann
 * 
 */
public class EditorPanelPresenter extends Presenter {

	private final String ADDED = "ADDED";
	private final String REMOVED = "REMOVED";
	private final String CHANGED = "CHANGED";

	private static final Logger log = LoggerFactory.getLogger(EditorPanelPresenter.class);

	private IEditorPanel editorPanel;
	private XAddress currentModelAddress;
	private TablePresenter tablePresenter;

	private HandlerRegistration handlerRegistration;

	private HandlerRegistration commitHandlerRegistration;

	public EditorPanelPresenter(IEditorPanel editorPanel) {
		this.editorPanel = editorPanel;

	}

	public void present() {
		this.editorPanel.init();
	}

	public void presentModel(XAddress address) {
		this.currentModelAddress = address;

		this.buildModelView();
	}

	public void buildModelView() {

		XyAdmin.getInstance().getController().unregistrateAllHandlers();

		this.editorPanel.clear();

		ModelControlPanel modelControlPanel = new ModelControlPanel(this);

		ModelInformationPanel modelInformationPanel = new ModelInformationPanel(
				this.currentModelAddress.getModel().toString());
		this.tablePresenter = new TablePresenter(this, modelInformationPanel);
		this.tablePresenter.generateTableOrShowInformation();
		this.editorPanel.add(modelControlPanel);
		this.editorPanel.add(modelInformationPanel);

		this.handlerRegistration = EventHelper.addModelChangedListener(this.currentModelAddress,
				new IModelChangedEventHandler() {

					@Override
					public void onModelChange(ModelChangedEvent event) {
						if (event.getStatus().equals(EntityStatus.DELETED)) {
							resetView();

						} else if (event.getStatus().equals(EntityStatus.EXTENDED)) {
							// nothing
						}

						else if (event.getStatus().equals(EntityStatus.INDEXED)) {
							if (event.getMoreInfos().equals(XX.toId("removed"))) {
								resetView();
							} else {
								EditorPanelPresenter.this.tablePresenter
										.generateTableOrShowInformation();
							}
						}
					}

				});
		XyAdmin.getInstance().getController().addRegistration(this.handlerRegistration);
	}

	private void resetView() {
		this.editorPanel.clear();
		Label noModelLabel = new Label("choose model via selection tree");
		this.editorPanel.add(noModelLabel);
	}

	public void loadModelsObjectsFromPersistence() {
		XyAdmin.getInstance().getController().loadModelsObjects(this.currentModelAddress);
	}

	public void handleFetchIDs() {
		@SuppressWarnings("unused")
		WarningDialog warning = new WarningDialog("not yet Implemented!");
	}

	void openCommitDialog(ModelControlPanel modelControlPanel) {

		// TODO research how this could have been better with explicit UI
		// components

		SessionCachedModel model = this.getCurrentModel();
		VerticalPanel changesPanel = new VerticalPanel();
		changesPanel.addStyleName("changesPanelStyle");

		/* if the model was added */
		if (XyAdmin.getInstance().getModel().getRepo(this.currentModelAddress.getRepository())
				.isAddedModel(this.currentModelAddress.getModel())) {
			HTML addedModelHTML = new HTML("ADDED MODEL " + this.currentModelAddress.toString());
			addedModelHTML.getElement().setAttribute("style", "font-size : 20px");
			addedModelHTML.setStyleName("addedEntityStyle");
			changesPanel.add(addedModelHTML);
			changesPanel.setCellHorizontalAlignment(addedModelHTML,
					HasHorizontalAlignment.ALIGN_CENTER);
			changesPanel.add(new HTML("<br>"));
		}

		/* for all added objects */
		List<XId> addedList = new ArrayList<XId>();
		for (XReadableObject object : this.getCurrentModel().getAdded()) {
			addedList.add(object.getId());
		}

		Collections.sort(addedList, XidComparator.INSTANCE);
		int count = 0;
		String objectsStatus = this.ADDED;
		count = compileObjectInfos(changesPanel, addedList, count, objectsStatus);

		/* for deleted objects */
		List<XId> removedList = new ArrayList<XId>(model.getRemoved());
		Collections.sort(removedList, XidComparator.INSTANCE);
		objectsStatus = this.REMOVED;
		count = compileObjectInfos(changesPanel, removedList, count, objectsStatus);

		/* for all changed objects */
		List<XId> changedList = new ArrayList<XId>();
		for (IObjectDiff object : model.getPotentiallyChanged()) {
			changedList.add(object.getId());
		}
		Collections.sort(changedList, XidComparator.INSTANCE);
		objectsStatus = this.CHANGED;
		count = compileObjectInfos(changesPanel, changedList, count, objectsStatus);

		CommittingDialog committingDialog = new CommittingDialog(this, changesPanel);
		committingDialog.show();

	}

	private int compileObjectInfos(VerticalPanel changesPanel, List<XId> changedObjectList,
			int count, String objectsStatus) {
		int count2 = count;
		for (XId currentObject : changedObjectList) {

			String[] backgroundColors = new String[] { "darkgray", "lightgray" };
			String backgroundColor = backgroundColors[count2 % 2];

			HorizontalPanel objectIdPanel = new HorizontalPanel();
			objectIdPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			objectIdPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
			Label objectIdLabel = new Label(currentObject.toString());
			if (!objectsStatus.equals(this.CHANGED)) {
				String statussStyle = "addedEntityStyle";
				if (objectsStatus.equals(this.REMOVED))
					statussStyle = "removedEntityStyle";
				VerticalPanel objectIdStatusCombination = new VerticalPanel();
				objectIdStatusCombination
						.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
				objectIdStatusCombination.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
				objectIdStatusCombination.add(objectIdLabel);
				HTML breakHTML = new HTML("<br>");
				breakHTML.setStyleName("breakStyle");
				objectIdStatusCombination.add(breakHTML);
				Label statusLabel = new Label(objectsStatus);
				statusLabel.addStyleName(statussStyle);
				statusLabel.getElement().setAttribute("style", "font-size: 16px");
				objectIdStatusCombination.add(statusLabel);
				objectIdPanel.add(objectIdStatusCombination);
			} else {
				objectIdPanel.add(objectIdLabel);
			}

			VerticalPanel fieldChangesPanel = compileFieldChanges(currentObject, objectsStatus);

			changesPanel.add(new ObjectChangesPanel(backgroundColor, objectIdPanel,
					fieldChangesPanel));

			count2++;
		}
		return count2;
	}

	private VerticalPanel compileFieldChanges(XId objectsId, String objectsStatus) {
		VerticalPanel fieldChangesPanel = new VerticalPanel();

		if (objectsStatus.equals(this.ADDED)) {
			for (XId fieldId : this.getCurrentModel().getObject(objectsId)) {
				XWritableField field = this.getCurrentModel().getObject(objectsId)
						.getField(fieldId);

				fieldChangesPanel.add(buildFieldInfo(objectsStatus, fieldId));
				XValue value = field.getValue();
				if (value != null) {
					HTML fieldChangeElement = new HTML("--> changed value of " + fieldId.toString()
							+ " to \"" + value.toString() + "\" (" + value.getType().toString()
							+ ")");
					fieldChangesPanel.add(fieldChangeElement);
				}
			}
			if (!fieldChangesPanel.iterator().hasNext()) {
				fieldChangesPanel.add(new HTML(""));
			}
		} else if (objectsStatus.equals(this.REMOVED)) {
			{
				/*
				 * it was intended to show all changes -crossed-, but currently
				 * there is no way to do this
				 */
				// XWritableObject removedObject =
				// this.getCurrentModel().getObject(objectsId);
				// for(XId fieldId : removedObject) {
				// XWritableField field = removedObject.getField(fieldId);
				// fieldChangesPanel.add(buildFieldInfo(objectsStatus,
				// fieldId));
				// XValue value = field.getValue();
				// if(value != null) {
				// HTML fieldChangeElement = new HTML("--> changed value of "
				// + fieldId.toString() + " to \"" + value.toString() + "\" ("
				// + value.getType().toString() + ")");
				// fieldChangeElement.addStyleName("deletedEntityStyle");
				// fieldChangesPanel.add(fieldChangeElement);
				// }
				// break;
				// }

				fieldChangesPanel.add(new HTML());
			}
		} else if (objectsStatus.equals((this.CHANGED))) {
			Collection<? extends IObjectDiff> potentiallyChangedObjects = this.getCurrentModel()
					.getPotentiallyChanged();
			for (IObjectDiff iObjectDiff : potentiallyChangedObjects) {
				if (iObjectDiff.getId().equals(objectsId)) {
					String fieldStatus = this.ADDED;
					Collection<? extends XReadableField> addedFields = iObjectDiff.getAdded();
					for (XReadableField addedField : addedFields) {
						fieldChangesPanel.add(buildFieldInfo(fieldStatus, addedField.getId()));
						XValue value = addedField.getValue();
						if (value != null) {
							HTML fieldChangeElement = new HTML("--> changed value of "
									+ addedField.getId().toString() + " to \"" + value.toString()
									+ "\" (" + value.getType().toString() + ")");
							fieldChangesPanel.add(fieldChangeElement);
						}
					}
					fieldStatus = this.CHANGED;
					Collection<? extends IFieldDiff> changedFields = iObjectDiff
							.getPotentiallyChanged();
					for (IFieldDiff iFieldDiff : changedFields) {
						fieldChangesPanel.add(buildFieldInfo(fieldStatus, iFieldDiff.getId()));
						XValue value = iFieldDiff.getValue();
						String valueString = value.toString();
						XValue oldValue = iFieldDiff.getInitialValue();
						String oldValueString = "";
						if (oldValue == null)
							oldValueString = "null";
						else
							oldValueString = oldValue.toString();
						String valueTypeString = value.getType().toString();
						HTML fieldChangeElement = new HTML("--> changed value of "
								+ iFieldDiff.getId().toString() + " from \"" + oldValueString
								+ "\" to \"" + valueString + "\" (" + valueTypeString + ")");
						fieldChangesPanel.add(fieldChangeElement);
					}
					fieldStatus = this.REMOVED;
					Collection<XId> removedFields = iObjectDiff.getRemoved();
					for (XId removedFieldId : removedFields) {
						fieldChangesPanel.add(buildFieldInfo(fieldStatus, removedFieldId));
						// XValue fieldsValue =
						// this.getCurrentModel().getObject(objectsId)
						// .getField(removedFieldId).getValue();
						// if(fieldsValue != null) {
						// HTML fieldChangeElement = new HTML("--> value of "
						// + removedFieldId.toString() + " to \"" +
						// fieldsValue.toString()
						// + "\" (" + fieldsValue.getType().toString() + ")");
						// fieldChangeElement.addStyleName("deletedEntityStyle");
						// fieldChangesPanel.add(fieldChangeElement);
						// }
					}
				}
			}
		}
		return fieldChangesPanel;
	}

	private HTML buildFieldInfo(String fieldsStatus, XId fieldsId) {
		String style = "addedEntityStyle";
		if (fieldsStatus.equals(this.REMOVED)) {
			style = "removedEntityStyle";
		} else if (fieldsStatus.equals(this.CHANGED)) {
			style = "changedEntityStyle";
		}
		HTML containerDiv = new HTML("");
		HTML fieldStatusDiv = new HTML(fieldsStatus);
		fieldStatusDiv.getElement().setAttribute("style", "display: inline");
		fieldStatusDiv.addStyleName(style);
		containerDiv.getElement().appendChild(fieldStatusDiv.getElement());
		String fieldInfoString = " field \"" + fieldsId.toString() + "\"";
		HTML infoHtml = new HTML(fieldInfoString);
		infoHtml.getElement().setAttribute("style", "display: inline");
		containerDiv.getElement().appendChild(infoHtml.getElement());
		return containerDiv;

	}

	@SuppressWarnings("unused")
	void openDiscardChangesDialog() {
		new ConfirmationDialog(this, "discard all Changes");
	}

	public void expandAll(String expandButtonText) {
		this.tablePresenter.expandAll(expandButtonText);
	}

	public XAddress getCurrentModelAddress() {
		return this.currentModelAddress;
	}

	public SessionCachedModel getCurrentModel() {
		SessionCachedModel selectedModel = XyAdmin.getInstance().getModel()
				.getRepo(this.currentModelAddress.getRepository())
				.getModel(this.currentModelAddress.getModel());
		return selectedModel;
	}

	public void commit(final CommittingDialog committingDialog) {
		XRepositoryCommand addModelCommand = null;
		if (XyAdmin.getInstance().getModel().getRepo(this.currentModelAddress.getRepository())
				.isAddedModel(this.currentModelAddress.getModel())) {
			addModelCommand = X.getCommandFactory().createAddModelCommand(
					this.currentModelAddress.getRepository(), this.currentModelAddress.getModel(),
					true);
		}

		XTransaction modelTransactions = null;
		try {
			modelTransactions = XyAdmin.getInstance().getModel()
					.getRepo(this.currentModelAddress.getRepository())
					.getModelChanges(null, this.currentModelAddress).build();
		} catch (Exception e) {
			// just no changes
		}
		XyAdmin.getInstance().getController()
				.commit(this.currentModelAddress, addModelCommand, modelTransactions);

		this.commitHandlerRegistration = EventHelper.addCommittingListener(
				this.currentModelAddress, new ICommitEventHandler() {

					@Override
					public void onCommit(CommittingEvent event) {
						processCommitResponse(committingDialog, event);
					}
				});
	}

	private void processCommitResponse(final CommittingDialog committingDialog,
			CommittingEvent event) {
		String message = "";
		long responseRevisionNumber = event.getNewRevision();

		if (event.getStatus().equals(CommittingEvent.CommitStatus.SUCCESSANDPROCEED)) {

			if (XCommandUtils.success(responseRevisionNumber)) {
				message = "successfully committed model! New revision number: "
						+ responseRevisionNumber;
			} else if (XCommandUtils.noChange(responseRevisionNumber)) {
				message = "no Changes!";
			} else if (XCommandUtils.failed(responseRevisionNumber)) {
				message = "commit failed!";
			} else {
				message = "i have no idea...";
			}

		} else if (event.getStatus().equals(CommittingEvent.CommitStatus.SUCCESS)) {
			if (XCommandUtils.success(responseRevisionNumber)) {
				message = "successfully committed model changes! New revision number: "
						+ event.getNewRevision();
				XyAdmin.getInstance().getModel().getRepo(event.getModelAddress().getRepository())
						.setCommitted(event.getModelAddress().getModel());
				XyAdmin.getInstance().getController().loadModelsObjects(this.currentModelAddress);
			} else if (XCommandUtils.noChange(responseRevisionNumber)) {
				message = "no Changes!";
			} else if (XCommandUtils.failed(responseRevisionNumber)) {
				message = "commit failed!";
			} else {
				message = "i have no idea...";
			}
		} else {
			message = "committing changes failed!";
		}

		committingDialog.addText(message);

		if (!event.getStatus().equals(CommittingEvent.CommitStatus.SUCCESSANDPROCEED)) {
			committingDialog.removeWaitingElements();
			committingDialog.addCloseOKButton();
			Controller.showDefaultCursor();
			this.commitHandlerRegistration.removeHandler();
		}
	}

	public void discardChanges() {
		log.info("now discarding all changes and building a new model view!");
		this.getCurrentModel().discardAllChanges();
		this.buildModelView();

	}

	public void showObjectAndField(XAddress desiredAddress) {
		this.tablePresenter.showObjectAndField(desiredAddress);

	}
}
