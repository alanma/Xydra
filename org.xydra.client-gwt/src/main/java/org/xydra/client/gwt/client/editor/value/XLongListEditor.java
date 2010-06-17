package org.xydra.client.gwt.client.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.core.XX;
import org.xydra.core.value.XLongListValue;
import org.xydra.core.value.XLongValue;


public class XLongListEditor extends XListEditor<XLongValue,XLongListValue> {
	
	public XLongListEditor(Iterator<Long> value, EditListener listener) {
		super(listener);
		
		if(value == null)
			return;
		
		while(value.hasNext())
			add(new XLongEditor(value.next(), this));
		
	}
	
	@Override
	protected XLongListValue asListValue(Iterator<XLongValue> entries) {
		List<Long> lst = new ArrayList<Long>();
		while(entries.hasNext())
			lst.add(entries.next().contents());
		return XX.toLongListValue(lst);
	}
	
	@Override
	public void add() {
		add(new XLongEditor(0L, getListenerForEntry()));
		changed();
	}
	
}
