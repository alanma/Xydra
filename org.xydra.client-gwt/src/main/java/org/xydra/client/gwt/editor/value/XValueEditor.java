package org.xydra.client.gwt.editor.value;

import org.xydra.core.value.XBooleanListValue;
import org.xydra.core.value.XBooleanValue;
import org.xydra.core.value.XDoubleListValue;
import org.xydra.core.value.XDoubleValue;
import org.xydra.core.value.XIDListValue;
import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XIntegerListValue;
import org.xydra.core.value.XIntegerValue;
import org.xydra.core.value.XListValue;
import org.xydra.core.value.XLongListValue;
import org.xydra.core.value.XLongValue;
import org.xydra.core.value.XStringListValue;
import org.xydra.core.value.XStringValue;
import org.xydra.core.value.XValue;

import com.google.gwt.user.client.ui.Composite;



public abstract class XValueEditor extends Composite {
	
	public interface EditListener {
		
		void newValue(XValue value);
		
	}
	
	public abstract XValue getValue();
	
	public static XValueEditor get(XValue value, EditListener listener) {
		if(value instanceof XListValue<?>) {
			if(value instanceof XStringListValue)
				return new XStringListEditor(((XStringListValue)value).iterator(), listener);
			else if(value instanceof XIDListValue)
				return new XIDListEditor(((XIDListValue)value).iterator(), listener);
			else if(value instanceof XBooleanListValue)
				return new XBooleanListEditor(((XBooleanListValue)value).iterator(), listener);
			else if(value instanceof XDoubleListValue)
				return new XDoubleListEditor(((XDoubleListValue)value).iterator(), listener);
			else if(value instanceof XLongListValue)
				return new XLongListEditor(((XLongListValue)value).iterator(), listener);
			else if(value instanceof XIntegerListValue)
				return new XIntegerListEditor(((XIntegerListValue)value).iterator(), listener);
			else
				throw new RuntimeException("Unexpected XListValue type: " + value);
		} else if(value instanceof XStringValue)
			return new XStringEditor(((XStringValue)value).contents(), listener);
		else if(value instanceof XIDValue)
			return new XIDEditor(((XIDValue)value).contents(), listener);
		else if(value instanceof XBooleanValue)
			return new XBooleanEditor(((XBooleanValue)value).contents(), listener);
		else if(value instanceof XDoubleValue)
			return new XDoubleEditor(((XDoubleValue)value).contents(), listener);
		else if(value instanceof XLongValue)
			return new XLongEditor(((XLongValue)value).contents(), listener);
		else if(value instanceof XIntegerValue)
			return new XIntegerEditor(((XIntegerValue)value).contents(), listener);
		else
			throw new RuntimeException("Unexpected non-list XValue type: " + value);
	}
	
}
