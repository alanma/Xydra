package org.xydra.webadmin.gwt.client.widgets;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.gwt.editor.value.XAddressEditor;
import org.xydra.webadmin.gwt.client.widgets.dialogs.WarningDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

public class AddressWidget extends Composite {

	interface ViewUiBinder extends UiBinder<Widget, AddressWidget> {
	}

	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	String inputString = "";

	@UiField
	HorizontalPanel mainPanel;

	@UiField(provided = true)
	XAddressEditor addressEditor;

	@UiField
	Button loadLocationButton;

	@UiField
	Button addElementButton;

	@UiField
	Button deleteElementButton;

	@UiField
	Button clearButton;
	private AddressWidgetPresenter presenter;

	public AddressWidget() {

		super();

		this.addressEditor = new XAddressEditor(Base.toAddress(Base.toId("repo1"), null, null, null),
				null);
		initWidget(uiBinder.createAndBindUi(this));
	}

	void init() {

	}

	@UiHandler("loadLocationButton")
	void onClickLoad(final ClickEvent e) {
		// showNYIDialog();
		final XAddress address = getAddress();
		this.presenter.openAddress(address);

	}

	private XAddress getAddress() {
		XAddress address = null;
		try {
			address = this.addressEditor.getValue();

		} catch (final Exception ex) {
			showDialog(ex.getLocalizedMessage());
		}
		return address;
	}

	@UiHandler("clearButton")
	void onClickClear(final ClickEvent e) {
		this.addressEditor.setValue(Base.toAddress(Base.toId("repo1"), null, null, null));
	}

	@UiHandler("addElementButton")
	void onClickAdd(final ClickEvent e) {
		this.presenter.addEntity(getAddress());

	}

	@UiHandler("deleteElementButton")
	void onClickDelete(final ClickEvent e) {
		this.presenter.removeEntity(getAddress());
	}

	void showDialog(final String message) {
		final WarningDialog dialog = new WarningDialog(message);
		dialog.show();
	}

	public void registerPresenter(final AddressWidgetPresenter addressWidgetPresenter) {
		this.presenter = addressWidgetPresenter;

	}
}
