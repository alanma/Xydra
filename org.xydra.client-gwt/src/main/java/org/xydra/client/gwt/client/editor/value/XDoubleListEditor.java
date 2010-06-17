package org.xydra.client.gwt.client.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.core.XX;
import org.xydra.core.value.XDoubleListValue;
import org.xydra.core.value.XDoubleValue;


public class XDoubleListEditor extends XListEditor<XDoubleValue,XDoubleListValue> {
	
	public XDoubleListEditor(Iterator<Double> value, EditListener listener) {
		super(listener);
		
		if(value == null)
			return;
		
		while(value.hasNext())
			add(new XDoubleEditor(value.next(), this));
		
	}
	
	@Override
	protected XDoubleListValue asListValue(Iterator<XDoubleValue> entries) {
		List<Double> lst = new ArrayList<Double>();
		while(entries.hasNext())
			lst.add(entries.next().contents());
		return XX.toDoubleListValue(lst);
	}
	
	@Override
	public void add() {
		add(new XDoubleEditor(0.0, getListenerForEntry()));
		changed();
	}
	
}
