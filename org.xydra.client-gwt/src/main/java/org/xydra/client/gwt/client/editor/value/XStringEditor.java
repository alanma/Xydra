package org.xydra.client.gwt.client.editor.value;

import org.xydra.core.X;
import org.xydra.core.value.XValue;

import com.google.gwt.user.client.ui.TextBox;



public class XStringEditor extends AtomicXValueEditor {
	
	private final TextBox editor = new TextBox();
	
	public XStringEditor(String oldValue, EditListener listener) {
		super(listener);
		
		if(oldValue != null)
			this.editor.setText(oldValue);
		
		initWidget(this.editor);
	}
	
	@Override
	public XValue getValue() {
		return X.getValueFactory().createStringValue(this.editor.getText());
	}
	
}
