package org.xydra.webadmin.gwt.client.widgets.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.xydra.base.Base;
import org.xydra.base.XAddress;
import org.xydra.base.XId;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XAddressListValue;
import org.xydra.base.value.XAddressSetValue;
import org.xydra.base.value.XAddressSortedSetValue;
import org.xydra.base.value.XBinaryValue;
import org.xydra.base.value.XBooleanListValue;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XDoubleListValue;
import org.xydra.base.value.XDoubleValue;
import org.xydra.base.value.XIdListValue;
import org.xydra.base.value.XIdSetValue;
import org.xydra.base.value.XIdSortedSetValue;
import org.xydra.base.value.XIntegerListValue;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XLongListValue;
import org.xydra.base.value.XLongValue;
import org.xydra.base.value.XStringListValue;
import org.xydra.base.value.XStringSetValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XV;
import org.xydra.base.value.XValue;
import org.xydra.core.XX;
import org.xydra.gwt.editor.value.XAddressEditor;
import org.xydra.gwt.editor.value.XAddressListEditor;
import org.xydra.gwt.editor.value.XAddressSetEditor;
import org.xydra.gwt.editor.value.XAddressSortedSetEditor;
import org.xydra.gwt.editor.value.XBinaryValueEditor;
import org.xydra.gwt.editor.value.XBooleanEditor;
import org.xydra.gwt.editor.value.XBooleanListEditor;
import org.xydra.gwt.editor.value.XDoubleEditor;
import org.xydra.gwt.editor.value.XDoubleListEditor;
import org.xydra.gwt.editor.value.XIdEditor;
import org.xydra.gwt.editor.value.XIdListEditor;
import org.xydra.gwt.editor.value.XIdSetEditor;
import org.xydra.gwt.editor.value.XIdSortedSetEditor;
import org.xydra.gwt.editor.value.XIntegerEditor;
import org.xydra.gwt.editor.value.XIntegerListEditor;
import org.xydra.gwt.editor.value.XLongEditor;
import org.xydra.gwt.editor.value.XLongListEditor;
import org.xydra.gwt.editor.value.XStringEditor;
import org.xydra.gwt.editor.value.XStringListEditor;
import org.xydra.gwt.editor.value.XStringSetEditor;
import org.xydra.gwt.editor.value.XValueEditor;
import org.xydra.gwt.editor.value.XValueEditor.EditListener;
import org.xydra.log.api.Logger;
import org.xydra.log.api.LoggerFactory;
import org.xydra.webadmin.gwt.client.widgets.XyAdmin;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class AddEditorDialog extends DialogBox {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(XyAdmin.class);

	interface ViewUiBinder extends UiBinder<Widget, AddEditorDialog> {
	}

	private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

	String inputString = "";

	@UiField
	VerticalPanel mainPanel;
	@UiField
	HorizontalPanel selectEditorPanel;
	@UiField
	HorizontalPanel editorPanel;
	@UiField
	ListBox listBox;

	@UiField(provided = true)
	TextAreaWidget textArea;

	@UiField(provided = true)
	ButtonPanel buttonPanel;

	XValueEditor valueEditor;

	private final XAddress address;

	public AddEditorDialog(final XAddress address) {

		this.address = address;

		final XId fieldName = address.getField();

		String textFieldInput = null;
		if (fieldName != null) {
			textFieldInput = fieldName.toString();
		}
		final ClickHandler okHandler = new ClickHandler() {

			@SuppressWarnings("unused")
			@Override
			public void onClick(final ClickEvent event) {

				XAddress eventualAddress = null;

				final XId fieldName = address.getField();
				if (fieldName != null) {
					eventualAddress = address;
				} else {
					final String desiredFieldName = AddEditorDialog.this.textArea.getText();
					final XId newFieldID = Base.toId(desiredFieldName);
					eventualAddress = Base.resolveField(address, newFieldID);
				}
				final XValue value = AddEditorDialog.this.valueEditor.getValue();
				// DataModel.getInstance().addField(eventualAddress, value);
				AddEditorDialog.this.removeFromParent();
			}
		};

		this.buttonPanel = new ButtonPanel(okHandler, this);
		this.textArea = new TextAreaWidget(textFieldInput, this.buttonPanel);

		setWidget(uiBinder.createAndBindUi(this));
		for (final ValueType valueType : ValueType.values()) {
			this.listBox.addItem(valueType.toString());
		}
		this.valueEditor = getDefaultValueEditorForType(ValueType.Address, this.address);
		this.editorPanel.add(this.valueEditor);

		this.setStyleName("dialogStyle");
		setText("add Element");

		center();
		// this.textArea.selectEverything();
	}

	public void selectEverything() {
		this.textArea.selectEverything();
	}

	@UiHandler("listBox")
	void onChange(final ChangeEvent event) {
		final String selectedItem = this.listBox.getItemText(this.listBox.getSelectedIndex());
		final ValueType valueType = ValueType.valueOf(selectedItem);

		this.editorPanel.clear();
		this.valueEditor = getDefaultValueEditorForType(valueType, this.address);
		this.editorPanel.add(this.valueEditor);
	}

	private XValueEditor getDefaultValueEditorForType(final ValueType valueType, final XAddress address) {

		XValueEditor editor = null;
		final EditedValueListener editListener = new EditedValueListener();

		final XAddress addressValue = Base.toAddress("/" + address.getRepository() + "/-/-/-");
		final HashSet<XAddress> addressSet = new HashSet<XAddress>();
		addressSet.add(addressValue);

		final XBooleanValue booleanValue = XV.toValue(false);
		final HashSet<Boolean> booleanSet = new HashSet<Boolean>();
		booleanSet.add(false);

		final XDoubleValue doubleValue = XV.toValue(0.0);

		final XId idValue = Base.toId("id");
		final ArrayList<XId> idList = new ArrayList<XId>();
		idList.add(idValue);

		final ArrayList<String> stringList = new ArrayList<String>();
		stringList.add("string");

		switch (valueType) {
		case Address:

			editor = new XAddressEditor(addressValue, editListener);
			break;
		case AddressList:

			final XAddressListValue addressListValue = XV.toAddressListValue(addressSet);

			editor = new XAddressListEditor(addressListValue.iterator(), editListener);
			break;
		case AddressSet:

			final XAddressSetValue addressSetValue = XV.toAddressSetValue(addressSet);

			editor = new XAddressSetEditor(addressSetValue.iterator(), editListener);
			break;
		case AddressSortedSet:

			final XAddressSortedSetValue addressSortedSetValue = XV.toAddressSortedSetValue(addressSet);

			editor = new XAddressSortedSetEditor(addressSortedSetValue.iterator(), editListener);

			break;
		case Binary:
			final Collection<Byte> bytes = new ArrayList<Byte>();
			bytes.add(new Byte("1"));
			final XBinaryValue binaryValue = XV.toBinaryValue(bytes);

			editor = new XBinaryValueEditor(binaryValue.getValue(), editListener);
			break;
		case Boolean:

			editor = new XBooleanEditor(booleanValue.contents(), editListener);
			break;
		case BooleanList:
			final XBooleanListValue booleanListValue = XV.toBooleanListValue(booleanSet);

			editor = new XBooleanListEditor(booleanListValue.iterator(), editListener);
			break;
		case Double:

			editor = new XDoubleEditor(doubleValue.contents(), editListener);
			break;
		case DoubleList:

			final ArrayList<Double> doubleList = new ArrayList<Double>();
			doubleList.add(0.0);

			final XDoubleListValue doubleListValue = XV.toDoubleListValue(doubleList);

			editor = new XDoubleListEditor(doubleListValue.iterator(), editListener);
			break;
		case Id:

			editor = new XIdEditor(idValue, editListener);
			break;
		case IdList:
			final XIdListValue idListValue = XV.toIdListValue(idList);

			editor = new XIdListEditor(idListValue.iterator(), editListener);
			break;
		case IdSet:
			final XIdSetValue idSetValue = XV.toIdSetValue(idList);

			editor = new XIdSetEditor(idSetValue.iterator(), editListener);
			break;
		case IdSortedSet:
			final XIdSortedSetValue idSortedSetValue = XV.toIdSortedSetValue(idList);

			editor = new XIdSortedSetEditor(idSortedSetValue.iterator(), editListener);
			break;
		case Integer:
			final XIntegerValue integerValue = XV.toValue(0);
			editor = new XIntegerEditor(integerValue.contents(), editListener);
			break;
		case IntegerList:

			final ArrayList<Integer> integerList = new ArrayList<Integer>();
			integerList.add(0);
			final XIntegerListValue integerListValue = XV.toIntegerListValue(integerList);

			editor = new XIntegerListEditor(integerListValue.iterator(), editListener);
			break;
		case Long:
			final XLongValue longValue = XV.toValue(0l);

			editor = new XLongEditor(longValue.contents(), editListener);
			break;
		case LongList:
			final ArrayList<Long> longList = new ArrayList<Long>();
			longList.add(0l);
			final XLongListValue longListValue = XV.toLongListValue(longList);

			editor = new XLongListEditor(longListValue.iterator(), editListener);
			break;
		case String:

			final XStringValue stringValue = XV.toValue("string");

			editor = new XStringEditor(stringValue.contents(), editListener);
			break;
		case StringList:

			final XStringListValue stringListValue = XV.toStringListValue(stringList);

			editor = new XStringListEditor(stringListValue.iterator(), editListener);
			break;
		case StringSet:
			final XStringSetValue stringSetValue = XV.toStringSetValue(stringList);

			editor = new XStringSetEditor(stringSetValue.iterator(), editListener);
			break;
		default:
			break;
		}

		return editor;
	}

	class EditedValueListener implements EditListener {

		@Override
		public void newValue(final XValue value) {
		}

	}

	public void setFieldEditable(final boolean b) {
		this.textArea.setEnabled(b);

	}

}
