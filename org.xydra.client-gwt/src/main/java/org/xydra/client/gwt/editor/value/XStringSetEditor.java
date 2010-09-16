package org.xydra.client.gwt.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.core.value.XStringSetValue;
import org.xydra.core.value.XStringValue;
import org.xydra.core.value.XV;


public class XStringSetEditor extends XCollectionEditor<XStringValue,XStringSetValue> {
	
	public XStringSetEditor(Iterator<String> value, EditListener listener) {
		super(listener);
		
		if(value == null)
			return;
		
		while(value.hasNext())
			add(new XStringEditor(value.next(), this));
		
	}
	
	@Override
	protected XStringSetValue asCollectionValue(Iterator<XStringValue> entries) {
		List<String> lst = new ArrayList<String>();
		while(entries.hasNext())
			lst.add(entries.next().contents());
		return XV.toStringSetValue(lst);
	}
	
	@Override
	public void add() {
		add(new XStringEditor(null, getListenerForEntry()));
		changed();
	}
	
}
