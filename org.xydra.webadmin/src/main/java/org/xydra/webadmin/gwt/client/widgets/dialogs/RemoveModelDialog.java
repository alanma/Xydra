package org.xydra.webadmin.gwt.client.widgets.dialogs;

import org.xydra.base.XAddress;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.EventHelper;
import org.xydra.webadmin.gwt.client.events.CommittingEvent;
import org.xydra.webadmin.gwt.client.events.CommittingEvent.CommitStatus;
import org.xydra.webadmin.gwt.client.events.CommittingEvent.ICommitEventHandler;
import org.xydra.webadmin.gwt.client.util.Presenter;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class RemoveModelDialog extends DialogBox {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);

	interface ViewUiBinder extends UiBinder<Widget, RemoveModelDialog> {
	}

	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	String inputString = "";

	@UiField
	VerticalPanel mainPanel;

	@UiField
	Label infoText;

	@UiField(provided = true)
	ButtonPanel buttonPanel;

	private final XAddress address;
	private final CheckBox checkBox;
	private final HorizontalPanel panel;

	public RemoveModelDialog(final Presenter presenter, final XAddress address) {

		super();

		this.address = address;

		final ClickHandler okHandler = new ClickHandler() {

			@Override
			public void onClick(final ClickEvent event) {
				final Boolean deleteFromRepo = RemoveModelDialog.this.checkBox.getValue();
				presenter.remove(RemoveModelDialog.this.address, deleteFromRepo);
				if (deleteFromRepo) {
					RemoveModelDialog.this.mainPanel.clear();
				} else {
					RemoveModelDialog.this.removeFromParent();
				}
			}
		};

		this.buttonPanel = new ButtonPanel(okHandler, this);

		setWidget(uiBinder.createAndBindUi(this));

		this.infoText.setText("Are you sure you want to delete the item " + address.toString()
				+ "?");

		this.panel = new HorizontalPanel();
		this.panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		this.mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		this.mainPanel.insert(this.panel, 1);
		final HTML text = new HTML("remove permanentely from repository ");
		this.panel.add(text);
		this.checkBox = new CheckBox();
		this.panel.add(this.checkBox);

		this.setStyleName("dialogStyle");
		setText("remove Entity");
		getElement().setId("removeDialog");

		center();

		EventHelper.addCommittingListener(address, new ICommitEventHandler() {

			@Override
			public void onCommit(final CommittingEvent event) {
				processCommitResponse(event.getModelAddress(), event.getStatus(),
						event.getNewRevision());

			}
		});
	}

	protected void processCommitResponse(final XAddress modelAddress, final CommitStatus status,
			final long newRevision) {
		String resultString = "successfully deleted model " + modelAddress.toString();
		switch (status) {
		case FAILED:
			resultString = "error!";
			break;
		case SUCCESS:
			break;
		default:
			break;
		}
		this.mainPanel.add(new Label(resultString));
		addCloseOKButton();
		Controller.showDefaultCursor();

	}

	public void addCloseOKButton() {
		final Button okButton = new Button("ok");
		okButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(final ClickEvent event) {
				RemoveModelDialog.this.removeFromParent();
			}
		});
		this.mainPanel.add(okButton);

	}

}
