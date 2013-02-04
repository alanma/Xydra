package org.xydra.webadmin.gwt.client.widgets.version2;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XAddressListValue;
import org.xydra.base.value.XAddressSetValue;
import org.xydra.base.value.XAddressSortedSetValue;
import org.xydra.base.value.XBinaryValue;
import org.xydra.base.value.XBooleanListValue;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XDoubleListValue;
import org.xydra.base.value.XDoubleValue;
import org.xydra.base.value.XIDListValue;
import org.xydra.base.value.XIDSetValue;
import org.xydra.base.value.XIDSortedSetValue;
import org.xydra.base.value.XIntegerListValue;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XLongListValue;
import org.xydra.base.value.XLongValue;
import org.xydra.base.value.XStringListValue;
import org.xydra.base.value.XStringSetValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XValue;
import org.xydra.gwt.editor.value.XAddressEditor;
import org.xydra.gwt.editor.value.XAddressListEditor;
import org.xydra.gwt.editor.value.XAddressSetEditor;
import org.xydra.gwt.editor.value.XAddressSortedSetEditor;
import org.xydra.gwt.editor.value.XBinaryValueEditor;
import org.xydra.gwt.editor.value.XBooleanEditor;
import org.xydra.gwt.editor.value.XBooleanListEditor;
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
import org.xydra.gwt.editor.value.XValueEditor.EditListener;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;
import org.xydra.webadmin.gwt.client.Controller;
import org.xydra.webadmin.gwt.client.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class FieldWidget extends Composite {
	
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);
	
	interface ViewUiBinder extends UiBinder<Widget,FieldWidget> {
	}
	
	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
	
	@UiField
	VerticalPanel mainPanel;
	
	public FieldWidget(final XAddress currentFieldAddress, XValue fieldValue, long revisionNumber) {
		
		super();
		initWidget(uiBinder.createAndBindUi(this));
		
		VerticalPanel infoPanel = new VerticalPanel();
		infoPanel.setStyleName("fieldValueInfo");
		
		ValueType valueType = fieldValue.getType();
		Label infoLabel = new Label(valueType.toString());
		
		XValueEditor editor = getEditorForType(valueType, fieldValue, currentFieldAddress);
		
		infoPanel.add(infoLabel);
		infoPanel.add(editor);
		
		Button deleteFieldButton = new Button("delete");
		deleteFieldButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				RemoveDialog removeDialog = new RemoveDialog(currentFieldAddress);
				removeDialog.show();
				
			}
		});
		infoPanel.add(deleteFieldButton);
		
		this.mainPanel.add(infoPanel);
		
		Label revisionNumberLabel = new Label("rev.: " + revisionNumber);
		revisionNumberLabel.setStyleName("revisionLabelStyle");
		this.mainPanel.add(revisionNumberLabel);
	}
	
	private XValueEditor getEditorForType(ValueType valueType, XValue fieldValue,
	        final XAddress address) {
		
		XValueEditor editor = null;
		EditedValueListener editListener = new EditedValueListener(address);
		
		switch(valueType) {
		case Address:
			
			XAddress addressValue = (XAddress)fieldValue;
			
			editor = new XAddressEditor(addressValue, editListener);
			break;
		case AddressList:
			
			XAddressListValue addressListValue = (XAddressListValue)fieldValue;
			
			editor = new XAddressListEditor(addressListValue.iterator(), editListener);
			break;
		case AddressSet:
			
			XAddressSetValue addressSetValue = (XAddressSetValue)fieldValue;
			
			editor = new XAddressSetEditor(addressSetValue.iterator(), editListener);
			break;
		case AddressSortedSet:
			
			XAddressSortedSetValue addressSortedSetValue = (XAddressSortedSetValue)fieldValue;
			
			editor = new XAddressSortedSetEditor(addressSortedSetValue.iterator(), editListener);
			
			break;
		case Binary:
			XBinaryValue binaryValue = (XBinaryValue)fieldValue;
			
			editor = new XBinaryValueEditor(binaryValue.contents(), editListener);
			break;
		case Boolean:
			XBooleanValue booleanValue = (XBooleanValue)fieldValue;
			
			editor = new XBooleanEditor(booleanValue.contents(), editListener);
			break;
		case BooleanList:
			XBooleanListValue booleanListValue = (XBooleanListValue)fieldValue;
			
			editor = new XBooleanListEditor(booleanListValue.iterator(), editListener);
			break;
		case Double:
			XDoubleValue doubleValue = (XDoubleValue)fieldValue;
			
			editor = new XDoubleEditor(doubleValue.contents(), editListener);
			break;
		case DoubleList:
			XDoubleListValue doubleListValue = (XDoubleListValue)fieldValue;
			
			editor = new XDoubleListEditor(doubleListValue.iterator(), editListener);
			break;
		case Id:
			XID idValue = (XID)fieldValue;
			
			editor = new XIDEditor(idValue, editListener);
			break;
		case IdList:
			XIDListValue idListValue = (XIDListValue)fieldValue;
			
			editor = new XIDListEditor(idListValue.iterator(), editListener);
			break;
		case IdSet:
			XIDSetValue idSetValue = (XIDSetValue)fieldValue;
			
			editor = new XIDSetEditor(idSetValue.iterator(), editListener);
			break;
		case IdSortedSet:
			XIDSortedSetValue idSortedSetValue = (XIDSortedSetValue)fieldValue;
			
			editor = new XIDSortedSetEditor(idSortedSetValue.iterator(), editListener);
			break;
		case Integer:
			XIntegerValue integerValue = (XIntegerValue)fieldValue;
			editor = new XIntegerEditor(integerValue.contents(), editListener);
			break;
		case IntegerList:
			
			XIntegerListValue integerListValue = (XIntegerListValue)fieldValue;
			
			editor = new XIntegerListEditor(integerListValue.iterator(), editListener);
			break;
		case Long:
			XLongValue longValue = (XLongValue)fieldValue;
			
			editor = new XLongEditor(longValue.contents(), editListener);
			break;
		case LongList:
			XLongListValue longListValue = (XLongListValue)fieldValue;
			
			editor = new XLongListEditor(longListValue.iterator(), editListener);
			break;
		case String:
			
			XStringValue stringValue = (XStringValue)fieldValue;
			
			editor = new XStringEditor(stringValue.contents(), editListener);
			break;
		case StringList:
			XStringListValue stringListValue = (XStringListValue)fieldValue;
			
			editor = new XStringListEditor(stringListValue.iterator(), editListener);
			break;
		case StringSet:
			XStringSetValue stringSetValue = (XStringSetValue)fieldValue;
			
			editor = new XStringSetEditor(stringSetValue.iterator(), editListener);
			break;
		default:
			break;
		}
		
		return editor;
	}
	
	class EditedValueListener implements EditListener {
		
		private XAddress address;
		
		EditedValueListener(XAddress address) {
			this.address = address;
		}
		
		@Override
		public void newValue(XValue value) {
			
			Controller.getInstance().getDataModel().changeValue(this.address, value);
		}
		
	}
	
}
