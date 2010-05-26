package org.xydra.client.gwt.client.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.core.XX;
import org.xydra.core.value.XBooleanValue;
import org.xydra.core.value.XValue;



public class XBooleanListEditor extends XListEditor {
	
	public XBooleanListEditor(Iterator<Boolean> value, EditListener listener) {
		super(listener);
		
		if(value == null)
			return;
		
		while(value.hasNext())
			add(new XBooleanEditor(value.next(), this));
		
	}
	
	@Override
	protected XValue asListValue(Iterator<XValue> entries) {
		List<Boolean> lst = new ArrayList<Boolean>();
		while(entries.hasNext())
			lst.add(((XBooleanValue)entries.next()).contents());
		return XX.toBooleanListValue(lst);
	}
	
	@Override
	public void add() {
		add(new XBooleanEditor(false, this));
		changed();
	}
	
}
