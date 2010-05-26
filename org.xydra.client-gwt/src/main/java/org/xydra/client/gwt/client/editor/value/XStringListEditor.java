package org.xydra.client.gwt.client.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.core.XX;
import org.xydra.core.value.XStringValue;
import org.xydra.core.value.XValue;



public class XStringListEditor extends XListEditor {
	
	public XStringListEditor(Iterator<String> value, EditListener listener) {
		super(listener);
		
		if(value == null)
			return;
		
		while(value.hasNext())
			add(new XStringEditor(value.next(), this));
		
	}
	
	@Override
	protected XValue asListValue(Iterator<XValue> entries) {
		List<String> lst = new ArrayList<String>();
		while(entries.hasNext())
			lst.add(((XStringValue)entries.next()).contents());
		return XX.toStringListValue(lst);
	}
	
	@Override
	public void add() {
		add(new XStringEditor(null, this));
		changed();
	}
	
}
