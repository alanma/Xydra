package org.xydra.client.gwt.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.core.XX;
import org.xydra.core.value.XBooleanListValue;
import org.xydra.core.value.XBooleanValue;


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
		return XX.toBooleanListValue(lst);
	}
	
	@Override
	public void add() {
		add(new XBooleanEditor(false, getListenerForEntry()));
		changed();
	}
	
}
