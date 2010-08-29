package org.xydra.client.gwt.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.core.value.XDoubleListValue;
import org.xydra.core.value.XDoubleValue;
import org.xydra.core.value.XV;


public class XDoubleListEditor extends XCollectionEditor<XDoubleValue,XDoubleListValue> {
	
	public XDoubleListEditor(Iterator<Double> value, EditListener listener) {
		super(listener);
		
		if(value == null)
			return;
		
		while(value.hasNext())
			add(new XDoubleEditor(value.next(), this));
		
	}
	
	@Override
	protected XDoubleListValue asCollectionValue(Iterator<XDoubleValue> entries) {
		List<Double> lst = new ArrayList<Double>();
		while(entries.hasNext())
			lst.add(entries.next().contents());
		return XV.toValue(lst);
	}
	
	@Override
	public void add() {
		add(new XDoubleEditor(0.0, getListenerForEntry()));
		changed();
	}
	
}
