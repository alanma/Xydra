package org.xydra.gwt.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.base.XID;
import org.xydra.base.value.XIDListValue;
import org.xydra.base.value.XV;


public class XIDListEditor extends XCollectionEditor<XID,XIDListValue> {
	
	public XIDListEditor(Iterator<XID> value, EditListener listener) {
		super(listener);
		
		if(value == null)
			return;
		
		while(value.hasNext())
			add(new XIDEditor(value.next(), this));
		
	}
	
	@Override
	protected XIDListValue asCollectionValue(Iterator<XID> entries) {
		List<XID> lst = new ArrayList<XID>();
		while(entries.hasNext())
			lst.add(entries.next());
		return XV.toIDListValue(lst);
	}
	
	@Override
	public void add() {
		add(new XIDEditor(null, getListenerForEntry()));
		changed();
	}
	
}
