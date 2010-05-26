package org.xydra.client.gwt.client.editor.value;

import org.xydra.core.X;
import org.xydra.core.value.XValue;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.TextBox;



public class XDoubleEditor extends AtomicXValueEditor implements KeyPressHandler {
	
	private final TextBox editor = new TextBox();
	
	public XDoubleEditor(double oldValue, EditListener listener) {
		super(listener);
		
		this.editor.setText(Double.toString(oldValue));
		
		this.editor.addKeyPressHandler(this);
		
		initWidget(this.editor);
	}
	
	@Override
	public XValue getValue() {
		double v = 0;
		try {
			v = Double.parseDouble(this.editor.getText());
		} catch(NumberFormatException nfe) {
			v = XValueUtils.generateDouble(this.editor.getText());
			this.editor.setText(Double.toString(v));
		}
		return X.getValueFactory().createDoubleValue(v);
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
