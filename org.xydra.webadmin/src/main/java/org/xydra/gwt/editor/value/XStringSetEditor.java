package org.xydra.gwt.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.base.value.XStringSetValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XV;

public class XStringSetEditor extends XCollectionEditor<XStringValue, XStringSetValue> {

	public XStringSetEditor(final Iterator<String> value, final EditListener listener) {
		super(listener);

		if (value == null) {
			return;
		}

		while (value.hasNext()) {
			add(new XStringEditor(value.next(), this));
		}

	}

	@Override
	protected XStringSetValue asCollectionValue(final Iterator<XStringValue> entries) {
		final List<String> lst = new ArrayList<String>();
		while (entries.hasNext()) {
			lst.add(entries.next().contents());
		}
		return XV.toStringSetValue(lst);
	}

	@Override
	public void add() {
		add(new XStringEditor(null, getListenerForEntry()));
		changed();
	}

}
