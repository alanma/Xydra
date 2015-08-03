package org.xydra.webadmin.gwt.client.widgets;

import org.xydra.base.Base;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.datamodels.DataModel;
import org.xydra.webadmin.gwt.client.widgets.editorpanel.EditorPanel;
import org.xydra.webadmin.gwt.client.widgets.editorpanel.EditorPanelPresenter;
import org.xydra.webadmin.gwt.client.widgets.selectiontree.SelectionTree;
import org.xydra.webadmin.gwt.client.widgets.selectiontree.SelectionTreePresenter;
import org.xydra.webadmin.gwt.shared.XyAdminServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

/**
 * Main Class. Singleton, that holds references to the most important global
 * objects:
 *
 * <ul>
 * <li> {@link Controller}
 * <li> {@link DataModel}
 *
 * </ul>
 * from this entity on the UI gets built. It builds the
 *
 * <ul>
 * <li> {@link SelectionTree} and instantiates its presenter, the
 * <li> {@link EditorPanel} and instantiates its presenter, and the
 * <li> {@link AddressWidget} and instantiates its presenter.
 * </ul>
 *
 * Starts the presenters.
 *
 * Puts some default-repository-id-widgets to the UI.
 *
 *
 *
 * @author kahmann
 *
 */
public class XyAdmin extends Composite {

	interface ViewUiBinder extends UiBinder<Widget, XyAdmin> {
	}

	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

	private final Controller controller;
	private DataModel model;

	private EventBus eventbus;

	@UiField
	SelectionTree selectionTree;

	@UiField
	EditorPanel editorPanel;

	@UiField
	AddressWidget addressWidget;

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);

	private static XyAdmin instance;

	public XyAdmin(final XyAdminServiceAsync service) {
		instance = this;

		getModel().addRepoID(Base.toId("repo1"));
		getModel().addRepoID(Base.toId("gae-repo"));
		initWidget(uiBinder.createAndBindUi(this));

		final SelectionTreePresenter selectionTreePresenter = new SelectionTreePresenter(
				this.selectionTree);
		final EditorPanelPresenter editorPanelPresenter = new EditorPanelPresenter(this.editorPanel);
		final AddressWidgetPresenter addressWidgetPresenter = new AddressWidgetPresenter(
				this.addressWidget);

		this.controller = new Controller(service, selectionTreePresenter, editorPanelPresenter,
				addressWidgetPresenter);

		getController().startPresenting();

	}

	public static XyAdmin getInstance() {
		if (instance == null) {
			throw new IllegalStateException("Please init first with a service");
		}
		return instance;
	}

	public synchronized Controller getController() {

		return this.controller;
	}

	public synchronized DataModel getModel() {
		if (this.model == null) {
			this.model = new DataModel();
		}
		return this.model;
	}

	public EventBus getEventBus() {
		if (this.eventbus == null) {
			this.eventbus = GWT.create(SimpleEventBus.class);
		}
		return this.eventbus;
	}

}
