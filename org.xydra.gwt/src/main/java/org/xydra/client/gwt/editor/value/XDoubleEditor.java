package org.xydra.client.gwt.editor.value;

import org.xydra.base.value.XDoubleValue;
import org.xydra.base.value.XV;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.TextBox;


public class XDoubleEditor extends AtomicXValueEditor<XDoubleValue> implements KeyPressHandler {
	
	private final TextBox editor = new TextBox();
	
	public XDoubleEditor(double oldValue, EditListener listener) {
		super(listener);
		
		this.editor.setText(Double.toString(oldValue));
		
		this.editor.addKeyPressHandler(this);
		
		initWidget(this.editor);
	}
	
	@Override
	public XDoubleValue getValue() {
		double v = 0;
		try {
			v = Double.parseDouble(this.editor.getText());
		} catch(NumberFormatException nfe) {
			v = XValueUtils.generateDouble(this.editor.getText());
			this.editor.setText(Double.toString(v));
		}
		return XV.toValue(v);
	}
	
	public void onKeyPress(KeyPressEvent e) {
		
		char cc = e.getCharCode();
		
		switch(cc) {
		case KeyCodes.KEY_DELETE:
			return;
		case KeyCodes.KEY_BACKSPACE:
			return;
		case KeyCodes.KEY_LEFT:
			return;
		case KeyCodes.KEY_RIGHT:
			return;
		case KeyCodes.KEY_UP:
			return;
		case KeyCodes.KEY_DOWN:
			return;
		}
		
		if(cc >= '0' && cc <= '9')
			return;
		
		if(cc == '.' && this.editor.getText().indexOf('.') < 0)
			return;
		
		e.preventDefault();
		e.stopPropagation();
		
	}
	
}
