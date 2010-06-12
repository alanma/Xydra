package org.xydra.client.gwt.client.editor;

import org.xydra.client.gwt.client.editor.value.XListEditor;
import org.xydra.client.gwt.client.editor.value.XValueEditor;
import org.xydra.client.gwt.client.editor.value.XValueUtils;
import org.xydra.client.gwt.client.editor.value.XValueEditor.EditListener;
import org.xydra.client.gwt.sync.XModelSynchronizer;
import org.xydra.core.XX;
import org.xydra.core.change.XFieldCommand;
import org.xydra.core.change.XFieldEvent;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.core.change.impl.memory.MemoryFieldCommand;
import org.xydra.core.change.impl.memory.MemoryObjectCommand;
import org.xydra.core.model.XID;
import org.xydra.core.model.XLoggedField;
import org.xydra.core.value.XBooleanListValue;
import org.xydra.core.value.XBooleanValue;
import org.xydra.core.value.XDoubleListValue;
import org.xydra.core.value.XDoubleValue;
import org.xydra.core.value.XIDListValue;
import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XIntegerListValue;
import org.xydra.core.value.XIntegerValue;
import org.xydra.core.value.XListValue;
import org.xydra.core.value.XLongListValue;
import org.xydra.core.value.XLongValue;
import org.xydra.core.value.XStringListValue;
import org.xydra.core.value.XStringValue;
import org.xydra.core.value.XValue;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;



public class XFieldEditor extends VerticalPanel implements EditListener, XFieldEventListener {
	
	private static final int IDX_NOVALUE = 0;
	private static final int IDX_LIST_STRING = 1;
	private static final int IDX_STRING = 2;
	private static final int IDX_LIST_XID = 3;
	private static final int IDX_XID = 4;
	private static final int IDX_LIST_BOOLEAN = 5;
	private static final int IDX_BOOLEAN = 6;
	private static final int IDX_LIST_DOUBLE = 7;
	private static final int IDX_DOUBLE = 8;
	private static final int IDX_LIST_LONG = 9;
	private static final int IDX_LONG = 10;
	private static final int IDX_LIST_INTEGER = 11;
	private static final int IDX_INTEGER = 12;
	
	private final XModelSynchronizer manager;
	private XLoggedField field;
	private final Label revision = new Label();
	private final ListBox type = new ListBox();
	private final HorizontalPanel inner = new HorizontalPanel();
	private final Button add = new Button("Add Entry");
	private final Button delete = new Button("Remove Field");
	private XValueEditor editor;
	
	private XValue currentValue;
	private XValue newValue;
	private boolean valueChanged;
	
