package org.xydra.client.gwt.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.core.X;
import org.xydra.core.model.XID;
import org.xydra.core.value.XIDSetValue;
import org.xydra.core.value.XIDValue;


public class XIDSetEditor extends XCollectionEditor<XIDValue,XIDSetValue> {
	
	public XIDSetEditor(Iterator<XID> value, EditListener listener) {
		super(listener);
		
		if(value == null)
			return;
		
		while(value.hasNext())
			add(new XIDEditor(value.next(), this));
		
	}
	
	@Override
	protected XIDSetValue asCollectionValue(Iterator<XIDValue> entries) {
		List<XID> lst = new ArrayList<XID>();
		while(entries.hasNext())
			lst.add(entries.next().contents());
		return X.getValueFactory().createIDSetValue(lst);
	}
	
	@Override
	public void add() {
		add(new XIDEditor(null, getListenerForEntry()));
		changed();
	}
	
}
