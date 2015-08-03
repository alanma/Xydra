package org.xydra.gwt.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.base.XId;
import org.xydra.base.value.XIdListValue;
import org.xydra.base.value.XV;

public class XIdListEditor extends XCollectionEditor<XId, XIdListValue> {

	public XIdListEditor(final Iterator<XId> value, final EditListener listener) {
		super(listener);

		if (value == null) {
			return;
		}

		while (value.hasNext()) {
			add(new XIdEditor(value.next(), this));
		}

	}

	@Override
	protected XIdListValue asCollectionValue(final Iterator<XId> entries) {
		final List<XId> lst = new ArrayList<XId>();
		while (entries.hasNext()) {
			lst.add(entries.next());
		}
		return XV.toIdListValue(lst);
	}

	@Override
	public void add() {
		add(new XIdEditor(null, getListenerForEntry()));
		changed();
	}

}
