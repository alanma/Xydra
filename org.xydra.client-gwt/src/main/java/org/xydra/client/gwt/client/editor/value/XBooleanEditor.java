package org.xydra.client.gwt.client.editor.value;

import org.xydra.core.X;
import org.xydra.core.value.XValue;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;



public class XBooleanEditor extends AtomicXValueEditor {
	
	private final CheckBox editor = new CheckBox();
	
	public XBooleanEditor(boolean oldValue, EditListener listener) {
		super(listener);
		
		this.editor.setValue(oldValue);
		
		initWidget(this.editor);
		
		// CheckBox doesn't seem to send change events
		this.editor.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) {
				onChange(null);
			}
		});
	}
	
	@Override
	public XValue getValue() {
		return X.getValueFactory().createBooleanValue(this.editor.getValue());
	}
	
}
