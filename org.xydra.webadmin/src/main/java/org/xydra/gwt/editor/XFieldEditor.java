package org.xydra.gwt.editor;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XFieldEvent;
import org.xydra.base.rmof.XReadableField;
import org.xydra.base.value.XAddressListValue;
import org.xydra.base.value.XAddressSetValue;
import org.xydra.base.value.XAddressSortedSetValue;
import org.xydra.base.value.XBinaryValue;
import org.xydra.base.value.XBooleanListValue;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XCollectionValue;
import org.xydra.base.value.XDoubleListValue;
import org.xydra.base.value.XDoubleValue;
import org.xydra.base.value.XIDListValue;
import org.xydra.base.value.XIDSetValue;
import org.xydra.base.value.XIDSortedSetValue;
import org.xydra.base.value.XIntegerListValue;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XListValue;
import org.xydra.base.value.XLongListValue;
import org.xydra.base.value.XLongValue;
import org.xydra.base.value.XSetValue;
import org.xydra.base.value.XStringListValue;
import org.xydra.base.value.XStringSetValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XValue;
import org.xydra.core.change.XFieldEventListener;
import org.xydra.gwt.editor.value.XAddressEditor;
import org.xydra.gwt.editor.value.XAddressListEditor;
import org.xydra.gwt.editor.value.XAddressSetEditor;
import org.xydra.gwt.editor.value.XAddressSortedSetEditor;
import org.xydra.gwt.editor.value.XBinaryValueEditor;
import org.xydra.gwt.editor.value.XBooleanEditor;
import org.xydra.gwt.editor.value.XBooleanListEditor;
import org.xydra.gwt.editor.value.XCollectionEditor;
import org.xydra.gwt.editor.value.XDoubleEditor;
import org.xydra.gwt.editor.value.XDoubleListEditor;
import org.xydra.gwt.editor.value.XIDEditor;
import org.xydra.gwt.editor.value.XIDListEditor;
import org.xydra.gwt.editor.value.XIDSetEditor;
import org.xydra.gwt.editor.value.XIDSortedSetEditor;
import org.xydra.gwt.editor.value.XIntegerEditor;
import org.xydra.gwt.editor.value.XIntegerListEditor;
import org.xydra.gwt.editor.value.XLongEditor;
import org.xydra.gwt.editor.value.XLongListEditor;
import org.xydra.gwt.editor.value.XStringEditor;
import org.xydra.gwt.editor.value.XStringListEditor;
import org.xydra.gwt.editor.value.XStringSetEditor;
import org.xydra.gwt.editor.value.XValueEditor;
import org.xydra.gwt.editor.value.XValueUtils;
import org.xydra.index.XI;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;


public class XFieldEditor extends VerticalPanel implements XFieldEventListener, AddEditor {
	
	private static final Logger log = LoggerFactory.getLogger(XFieldEditor.class);
	
	private static final int IDX_NOVALUE = 0;
	private static final int IDX_LIST_STRING = 1;
	private static final int IDX_STRING = 2;
	private static final int IDX_LIST_ID = 3;
	private static final int IDX_ID = 4;
	private static final int IDX_LIST_BOOLEAN = 5;
	private static final int IDX_BOOLEAN = 6;
	private static final int IDX_LIST_DOUBLE = 7;
	private static final int IDX_DOUBLE = 8;
	private static final int IDX_LIST_LONG = 9;
	private static final int IDX_LONG = 10;
	private static final int IDX_LIST_INTEGER = 11;
	private static final int IDX_INTEGER = 12;
	private static final int IDX_SET_STRING = 13;
	private static final int IDX_SET_ID = 14;
	private static final int IDX_LIST_BYTE = 15;
	private static final int IDX_ADDRESS = 16;
	private static final int IDX_LIST_ADDRESS = 17;
	private static final int IDX_SET_ADDRESS = 18;
	private static final int IDX_SET_ADDRESS_SORTED = 19;
	private static final int IDX_SET_ID_SORTED = 20;
	
	private final XReadableField field;
	private final Label revision = new Label();
	private final HorizontalPanel inner = new HorizontalPanel();
	private final Button delete = new Button("Remove Field");
	private final Label contents = new Label();
	private final Button edit = new Button("Edit");
	private final int innerIndex;
	
