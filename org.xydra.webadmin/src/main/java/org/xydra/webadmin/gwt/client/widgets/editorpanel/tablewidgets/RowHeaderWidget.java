package org.xydra.webadmin.gwt.client.widgets.editorpanel.tablewidgets;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class RowHeaderWidget extends Composite {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);

	interface ViewUiBinder extends UiBinder<Widget, RowHeaderWidget> {
	}

	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

	@UiField
	Button expandButton;

	@UiField(provided = true)
	EntityWidget entityWidget;

	private final RowPresenter presenter;

	public RowHeaderWidget(final RowPresenter presenter, final String expandButtonText) {
		this.presenter = presenter;
		this.entityWidget = new EntityWidget(presenter, presenter.getAddress(), new ClickHandler() {

			@Override
			public void onClick(final ClickEvent event) {
				RowHeaderWidget.this.expandButton.click();
			}
		});

		initWidget(uiBinder.createAndBindUi(this));

		this.expandButton.setText(expandButtonText);

	}

	@UiHandler("expandButton")
	void onClickExpand(final ClickEvent e) {

		this.presenter.handleExpandOrCollapse();
	}
}
