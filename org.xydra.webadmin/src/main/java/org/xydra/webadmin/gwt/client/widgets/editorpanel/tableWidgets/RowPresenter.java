package org.xydra.webadmin.gwt.client.widgets.editorpanel.tableWidgets;

import java.util.HashMap;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.rmof.XWritableField;
import org.xydra.base.rmof.XWritableObject;
import org.xydra.base.value.XValue;
import org.xydra.core.XX;
import org.xydra.core.change.SessionCachedModel;
import org.xydra.webadmin.gwt.client.EventHelper;
import org.xydra.webadmin.gwt.client.events.EntityStatus;
import org.xydra.webadmin.gwt.client.events.ObjectChangedEvent;
import org.xydra.webadmin.gwt.client.events.ObjectChangedEvent.IObjectChangedEventHandler;
import org.xydra.webadmin.gwt.client.util.Presenter;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;
import org.xydra.webadmin.gwt.client.widgets.editorpanel.tableWidgets.TablePresenter.Status;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Widget;


public class RowPresenter extends Presenter {
	
	private int verticalPositionInTable;
	private String backgroundStyle;
	private TablePresenter tablePresenter;
	private TablePresenter.Status status;
	private HashMap<XId,TableFieldWidget> scrollableFields = new HashMap<XId,TableFieldWidget>();
	private XAddress objectAddress;
	
	public RowPresenter(int verticalPositionInTable, String backgroundStyle,
	        XReadableObject currentObject, TablePresenter tablePresenter) {
		super();
		this.verticalPositionInTable = verticalPositionInTable;
		this.backgroundStyle = backgroundStyle;
		this.tablePresenter = tablePresenter;
	}
	
	public void presentRow(XReadableObject currentObject) {
		
		this.objectAddress = currentObject.getAddress();
		Grid table = this.tablePresenter.getContentTable();
		List<XId> columnIds = this.tablePresenter.getColumnIds();
		int amountColumns = columnIds.size();
		
		CellFormatter formatter = table.getCellFormatter();
		
		for(int j = 0; j <= amountColumns + 1; j++) {
			Widget widget = null;
			if(j == 0) {
				// if we have to insert a row header, insert the appropriate
				// widget
				String expandButtonText = " o p e n ";
				if(this.status.equals(Status.Present)) {
					expandButtonText = " c l o s e ";
				}
				widget = new RowHeaderWidget(this, expandButtonText);
			} else {
				if(this.status.equals(Status.Opened)) {
					// if this object is opened by the user
					int currentColumnIndex = j - 1;
					if(currentColumnIndex > amountColumns - 1) {
						// we have no column for this widget
						continue;
					}
					XId currentColumn = columnIds.get(currentColumnIndex);
					
					if(currentObject.hasField(currentColumn)) {
						
						FieldWidget widget2 = new FieldWidget(this, currentColumn);
						formatter.setVerticalAlignment(this.verticalPositionInTable, j,
						        HasVerticalAlignment.ALIGN_TOP);
						
						this.scrollableFields.put(currentColumn, widget2);
						
						widget = widget2;
					} else {
						widget = new EmptyFieldWidget(this, currentColumn);
					}
					
					table.getRowFormatter().setStyleName(this.verticalPositionInTable,
					        this.backgroundStyle);
				} else {
					table.getRowFormatter().removeStyleName(this.verticalPositionInTable,
					        this.backgroundStyle);
				}
			}
			table.setWidget(this.verticalPositionInTable, j, widget);
			formatter.setHorizontalAlignment(this.verticalPositionInTable, j,
			        HasHorizontalAlignment.ALIGN_CENTER);
		}
		formatter.setStyleName(this.verticalPositionInTable, 0, this.backgroundStyle);
		
		EventHelper.addObjectChangedListener(this.objectAddress, new IObjectChangedEventHandler() {
			
			@Override
			public void onObjectChange(ObjectChangedEvent event) {
				RowPresenter.this.processChange(event.getStatus(), event.getMoreInfos());
				
			}
		});
	}
	
	protected void processChange(EntityStatus status2, XId newFieldId) {
		XWritableObject currentObject = this.tablePresenter.getModel().getObject(
		        this.objectAddress.getObject());
		if(status2.equals(EntityStatus.EXTENDED)) {
			
			this.tablePresenter.checkAndResizeColumns(this.objectAddress, newFieldId);
			/* redraw the whole row */
			this.presentRow(currentObject);
			
		} else if(status2.equals(EntityStatus.CHANGED)) {
			this.clearRow();
			/* redraw the whole row */
			this.presentRow(currentObject);
			
		} else if(status2.equals(EntityStatus.DELETED)) {
			this.clearRow();
		}
	}
	
	private void clearRow() {
		Grid table = this.tablePresenter.getContentTable();
		for(int i = 0; i < table.getCellCount(this.verticalPositionInTable); i++) {
			table.clearCell(this.verticalPositionInTable, i);
		}
		
	}
	
	public XAddress getAddress() {
		return this.objectAddress;
	}
	
	public void handleExpandOrCollapse(Status status) {
		this.status = status;
		SessionCachedModel model = this.tablePresenter.getModel();
		XWritableObject object = model.getObject(this.objectAddress.getObject());
		this.presentRow(object);
	}
	
	public void handleExpandOrCollapse() {
		if(this.status.equals(Status.Opened)) {
			this.status = Status.Present;
		} else {
			this.status = Status.Opened;
		}
		
		handleExpandOrCollapse(this.status);
		
	}
	
	public XValue getFieldValue(XId id) {
		SessionCachedModel model = this.tablePresenter.getModel();
		XWritableObject object = model.getObject(this.objectAddress.getObject());
		XWritableField field = object.getField(id);
		return field.getValue();
	}
	
	public long getFieldRevisionNumber(XId id) {
		SessionCachedModel model = this.tablePresenter.getModel();
		XWritableObject object = model.getObject(this.objectAddress.getObject());
		XWritableField field = object.getField(id);
		
		return field.getRevisionNumber();
	}
	
	public void deleteField(XId id) {
		this.remove(XX.resolveField(this.objectAddress, id));
		
	}
	
	public void changeFieldValue(XId id, XValue newValue) {
		XyAdmin.getInstance().getModel()
		        .changeValue(XX.resolveField(this.objectAddress, id), newValue);
		
	}
	
	public void remove(XId id) {
		super.remove(XX.resolveField(this.objectAddress, id));
		
	}
	
	public void addField(XId id) {
		super.processUserInput(XX.resolveField(this.objectAddress, id), null);
		
	}
	
	public void addEmptyFieldWidget(XId newFieldId) {
		Grid table = this.tablePresenter.getContentTable();
		table.setWidget(this.verticalPositionInTable, table.getRowCount() - 1,
		        new EmptyFieldWidget(this, newFieldId));
	}
}
