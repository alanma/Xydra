package org.xydra.gwt.editor.value;

import org.xydra.base.Base;
import org.xydra.base.XAddress;

import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.TextBox;

public class XAddressEditor extends AtomicXValueEditor<XAddress> implements KeyPressHandler,
		KeyDownHandler {

	private static final String nameStartChar = "A-Z_a-z\\xC0-\\xD6\\xD8-\\xF6"
			+ "\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D"
			+ "\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF" + "\\uFDF0-\\uFFFD";
	private static final String nameChar = nameStartChar
			+ "\\-\\.0-9\\xB7\\u0300-\u036F\\u203F-\\u2040/";
	@SuppressWarnings("unused")
	private static final String startClass = "[" + nameStartChar + "]";
	@SuppressWarnings("unused")
	private static final String nameClass = "[" + nameChar + "]";

	private final TextBox editor = new TextBox();

	public XAddressEditor(final XAddress value, final EditListener listener) {
		super(listener);

		if (value != null) {
			this.editor.setText(value.toString());
		}

		this.editor.addKeyPressHandler(this);
		this.editor.addKeyDownHandler(this);

		initWidget(this.editor);
	}

	public void setValue(final XAddress value) {
		if (value != null) {
			this.editor.setText(value.toString());
		}
	}

	@Override
	public XAddress getValue() {
		final String text = this.editor.getText();
		if (text.equals("")) {
			return null;
		}
		final XAddress address = Base.toAddress(text);
		if (address == null) {
			return null;
		}
		return address;
	}

	@Override
	public void onKeyPress(final KeyPressEvent e) {

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
		// String c = new String(new char[] { cc });
		//
		// boolean allowed;
		//
		// if(this.editor.getCursorPos() == 0)
		// allowed = c.matches(startClass);
		// else
		// allowed = c.matches(nameClass);
		//
		// if(!allowed) {
		// e.preventDefault();
		// e.stopPropagation();
		// }

	}

	@Override
	public void onKeyDown(final KeyDownEvent e) {
		// boolean allowed = true;
		//
		// switch(e.getNativeKeyCode()) {
		// case KeyCodes.KEY_BACKSPACE: {
		// if(this.editor.getSelectionLength() == 0) {
		// if(this.editor.getCursorPos() != 1)
		// break;
		// String s = this.editor.getText();
		// if(s.length() <= 1)
		// break;
		// String c = s.substring(1, 2);
		// allowed = c.matches(startClass);
		// break;
		// }
		// }
		// //$FALL-THROUGH$
		// case KeyCodes.KEY_DELETE: {
		// if(this.editor.getCursorPos() > 0)
		// break;
		// String s = this.editor.getText();
		// int n = this.editor.getSelectionLength();
		// if(n == 0)
		// n = 1;
		// if(n >= s.length())
		// break;
		// String c = s.substring(n, n + 1);
		// allowed = c.matches(startClass);
		//
		// }
		//
		// }
		//
		// if(!allowed) {
		// e.preventDefault();
		// e.stopPropagation();
		// }
		//
	}

}
