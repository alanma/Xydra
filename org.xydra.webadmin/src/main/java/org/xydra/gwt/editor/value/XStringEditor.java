package org.xydra.gwt.editor.value;

import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XV;

import com.google.gwt.user.client.ui.TextBox;

public class XStringEditor extends AtomicXValueEditor<XStringValue> {

	private final TextBox editor = new TextBox();

	public XStringEditor(final String oldValue, final EditListener listener) {
		super(listener);

		if (oldValue != null) {
			this.editor.setText(oldValue);
		}

		initWidget(this.editor);
	}

	@Override
	public XStringValue getValue() {
		return XV.toValue(this.editor.getText());
	}

}
