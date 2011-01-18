package org.xydra.client.gwt.editor.value;

import org.xydra.base.XID;
import org.xydra.base.value.XBooleanListValue;
import org.xydra.base.value.XBooleanValue;
import org.xydra.base.value.XByteListValue;
import org.xydra.base.value.XCollectionValue;
import org.xydra.base.value.XDoubleListValue;
import org.xydra.base.value.XDoubleValue;
import org.xydra.base.value.XIDListValue;
import org.xydra.base.value.XIDSetValue;
import org.xydra.base.value.XIntegerListValue;
import org.xydra.base.value.XIntegerValue;
import org.xydra.base.value.XListValue;
import org.xydra.base.value.XLongListValue;
import org.xydra.base.value.XLongValue;
import org.xydra.base.value.XSetValue;
import org.xydra.base.value.XStringListValue;
import org.xydra.base.value.XStringSetValue;
import org.xydra.base.value.XStringValue;
import org.xydra.base.value.XValue;

import com.google.gwt.user.client.ui.Composite;


public abstract class XValueEditor extends Composite {
	
	public interface EditListener {
		
		void newValue(XValue value);
		
	}
	
	public abstract XValue getValue();
	
	public static XValueEditor get(XValue value, EditListener listener) {
		if(value instanceof XCollectionValue<?>) {
			if(value instanceof XListValue<?>) {
				if(value instanceof XStringListValue) {
					return new XStringListEditor(((XStringListValue)value).iterator(), listener);
				} else if(value instanceof XIDListValue) {
					return new XIDListEditor(((XIDListValue)value).iterator(), listener);
				} else if(value instanceof XBooleanListValue) {
					return new XBooleanListEditor(((XBooleanListValue)value).iterator(), listener);
				} else if(value instanceof XDoubleListValue) {
					return new XDoubleListEditor(((XDoubleListValue)value).iterator(), listener);
				} else if(value instanceof XLongListValue) {
					return new XLongListEditor(((XLongListValue)value).iterator(), listener);
				} else if(value instanceof XIntegerListValue) {
					return new XIntegerListEditor(((XIntegerListValue)value).iterator(), listener);
				} else if(value instanceof XByteListValue) {
					return new XByteListEditor(((XByteListValue)value).contents(), listener);
				}
				throw new RuntimeException("Unexpected XListValue type: " + value);
			} else if(value instanceof XSetValue<?>) {
				if(value instanceof XStringSetValue) {
					return new XStringSetEditor(((XStringSetValue)value).iterator(), listener);
				} else if(value instanceof XIDSetValue) {
					return new XIDSetEditor(((XIDSetValue)value).iterator(), listener);
				}
				throw new RuntimeException("Unexpected XSetValue type: " + value);
			}
			throw new RuntimeException("Unexpected XCollectionValue type: " + value);
		} else if(value instanceof XStringValue) {
			return new XStringEditor(((XStringValue)value).contents(), listener);
		} else if(value instanceof XID) {
			return new XIDEditor(((XID)value), listener);
		} else if(value instanceof XBooleanValue) {
			return new XBooleanEditor(((XBooleanValue)value).contents(), listener);
		} else if(value instanceof XDoubleValue) {
			return new XDoubleEditor(((XDoubleValue)value).contents(), listener);
		} else if(value instanceof XLongValue) {
			return new XLongEditor(((XLongValue)value).contents(), listener);
		} else if(value instanceof XIntegerValue) {
			return new XIntegerEditor(((XIntegerValue)value).contents(), listener);
		}
		throw new RuntimeException("Unexpected non-list XValue type: " + value);
	}
	
}
