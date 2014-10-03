package org.xydra.webadmin.gwt.client.widgets.dialogs;

import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ObjectChangesPanel extends Composite {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);

	interface ViewUiBinder extends UiBinder<Widget, ObjectChangesPanel> {
	}

	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

	@UiField(provided = true)
	HorizontalPanel objectIdPanel;

	@UiField(provided = true)
	VerticalPanel fieldChangesPanel;

	public ObjectChangesPanel(String backgroundColor, HorizontalPanel objectIdPanel,
			VerticalPanel fieldChangesPanel) {
		super();
		this.fieldChangesPanel = fieldChangesPanel;
		this.objectIdPanel = objectIdPanel;
		initWidget(uiBinder.createAndBindUi(this));
		this.getElement().setAttribute("style", "background-color: " + backgroundColor);
	}
}
