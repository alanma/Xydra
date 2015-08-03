package org.xydra.webadmin.gwt.client.widgets.dialogs;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.value.XValue;
import org.xydra.core.XX;
import org.xydra.gwt.editor.value.XIdEditor;
import org.xydra.gwt.editor.value.XValueEditor.EditListener;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.webadmin.gwt.client.util.Presenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class AddElementDialog extends DialogBox {

	private static final Logger log = LoggerFactory.getLogger(AddElementDialog.class);

	interface ViewUiBinder extends UiBinder<Widget, AddElementDialog> {
	}

	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	String inputString = "";

	@UiField
	VerticalPanel mainPanel;

	@UiField(provided = true)
	XIdEditor textArea;

	@UiField
	Label errorLabel;

	@UiField(provided = true)
	ButtonPanel buttonPanel;

	private final XAddress address;

	public AddElementDialog(final Presenter presenter, final XAddress address, final String text) {

		super();

		this.address = address;

		final ClickHandler okHandler = new ClickHandler() {

			@Override
			public void onClick(final ClickEvent event) {
				String value = "";
				try {
					value = AddElementDialog.this.textArea.getValue().toString();
					Presenter.processUserInput(AddElementDialog.this.address, value);
					AddElementDialog.this.removeFromParent();
				} catch (final Exception e) {
					String errorMessage = e.getMessage();
					for (int i = 0; i < e.getStackTrace().length; i++) {
						errorMessage += e.getStackTrace()[i] + "\n";
					}
					log.error(errorMessage);
					AddElementDialog.this.errorLabel.setText(e.getMessage());
				}

			}
		};

		this.buttonPanel = new ButtonPanel(okHandler, this);
		this.textArea = new XIdEditor(Base.toId("newID"), new EditListener() {

			@Override
			public void newValue(final XValue value) {
				// nothing

			}
		});
		this.textArea.getTextBox().addKeyDownHandler(new KeyDownHandler() {

			@Override
			public void onKeyDown(final KeyDownEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					AddElementDialog.this.buttonPanel.clickOk();
				}
			}

		});
		setWidget(uiBinder.createAndBindUi(this));

		this.setStyleName("dialogStyle");

		final String[] addressedSuccessors = { "nothing", "Object", "Field", "Model" };
		String dialogTitle = addressedSuccessors[address.getAddressedType().ordinal()];
		if (address.getRepository().equals(Base.toId("noRepo"))) {
			dialogTitle = "Repository";
		}
		setText("add " + dialogTitle);
		center();
	}

	public void selectEverything() {
		this.textArea.selectEverything();
	}

}
