package org.xydra.client.gwt.editor.value;

import org.xydra.core.X;
import org.xydra.core.value.XByteListValue;

import com.google.gwt.user.client.ui.Label;


public class XByteListEditor extends XValueEditor {
	
	byte[] value;
	
	public XByteListEditor(byte[] value, EditListener listener) {
		super();
		this.value = value;
		
		initWidget(new Label("(cannot edit byte values)"));
		
		setStyleName("editor-xvalue");
	}
	
	@Override
	public XByteListValue getValue() {
		return X.getValueFactory().createByteListValue(this.value);
	}
	
}
