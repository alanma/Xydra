package org.xydra.gwt.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.base.XAddress;
import org.xydra.base.value.XAddressSortedSetValue;
import org.xydra.base.value.XV;

public class XAddressSortedSetEditor extends XCollectionEditor<XAddress, XAddressSortedSetValue> {

	public XAddressSortedSetEditor(final Iterator<XAddress> value, final EditListener listener) {
		super(listener);

		if (value == null) {
			return;
		}

		while (value.hasNext()) {
			add(new XAddressEditor(value.next(), this));
		}

	}

	@Override
	protected XAddressSortedSetValue asCollectionValue(final Iterator<XAddress> entries) {
		final List<XAddress> lst = new ArrayList<XAddress>();
		while (entries.hasNext()) {
			lst.add(entries.next());
		}
		return XV.toAddressSortedSetValue(lst);
	}

	@Override
	public void add() {
		add(new XAddressEditor(this.dummyAddress, getListenerForEntry()));
		changed();
	}

}
