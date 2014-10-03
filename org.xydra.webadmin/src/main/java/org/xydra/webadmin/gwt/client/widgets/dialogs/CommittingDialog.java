package org.xydra.webadmin.gwt.client.widgets.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xydra.base.XId;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.util.DumpUtilsBase.XidComparator;
import org.xydra.core.model.delta.IFieldDiff;
import org.xydra.core.model.delta.IModelDiff;
import org.xydra.core.model.delta.IObjectDiff;
import org.xydra.core.util.DumpUtils;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.webadmin.gwt.client.resources.BundledRes;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.editorpanel.EditorPanelPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class CommittingDialog extends DialogBox {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);

	interface ViewUiBinder extends UiBinder<Widget, CommittingDialog> {
	}

	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	String inputString = "";

	@UiField
	VerticalPanel mainPanel;

	@UiField(provided = true)
	VerticalPanel changesPanel;

	@UiField(provided = true)
	ButtonPanel buttonPanel;

	public CommittingDialog(final EditorPanelPresenter presenter, VerticalPanel changes) {

		super();

		ClickHandler okHandler = new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				presenter.commit(CommittingDialog.this);

				CommittingDialog.this.setText("committing...");
				CommittingDialog.this.mainPanel.clear();
				CommittingDialog.this.mainPanel.add(new HTML("<br>"));
				CommittingDialog.this.mainPanel.add(new Image(BundledRes.INSTANCE.images()
						.spinner()));
				CommittingDialog.this.mainPanel.add(new HTML("<br> "));
				CommittingDialog.this.mainPanel.getElement().setAttribute("style",
						"min-width: 700px");

			}

		};

		this.buttonPanel = new ButtonPanel(okHandler, this);
		this.changesPanel = changes;

		setWidget(uiBinder.createAndBindUi(this));
		this.setStyleName("dialogStyle");
		this.setText("check changes of model "
				+ presenter.getCurrentModelAddress().getModel().toString());
		this.center();
	}

	public void addText(String message) {
		this.mainPanel.add(new Label(message));
	}

	public void addCloseOKButton() {
		Button okButton = new Button("ok");
		CommittingDialog.this.setText("commit ended");
		okButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				CommittingDialog.this.removeFromParent();
			}
		});
		this.mainPanel.add(okButton);
	}

	public static StringBuilder changesToString(final IModelDiff changedModel) {
		StringBuilder sb = new StringBuilder();
		List<XReadableObject> addedList = new ArrayList<XReadableObject>(changedModel.getAdded());
		Collections.sort(addedList, XidComparator.INSTANCE);
		for (XReadableObject addedObject : addedList) {
			sb.append("<br><br>=== ADDED   Object '" + addedObject.getId() + "' ===<br/>\n");
			sb.append(DumpUtils.toStringBuffer(addedObject).toString());
		}
		List<XId> removedList = new ArrayList<XId>(changedModel.getRemoved());
		Collections.sort(removedList, XidComparator.INSTANCE);
		for (XId removedObjectId : removedList) {
			sb.append("<br><br>=== REMOVED Object '" + removedObjectId + "' ===<br/>\n");
		}
		List<IObjectDiff> potentiallyChangedList = new ArrayList<IObjectDiff>(
				changedModel.getPotentiallyChanged());
		Collections.sort(potentiallyChangedList, XidComparator.INSTANCE);
		for (IObjectDiff changedObject : potentiallyChangedList) {
			if (changedObject.hasChanges()) {
				sb.append("<br><br>=== CHANGED Object '" + changedObject.getId() + "' === <br/>\n");
				sb.append(changesToString(changedObject).toString());
			}
		}
		return sb;
	}

	public static StringBuilder changesToString(final IObjectDiff changedObject) {
		StringBuilder sb = new StringBuilder();
		List<XReadableField> addedList = new ArrayList<XReadableField>(changedObject.getAdded());
		Collections.sort(addedList, XidComparator.INSTANCE);
		for (XReadableField field : addedList) {
			sb.append("--- ADDED Field '" + field.getId() + "' ---<br/>\n");
			sb.append(DumpUtils.toStringBuffer(field));
		}
		List<XId> removedList = new ArrayList<XId>(changedObject.getRemoved());
		Collections.sort(removedList, XidComparator.INSTANCE);
		for (XId objectId : changedObject.getRemoved()) {
			sb.append("--- REMOVED Field '" + objectId + "' ---<br/>\n");
		}
		List<IFieldDiff> potentiallyChangedList = new ArrayList<IFieldDiff>(
				changedObject.getPotentiallyChanged());
		Collections.sort(potentiallyChangedList, XidComparator.INSTANCE);
		for (IFieldDiff changedField : potentiallyChangedList) {
			if (changedField.isChanged()) {
				sb.append("--- CHANGED Field '" + changedField.getId() + "' ---<br/>\n");
				sb.append(changesToString(changedField).toString());
			}
		}
		return sb;
	}

	public static StringBuilder changesToString(final IFieldDiff changedField) {
		StringBuilder sb = new StringBuilder();
		sb.append("'" + changedField.getInitialValue() + "' ==> '" + changedField.getValue()
				+ "' \n");
		return sb;
	}

	public void removeWaitingElements() {
		this.mainPanel.remove(0);
		this.mainPanel.remove(0);
		this.mainPanel.remove(0);

	}

}
