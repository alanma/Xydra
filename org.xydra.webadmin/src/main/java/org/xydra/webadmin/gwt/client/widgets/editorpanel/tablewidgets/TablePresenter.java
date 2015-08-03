package org.xydra.webadmin.gwt.client.widgets.editorpanel.tablewidgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.util.DumpUtilsBase.XidComparator;
import org.xydra.core.XX;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.webadmin.gwt.client.EventHelper;
import org.xydra.webadmin.gwt.client.events.EntityStatus;
import org.xydra.webadmin.gwt.client.events.ModelChangedEvent;
import org.xydra.webadmin.gwt.client.events.ModelChangedEvent.IModelChangedEventHandler;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.editorpanel.EditorPanelPresenter;
import org.xydra.webadmin.gwt.client.widgets.editorpanel.ModelInformationPanel;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Performs logic for table. So it:
 *
 * <ul>
 * <li>controls the table representing the model
 * <li>fulfills requests given via the {@link RowHeaderWidget}
 * <li>has Listeners to {@link ModelChangedEvent}
 * </ul>
 *
 * The listeners react, when
 * <ul>
 * <li>a model is extended by an object: A new row gets added
 * </ul>
 *
 * @author kahmann
 *
 */
public class TablePresenter {

	public static enum Status {

		Deleted,

		NotPresent,

		Opened,

		Present;
	}

	private static final Logger log = LoggerFactory.getLogger(TablePresenter.class);

	private int amountColumns;

	private final String[] backgroundStyles = { "evenRow", "oddRow" };

	private final List<XId> columnIDs = new ArrayList<XId>();

	private final EditorPanelPresenter presenter;

	List<XId> rowList = new ArrayList<XId>();

	private ScrollPanel scrollPanel;

	private Grid contentTable;

	private Grid tableHeader;

	private VerticalPanel tablePanel;

	private final ModelInformationPanel panel;

	private final HashMap<XAddress, RowPresenter> rowsMap = new HashMap<XAddress, RowPresenter>();

	private HandlerRegistration handlerRegistration;

	public TablePresenter(final EditorPanelPresenter editorPanelPresenter,
			final ModelInformationPanel modelInformationPanel) {
		this.presenter = editorPanelPresenter;
		this.panel = modelInformationPanel;

	}

	public void generateTableOrShowInformation() {
		if (this.handlerRegistration != null) {
			XyAdmin.getInstance().getController().removeRegistration(this.handlerRegistration);
			this.handlerRegistration.removeHandler();
		}

		this.panel.clear();

		final SessionCachedModel model = this.presenter.getCurrentModel();
		if (!model.isEmpty()) {
			final VerticalPanel tableWidget = createTable(model);
			this.panel.setData(tableWidget);
			log.info("table for model " + this.presenter.getCurrentModelAddress().toString()
					+ " built, now firing ViewBuiltEvent!");

			Scheduler.get().scheduleDeferred(new ScheduledCommand() {

				@Override
				public void execute() {
					EventHelper.fireViewBuiltEvent(TablePresenter.this.presenter
							.getCurrentModelAddress());
				}
			});

		} else {
			String problemText = "";
			if (!model.knowsAllObjects()) {
				problemText = "No objects locally present!";
			} else {
				problemText = "No objects existing at all!";
			}
			final Label noObjectsLabel = new Label(problemText);
			final VerticalPanel dummyPanel = new VerticalPanel();
			dummyPanel.setStyleName("noData");
			dummyPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			dummyPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
			dummyPanel.add(noObjectsLabel);
			this.panel.setData(dummyPanel);
		}

		this.handlerRegistration = EventHelper.addModelChangedListener(
				this.presenter.getCurrentModelAddress(), new IModelChangedEventHandler() {

					@Override
					public void onModelChange(final ModelChangedEvent event) {
						handleModelChanges(event.getStatus(), event.getMoreInfos());
					}

				});
		XyAdmin.getInstance().getController().addRegistration(this.handlerRegistration);
		log.info("handler registrated!");

	}

	private VerticalPanel createTable(final SessionCachedModel model) {

		final int maxColumnsWithRowHeader = determineColumnHeadersAndBuildPanel(model) + 1;

		buildTableContent(model, maxColumnsWithRowHeader);

		Window.addResizeHandler(new ResizeHandler() {

			@Override
			public void onResize(final ResizeEvent event) {
				setScrollTableHeight();
			}

		});

		return this.tablePanel;
	}

	private int determineColumnHeadersAndBuildPanel(final SessionCachedModel model) {
		for (final XId objectId : model) {
			final XWritableObject object = model.getObject(objectId);
			for (final XId xid : object) {
				if (!this.columnIDs.contains(xid)) {
					this.columnIDs.add(xid);
				}
			}
			Collections.sort(this.columnIDs, XidComparator.INSTANCE);
		}

		this.amountColumns = this.columnIDs.size();
		final int maxColumnsWithRowHeader = this.amountColumns + 1;

		this.tableHeader = new Grid(1, maxColumnsWithRowHeader);
		final ColumnHeaderWidget idWidget = new ColumnHeaderWidget(Base.toId("ID"));
		idWidget.setStyleName("fieldIDWidget rowHeaderWidth");
		this.tableHeader.setWidget(0, 0, idWidget);
		this.tableHeader.setStyleName("compactTable");
		this.tableHeader.getRowFormatter().setStyleName(0, "oddRow");

		int count = 1;
		for (final XId columnsID : this.columnIDs) {
			this.tableHeader.setWidget(0, count, new ColumnHeaderWidget(columnsID));
			count++;
		}

		return maxColumnsWithRowHeader;
	}

