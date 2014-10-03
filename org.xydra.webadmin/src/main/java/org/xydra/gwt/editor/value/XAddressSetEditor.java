package org.xydra.gwt.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.value.XAddressSetValue;
import org.xydra.base.value.XV;

public class XAddressSetEditor extends XCollectionEditor<XAddress, XAddressSetValue> {

	public XAddressSetEditor(Iterator<XAddress> value, EditListener listener) {
		super(listener);

		if (value == null)
			return;

		while (value.hasNext())
			add(new XAddressEditor(value.next(), this));

	}

	@Override
	protected XAddressSetValue asCollectionValue(Iterator<XAddress> entries) {
		List<XAddress> lst = new ArrayList<XAddress>();
		while (entries.hasNext())
			lst.add(entries.next());
		return XV.toAddressSetValue(lst);
	}

	@Override
	public void add() {
		add(new XAddressEditor(this.dummyAddress, getListenerForEntry()));
		changed();
	}

}
