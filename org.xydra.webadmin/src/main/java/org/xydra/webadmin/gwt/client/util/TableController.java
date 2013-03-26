package org.xydra.webadmin.gwt.client.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.XX;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.core.util.DumpUtils.XidComparator;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.datamodels.DataModel;
import org.xydra.webadmin.gwt.client.widgets.tablewidgets.ColumnHeaderWidget;
import org.xydra.webadmin.gwt.client.widgets.tablewidgets.EmptyFieldWidget;
import org.xydra.webadmin.gwt.client.widgets.tablewidgets.FieldWidget;
import org.xydra.webadmin.gwt.client.widgets.tablewidgets.RowHeaderWidget;
import org.xydra.webadmin.gwt.client.widgets.tablewidgets.TableFieldWidget;

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class TableController {
	
	public static enum Status {
		
		NotPresent,
		
		Present,
		
		Opened, Deleted;
		
	}
	
	private static final Logger log = LoggerFactory.getLogger(TableController.class);
	
	List<XId> columnIDs = new ArrayList<XId>();
	List<XId> rowList = new ArrayList<XId>();
	HashMap<XId,Status> objectStatus = new HashMap<XId,TableController.Status>();
	HashMap<XAddress,TableFieldWidget> scrollableFields = new HashMap<XAddress,TableFieldWidget>();
	private int rowCount = 0;
	
	private VerticalPanel tablePanel;
	private Grid tableHeader;
	private ScrollPanel scrollPanel;
	private Grid table;
	
	private int amountColumns;
	
	private String[] backgroundStyles = { "evenRow", "oddRow" };
	
	public TableController() {
		Controller.getInstance().registerTableController(this);
	}
	
	public VerticalPanel createTable(SessionCachedModel model) {
		
		setObjectStatuses(model);
		
		int maxColumnsWithRowHeader = buildColumnHeaders(model);
		
		setTableContent(model, maxColumnsWithRowHeader);
		
		Window.addResizeHandler(new ResizeHandler() {
			
			@Override
			public void onResize(ResizeEvent event) {
				setScrollTableHeight();
			}
			
		});
		
		return this.tablePanel;
	}
	
	private void setTableContent(SessionCachedModel model, int maxColumnsWithRowHeader) {
		int rowCountWithHeader = this.rowCount;
		this.table = new Grid(rowCountWithHeader, maxColumnsWithRowHeader);
		this.table.setStyleName("compactTable");
		
		this.scrollPanel = new ScrollPanel();
		this.scrollPanel.add(this.table);
		
		this.scrollPanel.setStyleName("scrollTableStyle");
		setScrollTableHeight();
		this.tablePanel = new VerticalPanel();
		this.tablePanel.add(this.tableHeader);
		this.tablePanel.add(this.scrollPanel);
		
		for(XId objectID : model) {
			
			this.rowList.add(objectID);
			
		}
		Collections.sort(this.rowList, XidComparator.INSTANCE);
		for(XId objectID : this.rowList) {
			
			insertRow(objectID, model);
			
		}
		
		CellFormatter cellFormatter = this.table.getCellFormatter();
		for(int i = 0; i < this.amountColumns; i++) {
			cellFormatter.setHorizontalAlignment(0, i, HasHorizontalAlignment.ALIGN_CENTER);
		}
	}
	
	private int buildColumnHeaders(SessionCachedModel model) {
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
	
	private void setObjectStatuses(SessionCachedModel model) {
		for(XId objectID : model) {
			boolean isPresent = model.isKnownObject(objectID);
			Status objectsStatus = Status.NotPresent;
			if(isPresent) {
				objectsStatus = Status.Present;
			}
			this.objectStatus.put(objectID, objectsStatus);
			this.rowCount++;
		}
	}
	
	public void notifyTable(XAddress eventLocation, Status status) {
		
		XId objectId = eventLocation.getObject();
		if(status.equals(Status.Deleted)) {
			removeRow(objectId);
			this.rowList.remove(objectId);
			this.objectStatus.remove(objectId);
		} else {
			
			if(!this.rowList.contains(objectId)) {
				this.rowList.add(objectId);
				int position = this.rowList.indexOf(objectId);
				this.table.insertRow(position);
			}
			
			this.objectStatus.put(objectId, status);
			
			SessionCachedModel model = DataModel.getInstance()
			        .getRepo(eventLocation.getRepository()).getModel(eventLocation.getModel());
			
			XId fieldID = eventLocation.getField();
			if(fieldID != null) {
				if(!this.columnIDs.contains(fieldID)) {
					this.columnIDs.add(fieldID);
					if(this.columnIDs.size() > this.amountColumns) {
						this.amountColumns++;
						this.tableHeader.resizeColumns(this.amountColumns + 1);
						this.table.resizeColumns(this.amountColumns + 1);
						
						log.info("extended table by 1");
					}
					this.tableHeader.setWidget(0, this.columnIDs.size(), new ColumnHeaderWidget(
					        fieldID));
				}
			}
			
			insertRow(objectId, model);
		}
	}
	
	private void removeRow(XId id) {
		int position = this.rowList.indexOf(id);
		this.table.removeRow(position);
		
	}
	
	private void insertRow(XId id, SessionCachedModel model) {
		
		CellFormatter formatter = this.table.getCellFormatter();
		int rowPosition = this.rowList.indexOf(id);
		String backGroundStyle = this.backgroundStyles[rowPosition % 2];
		
		XReadableObject currentObject = model.getObject(id);
		
		for(int j = 0; j <= this.amountColumns; j++) {
			Widget widget = null;
			
			if(j == 0) {
				// if we have to insert a row header, insert the appropriate
				// widget
				
				widget = new RowHeaderWidget(currentObject.getAddress(), this.objectStatus.get(id));
			} else {
				
				if(this.objectStatus.get(id).equals(Status.Opened)) {
					// if this object is opened by the user
					int currentColumnIndex = j - 1;
					if(currentColumnIndex > this.columnIDs.size() - 1) {
						// we have no column for this widget
						continue;
					}
					XId currentColumn = this.columnIDs.get(currentColumnIndex);
					
					XAddress currentObjectAddress = currentObject.getAddress();
					final XAddress currentFieldAddress = XX.resolveField(currentObjectAddress,
					        currentColumn);
					if(currentObject.hasField(currentColumn)) {
						
						XReadableField field = currentObject.getField(currentColumn);
						FieldWidget widget2 = new FieldWidget(field);
						formatter.setVerticalAlignment(rowPosition, j,
						        HasVerticalAlignment.ALIGN_TOP);
						
						this.scrollableFields.put(field.getAddress(), widget2);
						
						widget = widget2;
					} else {
						widget = new EmptyFieldWidget(currentFieldAddress);
					}
					
					this.table.getRowFormatter().setStyleName(rowPosition, backGroundStyle);
				} else {
					this.table.getRowFormatter().removeStyleName(rowPosition, backGroundStyle);
				}
			}
			
			this.table.setWidget(rowPosition, j, widget);
		}
		
		this.table.getCellFormatter().setStyleName(rowPosition, 0, backGroundStyle);
		
	}
	
	private void setScrollTableHeight() {
		String scrollTableHeight = "" + (Window.getClientHeight() - 93) + "px";
		TableController.this.scrollPanel.getElement().setAttribute("style",
		        "overflow-y:overlay; position:relative; zoom:1; height: " + scrollTableHeight);
	}
	
	public void expandObject(XAddress desiredAddress) {
		this.notifyTable(desiredAddress, Status.Opened);
	}
	
	public void scrollToField(XAddress fieldAddress) {
		this.scrollableFields.get(fieldAddress).scrollToMe();
	}
}
