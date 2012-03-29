package org.xydra.editor.value;

import org.xydra.base.value.XBinaryValue;
import org.xydra.base.value.XV;

import com.google.gwt.user.client.ui.Label;


public class XBinaryValueEditor extends XValueEditor {
	
	byte[] value;
	
	public XBinaryValueEditor(byte[] value, EditListener listener) {
		super();
		this.value = value;
		
		initWidget(new Label("(cannot edit byte values)"));
		
		setStyleName("editor-xvalue");
	}
	
	@Override
	public XBinaryValue getValue() {
		return XV.toValue(this.value);
	}
	
}
