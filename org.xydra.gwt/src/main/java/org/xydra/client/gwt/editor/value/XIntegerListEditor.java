package org.xydra.client.gwt.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.base.value.XIntegerListValue;
import org.xydra.base.value.XIntegerValue;
import org.xydra.core.value.XV;


public class XIntegerListEditor extends XCollectionEditor<XIntegerValue,XIntegerListValue> {
	
	public XIntegerListEditor(Iterator<Integer> value, EditListener listener) {
		super(listener);
		
		if(value == null)
			return;
		
		while(value.hasNext())
			add(new XIntegerEditor(value.next(), this));
		
	}
	
	@Override
	protected XIntegerListValue asCollectionValue(Iterator<XIntegerValue> entries) {
		List<Integer> lst = new ArrayList<Integer>();
		while(entries.hasNext())
			lst.add(entries.next().contents());
		return XV.toIntegerListValue(lst);
	}
	
	@Override
	public void add() {
		add(new XIntegerEditor(0, getListenerForEntry()));
		changed();
	}
	
}
