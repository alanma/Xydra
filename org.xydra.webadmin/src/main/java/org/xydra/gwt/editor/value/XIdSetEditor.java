package org.xydra.gwt.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.base.XId;
import org.xydra.base.value.XIdSetValue;
import org.xydra.base.value.XV;

public class XIdSetEditor extends XCollectionEditor<XId, XIdSetValue> {

	public XIdSetEditor(Iterator<XId> value, EditListener listener) {
		super(listener);

		if (value == null)
			return;

		while (value.hasNext())
			add(new XIdEditor(value.next(), this));

	}

	@Override
	protected XIdSetValue asCollectionValue(Iterator<XId> entries) {
		List<XId> lst = new ArrayList<XId>();
		while (entries.hasNext())
			lst.add(entries.next());
		return XV.toIdSetValue(lst);
	}

	@Override
	public void add() {
		add(new XIdEditor(null, getListenerForEntry()));
		changed();
	}

}
