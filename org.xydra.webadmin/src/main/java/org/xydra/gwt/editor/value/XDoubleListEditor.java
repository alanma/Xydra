package org.xydra.gwt.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.base.value.XDoubleListValue;
import org.xydra.base.value.XDoubleValue;
import org.xydra.base.value.XV;

public class XDoubleListEditor extends XCollectionEditor<XDoubleValue, XDoubleListValue> {

	public XDoubleListEditor(final Iterator<Double> value, final EditListener listener) {
		super(listener);

		if (value == null) {
			return;
		}

		while (value.hasNext()) {
			add(new XDoubleEditor(value.next(), this));
		}

	}

	@Override
	protected XDoubleListValue asCollectionValue(final Iterator<XDoubleValue> entries) {
		final List<Double> lst = new ArrayList<Double>();
		while (entries.hasNext()) {
			lst.add(entries.next().contents());
		}
		return XV.toDoubleListValue(lst);
	}

	@Override
	public void add() {
		add(new XDoubleEditor(0.0, getListenerForEntry()));
		changed();
	}

}
