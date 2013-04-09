package org.xydra.webadmin.gwt.client.widgets.editorpanel.tableWidgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.util.DumpUtilsBase.XidComparator;
import org.xydra.core.XX;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.EventHelper;
import org.xydra.webadmin.gwt.client.events.EntityStatus;
import org.xydra.webadmin.gwt.client.events.ModelChangedEvent;
import org.xydra.webadmin.gwt.client.events.ModelChangedEvent.IModelChangedEventHandler;
import org.xydra.webadmin.gwt.client.widgets.editorpanel.EditorPanelPresenter;
import org.xydra.webadmin.gwt.client.widgets.editorpanel.ModelInformationPanel;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;


/**
 * Manages collapse/expand of table object-field-value
 * 
 * @author andi
 */
public class TablePresenter {
	
	public static enum Status {
		
		Deleted,
		
		NotPresent,
		
		Opened,
		
		Present;
	}
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(TablePresenter.class);
	
	private int amountColumns;
	
	private String[] backgroundStyles = { "evenRow", "oddRow" };
	
	private List<XId> columnIDs = new ArrayList<XId>();
	
	private EditorPanelPresenter presenter;
	
	List<XId> rowList = new ArrayList<XId>();
	
	private ScrollPanel scrollPanel;
	
	private Grid contentTable;
	
	private Grid tableHeader;
	
	private VerticalPanel tablePanel;
	
	private ModelInformationPanel panel;
	
	private EditorPanelPresenter editorPanelPresenter;
	
	private HashMap<XAddress,RowPresenter> rowsMap;
	
	public TablePresenter(EditorPanelPresenter editorPanelPresenter,
	        ModelInformationPanel modelInformationPanel) {
		this.editorPanelPresenter = editorPanelPresenter;
		this.panel = modelInformationPanel;
	}
	
	public void generateTableOrShowInformation() {
		this.panel.clear();
		
		SessionCachedModel model = this.editorPanelPresenter.getCurrentModel();
		if(!model.isEmpty()) {
			VerticalPanel tableWidget = createTable(model);
			this.panel.setData(tableWidget);
		} else {
			String problemText = "";
			if(!model.knowsAllObjects()) {
				problemText = "no Objects locally present!";
			} else {
				problemText = "no objects existing at all!";
			}
			Label noObjectsLabel = new Label(problemText);
			VerticalPanel dummyPanel = new VerticalPanel();
			dummyPanel.setStyleName("noData");
			dummyPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
			dummyPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
			dummyPanel.add(noObjectsLabel);
			this.tablePanel.add(dummyPanel);
		}
		
	}
	
	public VerticalPanel createTable(SessionCachedModel model) {
		
		int maxColumnsWithRowHeader = determineColumnHeadersAndBuildPanel(model);
		
		buildTableContent(model, maxColumnsWithRowHeader);
		
		Window.addResizeHandler(new ResizeHandler() {
			
			@Override
			public void onResize(ResizeEvent event) {
				setScrollTableHeight();
			}
			
		});
		
		EventHelper.addModelChangeListener(this.presenter.getCurrentModelAddress(),
		        new IModelChangedEventHandler() {
			        
			        @Override
			        public void onModelChange(ModelChangedEvent event) {
				        processModelChanges(event.getStatus(), event.getMoreInfos());
			        }
			        
		        });
		
		return this.tablePanel;
	}
	
	private int determineColumnHeadersAndBuildPanel(SessionCachedModel model) {
		for(XId objectId : model) {
			XWritableObject object = model.getObject(objectId);
			for(XId xid : object) {
				if(!this.columnIDs.contains(xid)) {
					this.columnIDs.add(xid);
				}
			}
			Collections.sort(this.columnIDs, XidComparator.INSTANCE);
		}
		
		this.amountColumns = this.columnIDs.size();
		int maxColumnsWithRowHeader = this.amountColumns + 1;
		
		this.tableHeader = new Grid(1, maxColumnsWithRowHeader);
		ColumnHeaderWidget idWidget = new ColumnHeaderWidget(XX.toId("ID"));
		idWidget.setStyleName("fieldIDWidget rowHeaderWidth");
		this.tableHeader.setWidget(0, 0, idWidget);
		this.tableHeader.setStyleName("compactTable");
		this.tableHeader.getRowFormatter().setStyleName(0, "oddRow");
		
		int count = 1;
		for(XId columnsID : this.columnIDs) {
			this.tableHeader.setWidget(0, count, new ColumnHeaderWidget(columnsID));
			count++;
		}
		
		return maxColumnsWithRowHeader;
	}
	
