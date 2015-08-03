package org.xydra.webadmin.gwt.client.widgets.editorpanel.tablewidgets;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XType;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.webadmin.gwt.client.resources.BundledRes;
import org.xydra.webadmin.gwt.client.util.Presenter;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.dialogs.AddElementDialog;
import org.xydra.webadmin.gwt.client.widgets.dialogs.AddressDialog;
import org.xydra.webadmin.gwt.client.widgets.dialogs.RemoveElementDialog;
import org.xydra.webadmin.gwt.client.widgets.dialogs.RemoveModelDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class EntityWidget extends Composite {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);

	interface ViewUiBinder extends UiBinder<Widget, EntityWidget> {
	}

	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

	@UiField
	VerticalPanel panel;

	@UiField
	HTMLPanel buttonPanel;

	@UiField
	HorizontalPanel idPanel;

	@UiField
	Anchor idLabel;

	@UiField
	Label revisionLabel;

	@UiField
	Button removeButton;

	@UiField
	Button addButton;

	@UiField
	Button showAddressButton;

	private final XAddress address;

	private String addText;
	private String addTitle;
	private String removeTitle;

	private final String addFieldText = "enter Field ID";
	private final String addObjectText = "enter Object ID";
	private final String addFieldButtonTitle = "add new field";
	private final String addObjectButtonTitle = "add new object";
	private final String removeObjectButtonTitle = "remove this object";
	private final String removeModelButtonTitle = "remove this model";

	private final HandlerRegistration removeClickHandlerRegistration;

	private final Presenter presenter;

	public EntityWidget(final Presenter presenter, final XAddress address, final ClickHandler anchorClickHandler) {
		super();

		this.presenter = presenter;
		this.address = address;
		initWidget(uiBinder.createAndBindUi(this));

		final Image deleteImg = new Image(BundledRes.INSTANCE.images().delete());
		this.removeButton.getElement().appendChild(deleteImg.getElement());
		this.removeButton.setStyleName("imageButtonStyle");
		final Image addImg = new Image(BundledRes.INSTANCE.images().add());
		this.addButton.getElement().appendChild(addImg.getElement());
		this.addButton.setStyleName("imageButtonStyle");
		final Image textImage = new Image(BundledRes.INSTANCE.images().list());
		this.showAddressButton.getElement().appendChild(textImage.getElement());
		this.showAddressButton.setStyleName("imageButtonStyle");
		this.removeButton.getElement().setAttribute("style", "float: right");
		this.addButton.getElement().setAttribute("style", "float: left");
		int rightPosition = 37;
		if (this.address.getObject() != null) {
			rightPosition = 28;
		}
		this.showAddressButton.getElement().setAttribute("style",
				"float: right; position: relative; right: " + rightPosition + "% ");

		XId entityId = address.getModel();
		long revisionNumber = -1000l;
		final SessionCachedModel model = XyAdmin.getInstance().getModel()
				.getRepo(address.getRepository()).getModel(entityId);

		switch (address.getAddressedType()) {
		case XMODEL:
			// entityID stays the same
			revisionNumber = model.getRevisionNumber();
			this.addText = this.addObjectText;
			this.addTitle = this.addObjectButtonTitle;
			this.removeTitle = this.removeModelButtonTitle;

			this.idPanel.addStyleName("modelIDLabel");
			this.idLabel.addStyleName("modelAnchorStyle");
			break;
		case XOBJECT:

			this.addText = this.addFieldText;
			this.addTitle = this.addFieldButtonTitle;
			this.removeTitle = this.removeObjectButtonTitle;
			entityId = address.getObject();
			final XWritableObject object = model.getObject(entityId);
			revisionNumber = object.getRevisionNumber();

			this.idPanel.addStyleName("objectIDLabel");
			break;
		default:
			// nothing
		}

		this.idLabel.setText(entityId.toString());
		this.idLabel.addClickHandler(anchorClickHandler);
		this.revisionLabel.setText("rev. " + revisionNumber);
		this.addButton.setTitle(this.addTitle);
		this.removeButton.setTitle(this.removeTitle);
		this.showAddressButton.setTitle("show entity's address");

		this.addDomHandler(new MouseOverHandler() {

			@Override
			public void onMouseOver(final MouseOverEvent event) {
				EntityWidget.this.addButton.setVisible(true);
				EntityWidget.this.removeButton.setVisible(true);
				EntityWidget.this.showAddressButton.setVisible(true);
			}
		}, MouseOverEvent.getType());

		this.addDomHandler(new MouseOutHandler() {

			@Override
			public void onMouseOut(final MouseOutEvent event) {
				EntityWidget.this.addButton.setVisible(false);
				EntityWidget.this.removeButton.setVisible(false);
				EntityWidget.this.showAddressButton.setVisible(false);

			}
		}, MouseOutEvent.getType());

		this.addButton.setVisible(false);
		this.removeButton.setVisible(false);
		this.showAddressButton.setVisible(false);

		this.removeClickHandlerRegistration = this.removeButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(final ClickEvent event) {
				final RemoveElementDialog removeDialog = new RemoveElementDialog(
						EntityWidget.this.presenter, EntityWidget.this.address);
				removeDialog.show();

			}
		});

	}

	@UiHandler("addButton")
	void onClickAdd(final ClickEvent event) {
		final AddElementDialog addDialog = new AddElementDialog(this.presenter,
				EntityWidget.this.address, this.addText);
		addDialog.show();
		addDialog.selectEverything();

	}

	@UiHandler("showAddressButton")
	void onClickShow(final ClickEvent event) {
		String entityIdString = "";
		final XType entitysType = this.address.getAddressedType();
		switch (entitysType) {
		case XFIELD:
			entityIdString = " field " + this.address.getField().toString();
			break;
		case XMODEL:
			entityIdString = " model " + this.address.getModel().toString();
			break;
		case XOBJECT:
			entityIdString = " object " + this.address.getObject().toString();
			break;
		default:
			break;
		}
		final AddressDialog addressDialog = new AddressDialog(entityIdString, this.address.toString());
		addressDialog.show();
		addressDialog.selectEverything();

	}

	public void setDeleteModelDialog() {
		this.removeClickHandlerRegistration.removeHandler();
		this.removeButton.addClickHandler(new ClickHandler() {

			@SuppressWarnings("unused")
			@Override
			public void onClick(final ClickEvent event) {

				new RemoveModelDialog(EntityWidget.this.presenter, EntityWidget.this.address);

			}
		});
	}

	public void setStatusDeleted() {
		this.idLabel.addStyleName("deletedStyle");

	}

	public void setRevisionUnknown() {
		this.revisionLabel.setText("???");
	}

	public void setRevisionNumber(final long modelsRevisionNumber) {
		this.revisionLabel.setText("Rev. " + modelsRevisionNumber);

	}

	public void removeStatusDeleted() {
		this.idLabel.removeStyleName("deletedStyle");
	}
}