	private void buildTableContent(final SessionCachedModel model, final int maxColumnsWithRowHeader) {

		this.rowList.clear();
		int rowCount = 0;

		for (final XId objectID : model) {
			this.rowList.add(objectID);
			rowCount++;
		}

		Collections.sort(this.rowList, XidComparator.INSTANCE);

		initUIComponents(maxColumnsWithRowHeader, rowCount);

		int count = 0;
		for (final XId objectId : model) {

			addRow(model, count, objectId);

			count++;
		}
	}

	private int addRow(final SessionCachedModel model, final int verticalRowPosition, final XId objectId) {
		final XReadableObject currentObject = model.getObject(objectId);
		final String backgroundStyle = this.backgroundStyles[verticalRowPosition % 2];

		final RowPresenter rowPresenter = new RowPresenter(verticalRowPosition, backgroundStyle,
				currentObject, this);
		rowPresenter.presentRow(currentObject);
		final XAddress currentModelAddress = this.presenter.getCurrentModelAddress();
		final XAddress objectAddress = Base.resolveObject(currentModelAddress, objectId);
		this.rowsMap.put(objectAddress, rowPresenter);
		return verticalRowPosition;
	}

	private void handleModelChanges(final EntityStatus status, final XId moreInfos) {
		if (status.equals(EntityStatus.EXTENDED)) {
			if (this.contentTable == null) {
				generateTableOrShowInformation();

			} else {
				extendByRow(Base.resolveObject(this.presenter.getCurrentModelAddress(),
						moreInfos));
			}
		}

	}

	private void extendByRow(final XAddress newObjectsAddress) {
		this.rowList.add(newObjectsAddress.getObject());
		final int position = this.rowList.size() - 1;
		log.info("rowListSize: " + this.rowList.size() + ", position: " + position);
		this.contentTable.insertRow(position);
		final SessionCachedModel model = this.presenter.getCurrentModel();
		log.info("adding row for object " + newObjectsAddress.toString());

		addRow(model, position, newObjectsAddress.getObject());
	}

	//
	// public void scrollToField(XAddress fieldAddress) {
	// this.scrollableFields.get(fieldAddress).scrollToMe();
	// }

	private void setScrollTableHeight() {
		final String scrollTableHeight = "" + (Window.getClientHeight() - 93) + "px";
		TablePresenter.this.scrollPanel.getElement().setAttribute("style",
				"overflow-y:overlay; position:relative; zoom:1; height: " + scrollTableHeight);
	}

	private void initUIComponents(final int maxColumnsWithRowHeader, final int rowCount) {
		this.contentTable = new Grid(rowCount, maxColumnsWithRowHeader - 1);
		this.contentTable.setStyleName("compactTable");
		this.scrollPanel = new ScrollPanel();
		this.scrollPanel.add(this.contentTable);
		this.scrollPanel.setStyleName("scrollTableStyle");
		setScrollTableHeight();
		this.tablePanel = new VerticalPanel();
		this.tablePanel.add(this.tableHeader);
		this.tablePanel.add(this.scrollPanel);
	}

	public Grid getContentTable() {
		return this.contentTable;
	}

	public List<XId> getColumnIds() {
		return this.columnIDs;
	}

	public SessionCachedModel getModel() {
		return this.presenter.getCurrentModel();
	}

	public void checkAndResizeColumns(final XAddress objectAddress, final XId newFieldId) {
		if (!this.columnIDs.contains(newFieldId)) {
			this.columnIDs.add(newFieldId);
			final int newAmountOfColumns = this.tableHeader.getColumnCount() + 1;
			this.tableHeader.resizeColumns(newAmountOfColumns);
			this.tableHeader.setWidget(0, newAmountOfColumns - 1,
					new ColumnHeaderWidget(newFieldId));
			log.info("extended tableHeader by column " + newFieldId.toString() + ", now we have "
					+ newAmountOfColumns + " columns");

			this.contentTable.resizeColumns(newAmountOfColumns);
			final Set<Entry<XAddress, RowPresenter>> rows = this.rowsMap.entrySet();
			for (final Entry<XAddress, RowPresenter> row : rows) {
				if (row.getKey().equals(objectAddress)) {
					// nothing
				} else {
					// insert EmptyFieldWidget
					log.info("we come here...!");
					row.getValue().addEmptyFieldWidget(newFieldId);
				}
			}
		}

	}

	public void expandAll(final String expandButtonText) {

		Status status = Status.Present;
		if (expandButtonText.equals("expand all objects")) {
		} else {
			status = Status.Opened;
		}
		final Set<Entry<XAddress, RowPresenter>> rows = this.rowsMap.entrySet();
		for (final Entry<XAddress, RowPresenter> row : rows) {
			row.getValue().handleExpandOrCollapse(status);
		}

	}

	public void showObjectAndField(final XAddress desiredAddress) {
		final RowPresenter rowPresenter = this.rowsMap.get(Base.resolveObject(desiredAddress));
		rowPresenter.handleExpandOrCollapse(Status.Opened);
		final XId fieldId = desiredAddress.getField();
		if (fieldId != null) {
			rowPresenter.scrollToField(fieldId);
		}
		EventHelper.fireViewBuiltEvent(Base.toAddress(desiredAddress.getRepository(),
				desiredAddress.getModel(), desiredAddress.getObject(), null));

	}
}