	private void buildTableContent(SessionCachedModel model, int maxColumnsWithRowHeader) {
		
		int rowCount = 0;
		
		for(XId objectID : model) {
			this.rowList.add(objectID);
			rowCount++;
		}
		
		Collections.sort(this.rowList, XidComparator.INSTANCE);
		
		initUIComponents(maxColumnsWithRowHeader, rowCount);
		
		int count = 0;
		for(XId objectId : model) {
			
			addRow(model, count, objectId);
			
			count++;
		}
	}
	
	private int addRow(SessionCachedModel model, int verticalRowPosition, XId objectId) {
		XReadableObject currentObject = model.getObject(objectId);
		String backgroundStyle = this.backgroundStyles[verticalRowPosition % 2];
		
		RowPresenter rowPresenter = new RowPresenter(verticalRowPosition, backgroundStyle,
		        currentObject, this);
		rowPresenter.presentRow(currentObject);
		
		XAddress objectAddress = XX
		        .resolveObject(this.presenter.getCurrentModelAddress(), objectId);
		this.rowsMap.put(objectAddress, rowPresenter);
		return verticalRowPosition;
	}
	
	private void processModelChanges(EntityStatus status, XId moreInfos) {
		if(status.equals(EntityStatus.EXTENDED)) {
			this.extendByRow(XX.resolveObject(this.presenter.getCurrentModelAddress(), moreInfos));
		}
		
	}
	
	private void extendByRow(XAddress newObjectsAddress) {
		this.rowList.add(newObjectsAddress.getObject());
		int position = this.rowList.size() - 1;
		this.contentTable.insertRow(position);
		SessionCachedModel model = this.presenter.getCurrentModel();
		
		addRow(model, position, newObjectsAddress.getObject());
	}
	
	//
	// public void scrollToField(XAddress fieldAddress) {
	// this.scrollableFields.get(fieldAddress).scrollToMe();
	// }
	
	private void setScrollTableHeight() {
		String scrollTableHeight = "" + (Window.getClientHeight() - 93) + "px";
		TablePresenter.this.scrollPanel.getElement().setAttribute("style",
		        "overflow-y:overlay; position:relative; zoom:1; height: " + scrollTableHeight);
	}
	
	private void initUIComponents(int maxColumnsWithRowHeader, int rowCount) {
		this.contentTable = new Grid(rowCount, maxColumnsWithRowHeader);
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
		return this.editorPanelPresenter.getCurrentModel();
	}
	
	public void checkAndResizeColumns(XAddress objectAddress, XId newFieldId) {
		if(!this.columnIDs.contains(newFieldId)) {
			this.columnIDs.add(newFieldId);
			int newAmountOfColumns = this.tableHeader.getColumnCount() + 1;
			this.tableHeader.resizeColumns(newAmountOfColumns);
			this.tableHeader.setWidget(0, newAmountOfColumns - 1,
			        new ColumnHeaderWidget(newFieldId));
			
			this.contentTable.resizeColumns(newAmountOfColumns);
			Set<Entry<XAddress,RowPresenter>> rows = this.rowsMap.entrySet();
			for(Entry<XAddress,RowPresenter> row : rows) {
				if(row.getKey().equals(objectAddress)) {
					// nothing
				} else {
					// insert EmptyFieldWidget
					row.getValue().addEmptyFieldWidget(newFieldId);
				}
			}
		}
		
	}
	
	public void expandAll(String expandButtonText) {
		
		Status status = Status.Opened;
		if(expandButtonText.equals("expand all objects")) {
		} else {
			status = Status.Present;
		}
		Set<Entry<XAddress,RowPresenter>> rows = this.rowsMap.entrySet();
		for(Entry<XAddress,RowPresenter> row : rows) {
			row.getValue().handleExpandOrCollapse(status);
		}
		
	}
}
