package org.xydra.gwt.editor.value;

import org.xydra.base.value.XLongValue;
import org.xydra.base.value.XV;

import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.TextBox;

public class XLongEditor extends AtomicXValueEditor<XLongValue> implements KeyPressHandler {

	private final TextBox editor = new TextBox();

	public XLongEditor(long oldValue, EditListener listener) {
		super(listener);

		this.editor.setText(Long.toString(oldValue));

		this.editor.addKeyPressHandler(this);

		initWidget(this.editor);
	}

	@Override
	public XLongValue getValue() {
		long v = 0L;
		// try {
		v = Long.parseLong(this.editor.getText());
		// } catch(NumberFormatException nfe) {
		// v = XValueUtils.generateLong(this.editor.getText());
		// this.editor.setText(Long.toString(v));
		// }
		return XV.toValue(v);
	}

	@Override
	public void onKeyPress(KeyPressEvent e) {

		// char cc = e.getCharCode();
		//
		// switch(cc) {
		// case KeyCodes.KEY_DELETE:
		// return;
		// case KeyCodes.KEY_BACKSPACE:
		// return;
		// case KeyCodes.KEY_LEFT:
		// return;
		// case KeyCodes.KEY_RIGHT:
		// return;
		// case KeyCodes.KEY_UP:
		// return;
		// case KeyCodes.KEY_DOWN:
		// return;
		// }
		//
		// if(cc >= '0' && cc <= '9')
		// return;
		//
		// e.preventDefault();
		// e.stopPropagation();
		//
	}

}
