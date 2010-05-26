package org.xydra.client.gwt.client.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.core.XX;
import org.xydra.core.model.XID;
import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XValue;



public class XIDListEditor extends XListEditor {
	
	public XIDListEditor(Iterator<XID> value, EditListener listener) {
		super(listener);
		
		if(value == null)
			return;
		
		while(value.hasNext())
			add(new XIDEditor(value.next(), this));
		
	}
	
	@Override
	protected XValue asListValue(Iterator<XValue> entries) {
		List<XID> lst = new ArrayList<XID>();
		while(entries.hasNext())
			lst.add(((XIDValue)entries.next()).contents());
		return XX.toIDListValue(lst);
	}
	
	@Override
	public void add() {
		add(new XIDEditor(null, this));
		changed();
	}
	
}
