package org.xydra.client.gwt.editor.value;

import org.xydra.core.X;
import org.xydra.core.value.XStringValue;

import com.google.gwt.user.client.ui.TextBox;


public class XStringEditor extends AtomicXValueEditor<XStringValue> {
	
	private final TextBox editor = new TextBox();
	
	public XStringEditor(String oldValue, EditListener listener) {
		super(listener);
		
		if(oldValue != null)
			this.editor.setText(oldValue);
		
		initWidget(this.editor);
	}
	
	@Override
	public XStringValue getValue() {
		return X.getValueFactory().createStringValue(this.editor.getText());
	}
	
}
