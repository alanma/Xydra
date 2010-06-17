package org.xydra.client.gwt.client.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.core.XX;
import org.xydra.core.value.XIntegerListValue;
import org.xydra.core.value.XIntegerValue;


public class XIntegerListEditor extends XListEditor<XIntegerValue,XIntegerListValue> {
	
	public XIntegerListEditor(Iterator<Integer> value, EditListener listener) {
		super(listener);
		
		if(value == null)
			return;
		
		while(value.hasNext())
			add(new XIntegerEditor(value.next(), this));
		
	}
	
	@Override
	protected XIntegerListValue asListValue(Iterator<XIntegerValue> entries) {
		List<Integer> lst = new ArrayList<Integer>();
		while(entries.hasNext())
			lst.add(entries.next().contents());
		return XX.toIntegerListValue(lst);
	}
	
	@Override
	public void add() {
		add(new XIntegerEditor(0, getListenerForEntry()));
		changed();
	}
	
}
