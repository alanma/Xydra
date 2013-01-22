package org.xydra.webadmin.gwt.client.widgets.version2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.xydra.base.XID;
import org.xydra.base.rmof.XReadableObject;
import org.xydra.base.value.XValue;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;


public class TableGenerator {
	
	List<FieldRow> rows = new ArrayList<FieldRow>();
	HashMap<XID,HashMap<Integer,Widget>> map = new HashMap<XID,HashMap<Integer,Widget>>();
	List<XID> columnIDs = new ArrayList<XID>();
	
	public void add(FieldRow row) {
		this.rows.add(row);
		
	}
	
	public Grid createTable() {
		
		int rowCounter = 0;
		int columnCounter = 0;
		for(FieldRow row : this.rows) {
			XReadableObject object = row.getObject();
			int tempColumnCounter = 0;
			for(XID field : object) {
				HashMap<Integer,Widget> valueMap = this.map.get(field);
				if(valueMap == null) {
					valueMap = new HashMap<Integer,Widget>();
					this.map.put(field, valueMap);
				}
				XValue fieldValue = object.getField(field).getValue();
				String labelString = "null";
				if(fieldValue != null)
					labelString = fieldValue.toString();
				valueMap.put(rowCounter, new Label(labelString));
				tempColumnCounter++;
				
				this.columnIDs.add(field);
			}
			if(tempColumnCounter > columnCounter) {
				columnCounter = tempColumnCounter;
			}
			rowCounter++;
		}
		
		int rowsPlusHeader = rowCounter + 1;
		int columnsPlusFirstOne = columnCounter + 1;
		Grid table = new Grid(rowsPlusHeader, columnsPlusFirstOne);
		table.setText(0, 0, "id");
		
		int columnCount = 1;
		for(XID id : this.map.keySet()) {
			
			table.setText(0, columnCount, id.toString());
			HashMap<Integer,Widget> valueMap = this.map.get(id);
			
			Set<Integer> rowsWithInformation = valueMap.keySet();
			for(Integer rowNumber : rowsWithInformation) {
				table.setWidget(rowNumber + 1, columnCount, valueMap.get(rowNumber));
			}
			columnCount++;
		}
		
		// for(int i = 0; i < this.rows.size(); i++) {
		// table.setText(i + 1, 0, this.rows.get(i).getID().toString());
		// }
		
		for(int i = 0; i < this.rows.size(); i++) {
			XReadableObject currentObject = this.rows.get(i).getObject();
			
			for(int j = 0; j < this.columnIDs.size(); j++) {
				XID currentColumn = this.columnIDs.get(j);
				
				String widgetText = "";
				if(i == 0) {
					widgetText = currentColumn.toString();
				} else {
					if(currentObject.hasField(currentColumn)) {
						
					}
				}
				Label widget = new Label(widgetText);
				table.setWidget(i, j, widget);
				
			}
			
		}
		
		return table;
	}
}
