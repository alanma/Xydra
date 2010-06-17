package org.xydra.client.gwt.client.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.core.XX;
import org.xydra.core.model.XID;
import org.xydra.core.value.XIDListValue;
import org.xydra.core.value.XIDValue;


public class XIDListEditor extends XListEditor<XIDValue,XIDListValue> {
	
	public XIDListEditor(Iterator<XID> value, EditListener listener) {
		super(listener);
		
		if(value == null)
			return;
		
		while(value.hasNext())
			add(new XIDEditor(value.next(), this));
		
	}
	
	@Override
	protected XIDListValue asListValue(Iterator<XIDValue> entries) {
		List<XID> lst = new ArrayList<XID>();
		while(entries.hasNext())
			lst.add(entries.next().contents());
		return XX.toIDListValue(lst);
	}
	
	@Override
	public void add() {
		add(new XIDEditor(null, getListenerForEntry()));
		changed();
	}
	
}
