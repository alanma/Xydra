package org.xydra.client.gwt.client.editor.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xydra.core.XX;
import org.xydra.core.value.XDoubleValue;
import org.xydra.core.value.XValue;



public class XDoubleListEditor extends XListEditor {
	
	public XDoubleListEditor(Iterator<Double> value, EditListener listener) {
		super(listener);
		
		if(value == null)
			return;
		
		while(value.hasNext())
			add(new XDoubleEditor(value.next(), this));
		
	}
	
	@Override
	protected XValue asListValue(Iterator<XValue> entries) {
		List<Double> lst = new ArrayList<Double>();
		while(entries.hasNext())
			lst.add(((XDoubleValue)entries.next()).contents());
		return XX.toDoubleListValue(lst);
	}
	
	@Override
	public void add() {
		add(new XDoubleEditor(0.0, this));
		changed();
	}
	
}
