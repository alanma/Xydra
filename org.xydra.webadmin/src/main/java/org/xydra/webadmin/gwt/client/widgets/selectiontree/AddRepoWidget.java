package org.xydra.webadmin.gwt.client.widgets.selectiontree;

import org.xydra.base.Base;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class AddRepoWidget extends Composite {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AddRepoWidget.class);

	interface ViewUiBinder extends UiBinder<Widget, AddRepoWidget> {
	}

	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

	private final SelectionTreePresenter presenter;

	public AddRepoWidget(final SelectionTreePresenter presenter) {

		this.presenter = presenter;
		buildComponents();
	}

	private void buildComponents() {

		initWidget(uiBinder.createAndBindUi(this));

	}

	@UiHandler("addRepoButton")
	void onClickAdd(final ClickEvent event) {

		this.presenter.openAddElementDialog(Base.toAddress("/noRepo"), "enter Repository ID");
	}
}
