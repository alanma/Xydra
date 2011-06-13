package org.xydra.gwt.editor.value;

import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XV;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;


public class XBooleanEditor extends AtomicXValueEditor<XBooleanValue> {
	
	private final CheckBox editor = new CheckBox();
	
	public XBooleanEditor(boolean oldValue, EditListener listener) {
		super(listener);
		
		this.editor.setValue(oldValue);
		
		initWidget(this.editor);
		
		// CheckBox doesn't seem to send change events
		if(listener != null) {
			this.editor.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent e) {
					onChange(null);
				}
			});
		}
	}
	
	@Override
	public XBooleanValue getValue() {
		return XV.toValue(this.editor.getValue());
	}
	
}
