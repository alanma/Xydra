package org.xydra.client.gwt.client.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.core.XX;
import org.xydra.core.value.XIntegerValue;
import org.xydra.core.value.XValue;



public class XIntegerListEditor extends XListEditor {
	
	public XIntegerListEditor(Iterator<Integer> value, EditListener listener) {
		super(listener);
		
		if(value == null)
			return;
		
		while(value.hasNext())
			add(new XIntegerEditor(value.next(), this));
		
	}
	
	@Override
	protected XValue asListValue(Iterator<XValue> entries) {
		List<Integer> lst = new ArrayList<Integer>();
		while(entries.hasNext())
			lst.add(((XIntegerValue)entries.next()).contents());
		return XX.toIntegerListValue(lst);
	}
	
	@Override
	public void add() {
		add(new XIntegerEditor(0, this));
		changed();
	}
	
}
