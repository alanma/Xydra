package org.xydra.gwt.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.base.value.XLongListValue;
import org.xydra.base.value.XLongValue;
import org.xydra.base.value.XV;

public class XLongListEditor extends XCollectionEditor<XLongValue, XLongListValue> {

	public XLongListEditor(Iterator<Long> value, EditListener listener) {
		super(listener);

		if (value == null)
			return;

		while (value.hasNext())
			add(new XLongEditor(value.next(), this));

	}

	@Override
	protected XLongListValue asCollectionValue(Iterator<XLongValue> entries) {
		List<Long> lst = new ArrayList<Long>();
		while (entries.hasNext())
			lst.add(entries.next().contents());
		return XV.toLongListValue(lst);
	}

	@Override
	public void add() {
		add(new XLongEditor(0L, getListenerForEntry()));
		changed();
	}

}
