package org.xydra.webadmin.gwt.client.widgets.version2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.XX;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XValue;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;


public class TableGenerator {
	
	private static final Logger log = LoggerFactory.getLogger(TableGenerator.class);
	
	List<FieldRow> rows = new ArrayList<FieldRow>();
	HashMap<XID,ValueType> valueTypes = new HashMap<XID,ValueType>();
	List<XID> columnIDs = new ArrayList<XID>();
	
	public void add(FieldRow row) {
		this.rows.add(row);
		
	}
	
	public Grid createTable() {
		
		for(FieldRow row : this.rows) {
			XReadableObject object = row.getObject();
			for(XID xid : object) {
				if(!this.columnIDs.contains(xid)) {
					this.columnIDs.add(xid);
				}
			}
		}
		Grid table = new Grid(this.rows.size() + 1, this.columnIDs.size() + 1);
		
		for(int i = 0; i < this.rows.size() + 1; i++) {
			
			for(int j = 0; j < this.columnIDs.size() + 1; j++) {
				String widgetText = "";
				Widget widget = null;
				
				if(j == 0) {
					
					if(i == 0) {
						widgetText = "id";
						widget = new Label(widgetText);
					} else {
						XReadableObject currentObject = this.rows.get(i - 1).getObject();
						long revision = currentObject.getRevisionNumber();
						
						widget = new ObjectHeadWidget(currentObject.getAddress(), revision);
					}
				} else {
					XID currentColumn = this.columnIDs.get(j - 1);
					
					if(i == 0) {
						
						widget = new Label(currentColumn.toString());
						
					} else {
						XReadableObject currentObject = this.rows.get(i - 1).getObject();
						XAddress currentObjectAddress = currentObject.getAddress();
						final XAddress currentFieldAddress = XX.resolveField(currentObjectAddress,
						        currentColumn);
						if(currentObject.hasField(currentColumn)) {
							XValue fieldValue = currentObject.getField(currentColumn).getValue();
							
							if(fieldValue != null) {
								long revisionNumber = currentObject.getField(currentColumn)
								        .getRevisionNumber();
								
								widget = new FieldWidget(currentFieldAddress, fieldValue,
								        revisionNumber);
							} else {
								widget = new EmptyFieldWidget(currentFieldAddress);
								
							}
							
						} else {
							widget = new EmptyFieldWidget(currentFieldAddress);
						}
					}
					
				}
				
				table.setWidget(i, j, widget);
			}
			
		}
		
		return table;
	}
	
}
