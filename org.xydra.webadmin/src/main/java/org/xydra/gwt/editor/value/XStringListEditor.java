package org.xydra.gwt.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.base.value.XStringListValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XV;

public class XStringListEditor extends XCollectionEditor<XStringValue, XStringListValue> {

	public XStringListEditor(final Iterator<String> value, final EditListener listener) {
		super(listener);

		if (value == null) {
			return;
		}

		while (value.hasNext()) {
			add(new XStringEditor(value.next(), this));
		}

	}

	@Override
	protected XStringListValue asCollectionValue(final Iterator<XStringValue> entries) {
		final List<String> lst = new ArrayList<String>();
		while (entries.hasNext()) {
			lst.add(entries.next().contents());
		}
		return XV.toStringListValue(lst);
	}

	@Override
	public void add() {
		add(new XStringEditor(null, getListenerForEntry()));
		changed();
	}

}
