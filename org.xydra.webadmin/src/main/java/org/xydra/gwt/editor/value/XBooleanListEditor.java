package org.xydra.gwt.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.base.value.XBooleanListValue;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XV;


public class XBooleanListEditor extends XCollectionEditor<XBooleanValue,XBooleanListValue> {
	
	public XBooleanListEditor(Iterator<Boolean> value, EditListener listener) {
		super(listener);
		
		if(value == null)
			return;
		
		while(value.hasNext())
			add(new XBooleanEditor(value.next(), this));
		
	}
	
	@Override
	protected XBooleanListValue asCollectionValue(Iterator<XBooleanValue> entries) {
		List<Boolean> lst = new ArrayList<Boolean>();
		while(entries.hasNext())
			lst.add(entries.next().contents());
		return XV.toBooleanListValue(lst);
	}
	
	@Override
	public void add() {
		add(new XBooleanEditor(false, getListenerForEntry()));
		changed();
	}
	
}