	private ListBox type;
	private Button add;
	private Button save;
	private Button cancel;
	private XValueEditor editor;
	
	public XFieldEditor(XReadableField field2) {
		
		this.field = field2;
		
		add(this.inner);
		
		this.innerIndex = this.inner.getWidgetCount();
		this.inner.add(this.contents);
		this.inner.add(this.edit);
		this.inner.add(this.delete);
		
		this.edit.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				showEditor();
			}
		});
		
		this.delete.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				delete();
			}
		});
		
		setStyleName("editor-xfield");
		
		changeValue(field2.getValue());
	}
	
	protected void delete() {
		Controller.getInstance().getDataModel().removeItem(this.field.getAddress());
	}
	
	protected void changeValue(XValue value) {
		
		String str;
		if(value == null) {
			str = "(no value)";
		} else {
			str = value.toString();
			if(value instanceof XCollectionValue<?>) {
				if(value instanceof XListValue<?>) {
					if(value instanceof XStringListValue) {
						str += " (string list)";
					} else if(value instanceof XIDListValue) {
						str += " (id list)";
					} else if(value instanceof XBooleanListValue) {
						str += " (boolean list)";
					} else if(value instanceof XDoubleListValue) {
						str += " (double list)";
					} else if(value instanceof XLongListValue) {
						str += " (long list)";
					} else if(value instanceof XIntegerListValue) {
						str += " (integer list)";
					} else if(value instanceof XAddressListValue) {
						str += " (address list)";
					} else {
						throw new RuntimeException("Unexpected XListValue type: " + value);
					}
				} else if(value instanceof XSetValue<?>) {
					if(value instanceof XStringSetValue) {
						str += " (string set)";
					} else if(value instanceof XIDSortedSetValue) {
						str += " (sorted id set)";
					} else if(value instanceof XIDSetValue) {
						str += " (id set)";
					} else if(value instanceof XAddressSortedSetValue) {
						str += " (sorted address set)";
					} else if(value instanceof XAddressSetValue) {
						str += " (address set)";
					} else {
						throw new RuntimeException("Unexpected XSetValue type: " + value.getClass()
						        + ": " + value.getType());
					}
				} else {
					throw new RuntimeException("Unexpected XCollectionValue type: " + value);
				}
			} else {
				if(value instanceof XStringValue) {
					str += " (string)";
				} else if(value instanceof XID) {
					str += " (id)";
				} else if(value instanceof XBooleanValue) {
					str += " (boolean)";
				} else if(value instanceof XBinaryValue) {
					str += " (binary)";
				} else if(value instanceof XDoubleValue) {
					str += " (double)";
				} else if(value instanceof XLongValue) {
					str += " (long)";
				} else if(value instanceof XIntegerValue) {
					str += " (integer)";
				} else if(value instanceof XAddress) {
					str += " (address)";
				} else {
					throw new RuntimeException("Unexpected non-list XValue type: " + value);
				}
			}
			
		}
		
		this.contents.setText(str);
		
	}
	
	@Override
	public void onChangeEvent(XFieldEvent event) {
		log.info("editor: got " + event);
		XValue value = event.getNewValue();
		changeValue(value);
		this.revision.setText(Long.toString(event.getOldFieldRevision()));
	}
	
	public void removeEditor() {
		
		if(this.save == null) {
			return;
		}
		
		if(this.editor != null) {
			this.editor.removeFromParent();
		}
		if(this.add != null) {
			this.add.removeFromParent();
		}
		this.save.removeFromParent();
		this.cancel.removeFromParent();
		this.type.removeFromParent();
		this.edit.setVisible(true);
		this.contents.setVisible(true);
		this.editor = null;
		this.add = null;
		this.save = null;
		this.cancel = null;
		this.type = null;
		
	}
	
	public void showEditor() {
		
		if(this.save != null) {
			return;
		}
		
		this.edit.setVisible(false);
		this.contents.setVisible(false);
		
		this.cancel = new Button("Cancel");
		
		this.inner.insert(this.cancel, this.innerIndex);
		this.cancel.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				removeEditor();
			}
		});
		
		this.save = new Button("Save");
		this.inner.insert(this.save, this.innerIndex);
		this.save.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				saveValue();
			}
		});
		
		this.add = new Button("Add Entry");
		this.inner.insert(this.add, this.innerIndex);
		this.add.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				((XCollectionEditor<?,?>)XFieldEditor.this.editor).add();
			}
		});
		
		this.type = new ListBox();
		this.type.addItem("(No Value)");
		this.type.addItem("string list");
		this.type.addItem("string");
		this.type.addItem("id list");
		this.type.addItem("id");
		this.type.addItem("boolean list");
		this.type.addItem("boolean");
		this.type.addItem("double list");
		this.type.addItem("double");
		this.type.addItem("long list");
		this.type.addItem("long");
		this.type.addItem("integer list");
		this.type.addItem("integer");
		this.type.addItem("string set");
		this.type.addItem("xid set");
		this.type.addItem("byte list");
		this.type.addItem("address");
		this.type.addItem("address list");
		this.type.addItem("address set");
		this.type.addItem("sorted address set");
		this.type.addItem("sorted id set");
		
		this.inner.insert(this.type, this.innerIndex);
		this.type.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent e) {
				typeChanged();
			}
		});
		
		XValue value = this.field.getValue();
		if(value == null) {
			this.type.setSelectedIndex(IDX_NOVALUE);
			this.add.setVisible(false);
		} else {
			this.editor = XValueEditor.get(value, null);
			if(value instanceof XCollectionValue<?>) {
				if(value instanceof XListValue<?>) {
					if(value instanceof XStringListValue) {
						this.type.setSelectedIndex(IDX_LIST_STRING);
					} else if(value instanceof XIDListValue) {
						this.type.setSelectedIndex(IDX_LIST_ID);
					} else if(value instanceof XBooleanListValue) {
						this.type.setSelectedIndex(IDX_LIST_BOOLEAN);
					} else if(value instanceof XDoubleListValue) {
						this.type.setSelectedIndex(IDX_LIST_DOUBLE);
					} else if(value instanceof XLongListValue) {
						this.type.setSelectedIndex(IDX_LIST_LONG);
					} else if(value instanceof XIntegerListValue) {
						this.type.setSelectedIndex(IDX_LIST_INTEGER);
					} else if(value instanceof XAddressListValue) {
						this.type.setSelectedIndex(IDX_LIST_ADDRESS);
					} else {
						throw new RuntimeException("Unexpected XListValue type: " + value);
					}
				} else if(value instanceof XSetValue<?>) {
					if(value instanceof XStringSetValue) {
						this.type.setSelectedIndex(IDX_SET_STRING);
					} else if(value instanceof XIDSortedSetValue) {
						this.type.setSelectedIndex(IDX_SET_ID_SORTED);
					} else if(value instanceof XIDSetValue) {
						this.type.setSelectedIndex(IDX_SET_ID);
					} else if(value instanceof XAddressSortedSetValue) {
						this.type.setSelectedIndex(IDX_SET_ADDRESS_SORTED);
					} else if(value instanceof XAddressSetValue) {
						this.type.setSelectedIndex(IDX_SET_ADDRESS);
					} else {
						throw new RuntimeException("Unexpected XSetValue type: " + value);
					}
				} else {
					throw new RuntimeException("Unexpected XCollection Value type: " + value);
				}
			} else {
				if(value instanceof XStringValue) {
					this.type.setSelectedIndex(IDX_STRING);
				} else if(value instanceof XID) {
					this.type.setSelectedIndex(IDX_ID);
				} else if(value instanceof XBooleanValue) {
					this.type.setSelectedIndex(IDX_BOOLEAN);
				} else if(value instanceof XBinaryValue) {
					this.type.setSelectedIndex(IDX_LIST_BYTE);
				} else if(value instanceof XDoubleValue) {
					this.type.setSelectedIndex(IDX_DOUBLE);
				} else if(value instanceof XLongValue) {
					this.type.setSelectedIndex(IDX_LONG);
				} else if(value instanceof XIntegerValue) {
					this.type.setSelectedIndex(IDX_INTEGER);
				} else if(value instanceof XAddress) {
					this.type.setSelectedIndex(IDX_ADDRESS);
				} else {
					throw new RuntimeException("Unexpected non-collection XValue type: " + value);
				}
			}
			
		}
		
		attachEditor();
		
	}
	
	protected void saveValue() {
		XValue newValue = this.editor == null ? null : this.editor.getValue();
		
		log.info("editor: saving changed value: " + newValue);
		
		if(this.editor != null && newValue == null) {
			// invalid editor contents
			return;
		}
		
		removeEditor();
		
		if(XI.equals(newValue, this.field.getValue())) {
			// nothing changed
			return;
		}
		
		if(this.field.isEmpty()) {
			Controller.getInstance().getDataModel().changeValue(this.field.getAddress(), newValue);
		} else {
			if(newValue == null) {
				Controller.getInstance().getDataModel().removeItem(this.field.getAddress());
			} else {
				Controller.getInstance().getDataModel()
				        .changeValue(this.field.getAddress(), newValue);
			}
		}
	}
	
	protected void typeChanged() {
		
		log.info("editor: type changed to " + this.type.getSelectedIndex());
		
		XValue value = null;
		if(this.editor != null) {
			value = this.editor.getValue();
			this.editor.removeFromParent();
		}
		
		switch(this.type.getSelectedIndex()) {
		case IDX_NOVALUE:
			this.editor = null;
			break;
		case IDX_LIST_STRING:
			this.editor = new XStringListEditor(XValueUtils.asStringList(value), null);
			break;
		case IDX_LIST_ID:
			this.editor = new XIDListEditor(XValueUtils.asXIDList(value), null);
			break;
		case IDX_LIST_ADDRESS:
			this.editor = new XAddressListEditor(XValueUtils.asAddressList(value), null);
			break;
		case IDX_LIST_BOOLEAN:
			this.editor = new XBooleanListEditor(XValueUtils.asBooleanList(value), null);
			break;
		case IDX_LIST_DOUBLE:
			this.editor = new XDoubleListEditor(XValueUtils.asDoubleList(value), null);
			break;
		case IDX_LIST_LONG:
			this.editor = new XLongListEditor(XValueUtils.asLongList(value), null);
			break;
		case IDX_LIST_INTEGER:
			this.editor = new XIntegerListEditor(XValueUtils.asIntegerList(value), null);
			break;
		case IDX_LIST_BYTE:
			this.editor = new XBinaryValueEditor(XValueUtils.asByteList(value), null);
			break;
		
		case IDX_STRING:
			this.editor = new XStringEditor(XValueUtils.asString(value), null);
			break;
		case IDX_ID:
			this.editor = new XIDEditor(XValueUtils.asXID(value), null);
			break;
		case IDX_ADDRESS:
			this.editor = new XAddressEditor(XValueUtils.asAddress(value), null);
			break;
		case IDX_BOOLEAN:
			this.editor = new XBooleanEditor(XValueUtils.asBoolean(value), null);
			break;
		case IDX_DOUBLE:
			this.editor = new XDoubleEditor(XValueUtils.asDouble(value), null);
			break;
		case IDX_LONG:
			this.editor = new XLongEditor(XValueUtils.asLong(value), null);
			break;
		case IDX_INTEGER:
			this.editor = new XIntegerEditor(XValueUtils.asInteger(value), null);
			break;
		
		case IDX_SET_STRING:
			this.editor = new XStringSetEditor(XValueUtils.asStringList(value), null);
			break;
		case IDX_SET_ID:
			this.editor = new XIDSetEditor(XValueUtils.asXIDList(value), null);
			break;
		case IDX_SET_ID_SORTED:
			this.editor = new XIDSortedSetEditor(XValueUtils.asXIDList(value), null);
			break;
		case IDX_SET_ADDRESS:
			this.editor = new XAddressSetEditor(XValueUtils.asAddressList(value), null);
			break;
		case IDX_SET_ADDRESS_SORTED:
			this.editor = new XAddressSortedSetEditor(XValueUtils.asAddressList(value), null);
			break;
		
		default:
			throw new RuntimeException("Unxecpected index from the 'value type' ListBox.");
		}
		
		attachEditor();
		
	}
	
	private void attachEditor() {
		if(this.editor != null) {
			if(this.editor instanceof XCollectionEditor<?,?>) {
				add(this.editor);
				this.add.setVisible(true);
			} else {
				this.inner.insert(this.editor, this.innerIndex + 1);
				this.add.setVisible(false);
			}
		}
	}
	
}
