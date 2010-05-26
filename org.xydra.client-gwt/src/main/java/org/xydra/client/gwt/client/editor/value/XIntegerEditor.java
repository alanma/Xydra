package org.xydra.client.gwt.client.editor.value;

import org.xydra.core.X;
import org.xydra.core.value.XValue;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.TextBox;



public class XIntegerEditor extends AtomicXValueEditor implements KeyPressHandler {
	
	private final TextBox editor = new TextBox();
	
	public XIntegerEditor(int oldValue, EditListener listener) {
		super(listener);
		
		this.editor.setText(Integer.toString(oldValue));
		
		this.editor.addKeyPressHandler(this);
		
		initWidget(this.editor);
	}
	
	@Override
	public XValue getValue() {
		int v = 0;
		try {
			v = Integer.parseInt(this.editor.getText());
		} catch(NumberFormatException nfe) {
			v = (int)XValueUtils.generateLong(this.editor.getText());
			this.editor.setText(Integer.toString(v));
		}
		return X.getValueFactory().createIntegerValue(v);
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
		
		e.preventDefault();
		e.stopPropagation();
		
	}
	
}
