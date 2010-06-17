package org.xydra.client.gwt.client.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.core.XX;
import org.xydra.core.value.XStringListValue;
import org.xydra.core.value.XStringValue;


public class XStringListEditor extends XListEditor<XStringValue,XStringListValue> {
	
	public XStringListEditor(Iterator<String> value, EditListener listener) {
		super(listener);
		
		if(value == null)
			return;
		
		while(value.hasNext())
			add(new XStringEditor(value.next(), this));
		
	}
	
	@Override
	protected XStringListValue asListValue(Iterator<XStringValue> entries) {
		List<String> lst = new ArrayList<String>();
		while(entries.hasNext())
			lst.add(entries.next().contents());
		return XX.toStringListValue(lst);
	}
	
	@Override
	public void add() {
		add(new XStringEditor(null, getListenerForEntry()));
		changed();
	}
	
}
