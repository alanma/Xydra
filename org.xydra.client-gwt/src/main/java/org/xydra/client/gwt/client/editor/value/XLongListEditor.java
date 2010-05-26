package org.xydra.client.gwt.client.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.core.XX;
import org.xydra.core.value.XLongValue;
import org.xydra.core.value.XValue;



public class XLongListEditor extends XListEditor {
	
	public XLongListEditor(Iterator<Long> value, EditListener listener) {
		super(listener);
		
		if(value == null)
			return;
		
		while(value.hasNext())
			add(new XLongEditor(value.next(), this));
		
	}
	
	@Override
	protected XValue asListValue(Iterator<XValue> entries) {
		List<Long> lst = new ArrayList<Long>();
		while(entries.hasNext())
			lst.add(((XLongValue)entries.next()).contents());
		return XX.toLongListValue(lst);
	}
	
	@Override
	public void add() {
		add(new XLongEditor(0L, this));
		changed();
	}
	
}