	public XFieldEditor(XID fieldId, XModelSynchronizer manager) {
		
		this.manager = manager;
		
		add(this.inner);
		
		this.inner.add(new Label(fieldId.toString() + " ["));
		this.inner.add(this.revision);
		this.inner.add(new Label("] "));
		this.inner.add(this.type);
		this.inner.add(this.add);
		
		this.type.addItem("(No Value)");
		this.type.addItem("string list");
		this.type.addItem("string");
		this.type.addItem("xid list");
		this.type.addItem("xid");
		this.type.addItem("boolean list");
		this.type.addItem("boolean");
		this.type.addItem("double list");
		this.type.addItem("double");
		this.type.addItem("long list");
		this.type.addItem("long");
		this.type.addItem("integer list");
		this.type.addItem("integer");
		
		this.inner.add(this.delete);
		
		this.type.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent e) {
				typeChanged();
			}
		});
		
		this.add.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) {
				add();
			}
		});
		
		this.delete.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) {
				delete();
			}
		});
		
		setStyleName("editor-xfield");
	}
	
	protected void delete() {
		this.manager.executeCommand(MemoryObjectCommand.createRemoveCommand(this.field.getAddress()
		        .getParent(), this.field.getRevisionNumber(), this.field.getID()), null);
	}
	
	protected void add() {
		if(this.editor != null && this.editor instanceof XListEditor)
			((XListEditor)this.editor).add();
		else
			this.add.setVisible(false);
	}
	
	protected void typeChanged() {
		
		XValue value = this.field.getValue();
		
		if(this.editor != null) {
			this.editor.removeFromParent();
			this.editor = null;
		}
		
		XValue newValue;
		switch(this.type.getSelectedIndex()) {
		case IDX_NOVALUE:
			newValue = null;
			break;
		case IDX_LIST_STRING:
			newValue = XValueUtils.asStringListValue(value);
			break;
		case IDX_LIST_XID:
			newValue = XValueUtils.asXIDListValue(value);
			break;
		case IDX_LIST_BOOLEAN:
			newValue = XValueUtils.asBooleanListValue(value);
			break;
		case IDX_LIST_DOUBLE:
			newValue = XValueUtils.asDoubleListValue(value);
			break;
		case IDX_LIST_LONG:
			newValue = XValueUtils.asLongListValue(value);
			break;
		case IDX_LIST_INTEGER:
			newValue = XValueUtils.asIntegerListValue(value);
			break;
		
		case IDX_STRING:
			newValue = XValueUtils.asStringValue(value);
			break;
		case IDX_XID:
			newValue = XValueUtils.asXIDValue(value);
			break;
		case IDX_BOOLEAN:
			newValue = XValueUtils.asBooleanValue(value);
			break;
		case IDX_DOUBLE:
			newValue = XValueUtils.asDoubleValue(value);
			break;
		case IDX_LONG:
			newValue = XValueUtils.asLongValue(value);
			break;
		case IDX_INTEGER:
			newValue = XValueUtils.asIntegerValue(value);
			break;
		default:
			throw new RuntimeException("Unxecpected index from the 'value type' ListBox.");
		}
		
		if(!XX.equals(value, newValue)) {
			newValue(value);
		}
		
	}
	
	public void newValue(XValue value) {
		XFieldCommand command;
		if(this.field.isEmpty()) {
			command = MemoryFieldCommand.createAddCommand(this.field.getAddress(), this.field
			        .getRevisionNumber(), value);
		} else {
			command = MemoryFieldCommand.createChangeCommand(this.field.getAddress(), this.field
			        .getRevisionNumber(), value);
		}
		this.manager.executeCommand(command, null);
	}
	
	protected void changeValue() {
		
		if(!this.valueChanged) {
			return;
		}
		
		XValue value = this.newValue;
		
		if(value == null) {
			this.type.setSelectedIndex(IDX_NOVALUE);
			this.add.setVisible(false);
		} else {
			this.editor = XValueEditor.get(value, this);
			if(value instanceof XListValue<?>) {
				if(value instanceof XStringListValue)
					this.type.setSelectedIndex(IDX_LIST_STRING);
				else if(value instanceof XIDListValue)
					this.type.setSelectedIndex(IDX_LIST_XID);
				else if(value instanceof XBooleanListValue)
					this.type.setSelectedIndex(IDX_LIST_BOOLEAN);
				else if(value instanceof XDoubleListValue)
					this.type.setSelectedIndex(IDX_LIST_DOUBLE);
				else if(value instanceof XLongListValue)
					this.type.setSelectedIndex(IDX_LIST_LONG);
				else if(value instanceof XIntegerListValue)
					this.type.setSelectedIndex(IDX_LIST_INTEGER);
				else
					throw new RuntimeException("Unexpected XListValue type: " + value);
				this.add.setVisible(true);
				add(this.editor);
			} else {
				if(value instanceof XStringValue)
					this.type.setSelectedIndex(IDX_STRING);
				else if(value instanceof XIDValue)
					this.type.setSelectedIndex(IDX_XID);
				else if(value instanceof XBooleanValue)
					this.type.setSelectedIndex(IDX_BOOLEAN);
				else if(value instanceof XDoubleValue)
					this.type.setSelectedIndex(IDX_DOUBLE);
				else if(value instanceof XLongValue)
					this.type.setSelectedIndex(IDX_LONG);
				else if(value instanceof XIntegerValue)
					this.type.setSelectedIndex(IDX_INTEGER);
				else
					throw new RuntimeException("Unexpected non-list XValue type: " + value);
				this.add.setVisible(false);
				this.inner.insert(this.editor, this.inner.getWidgetCount() - 1);
			}
			
		}
		
		this.newValue = null;
		this.valueChanged = false;
	}
	
	public void setField(XLoggedField field) {
		this.field = field;
		boolean oldChanged = this.valueChanged;
		this.newValue = field.getValue();
		this.valueChanged = true;
		if(!oldChanged) {
			DeferredCommand.addCommand(new Command() {
				public void execute() {
					changeValue();
				}
			});
		}
		this.revision.setText(Long.toString(field.getRevisionNumber()));
	}
	
	public void onChangeEvent(XFieldEvent event) {
		XValue value = event.getNewValue();
		if(XX.equals(this.currentValue, value)) {
			this.newValue = null;
			this.valueChanged = false;
		} else {
			boolean oldChanged = this.valueChanged;
			this.newValue = value;
			this.valueChanged = true;
			if(!oldChanged) {
				DeferredCommand.addCommand(new Command() {
					public void execute() {
						changeValue();
					}
				});
			}
		}
		this.revision.setText(Long.toString(event.getFieldRevisionNumber()));
	}
	
}
