package org.xydra.client.gwt.client.editor.value;

import java.util.ArrayList;
import java.util.List;

import org.xydra.core.X;
import org.xydra.core.model.XID;
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



/**
 * Collection of procedures to convert XValues to different types but keep as
 * much information as possible while doing so.
 */
abstract public class XValueUtils {
	
	@SuppressWarnings("unchecked")
	protected static Object listGetFirst(XListValue v) {
		if(!v.isEmpty())
			return v.get(0);
		else
			return "";
	}
	
	@SuppressWarnings("unchecked")
	static private String asString(XValue value) {
		if(value == null)
			return "";
		if(value instanceof XListValue<?>) {
			return listGetFirst((XListValue)value).toString();
		} else
			return value.toString();
	}
	
	static public XStringValue asStringValue(XValue value) {
		if(value instanceof XStringValue) {
			return (XStringValue)value;
		}
		return X.getValueFactory().createStringValue(asString(value));
	}
	
	@SuppressWarnings("unchecked")
	static public XStringListValue asStringListValue(XValue value) {
		if(value == null)
			return X.getValueFactory().createStringListValue(new String[] {});
		else if(value instanceof XStringListValue)
			return (XStringListValue)value;
		else if(value instanceof XListValue<?>) {
			XListValue old = (XListValue)value;
			String[] list = new String[old.size()];
			for(int i = 0; i < list.length; i++) {
				list[i] = old.get(i).toString();
			}
			return X.getValueFactory().createStringListValue(list);
		} else
			return X.getValueFactory().createStringListValue(new String[] { value.toString() });
	}
	
	static public XID asXID(XValue value) {
		
		if(value == null)
			return X.getIDProvider().createUniqueID();
		
		if(value instanceof XIDValue)
			return ((XIDValue)value).contents();
		else if(value instanceof XIDListValue) {
			XIDListValue lv = (XIDListValue)value;
			if(!lv.isEmpty())
				return lv.get(0);
			else
				return X.getIDProvider().createUniqueID();
		} else {
			XID id = generateXid(asString(value));
			if(id == null) {
				return X.getIDProvider().createUniqueID();
			}
			return id;
		}
	}
	
	static public XIDValue asXIDValue(XValue value) {
		
		if(value == null)
			return X.getValueFactory().createIDValue(X.getIDProvider().createUniqueID());
		
		if(value instanceof XIDValue)
			return (XIDValue)value;
		else
			return X.getValueFactory().createIDValue(asXID(value));
	}
	
	private static final String nameStartChar = "A-Z_a-z\\xC0-\\xD6\\xD8-\\xF6"
	        + "\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D"
	        + "\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF" + "\\uFDF0-\\uFFFD";
	private static final String nameChar = nameStartChar
	        + "\\-\\.0-9\\xB7\\u0300-\u036F\\u203F-\\u2040";
	private static final String startClass = "[" + nameStartChar + "]";
	
	public static XID generateXid(String string) {
		
		String cleaned = string.replaceAll("[^" + nameChar + "]+", "");
		
		if(cleaned.length() == 0)
			return null;
		
		if(!cleaned.substring(0, 1).matches(startClass))
			cleaned = "_" + cleaned;
		
		return X.getIDProvider().fromString(cleaned);
	}
	
	@SuppressWarnings("unchecked")
	static public XIDListValue asXIDListValue(XValue value) {
		if(value == null)
			return X.getValueFactory().createIDListValue(new XID[] {});
		else if(value instanceof XIDListValue)
			return (XIDListValue)value;
		else if(value instanceof XListValue<?>) {
			XListValue old = (XListValue)value;
			List<XID> ids = new ArrayList<XID>();
			for(Object obj : old) {
				XID id = generateXid(obj.toString());
				if(id != null) {
					ids.add(id);
				}
			}
			XID[] ida = new XID[ids.size()];
			ida = ids.toArray(ida);
			return X.getValueFactory().createIDListValue(ida);
		} else
			return X.getValueFactory().createIDListValue(new XID[] { asXID(value) });
	}
	
	@SuppressWarnings("unchecked")
	static public double asDouble(XValue value) {
		
		if(value == null)
			return 0.0;
		
		if(value instanceof XDoubleValue)
			return ((XDoubleValue)value).contents();
		if(value instanceof XIntegerValue)
			return ((XIntegerValue)value).contents();
		if(value instanceof XLongValue)
			return ((XLongValue)value).contents();
		if(value instanceof XListValue<?>) {
			Object o = listGetFirst((XListValue)value);
			if(o instanceof Double)
				return (Double)o;
			if(o instanceof Integer)
				return (Integer)o;
			if(o instanceof Long)
				return (Long)o;
			return generateDouble(o.toString());
		} else
			return generateDouble(value.toString());
	}
	
	static public XDoubleValue asDoubleValue(XValue value) {
		
		if(value == null)
			return X.getValueFactory().createDoubleValue(0.0);
		
		if(value instanceof XDoubleValue)
			return (XDoubleValue)value;
		else
			return X.getValueFactory().createDoubleValue(asDouble(value));
	}
	
	static public double generateDouble(String string) {
		try {
			return Double.parseDouble(string);
		} catch(NumberFormatException nfe) {
			String cleaned = string.replaceAll("[^0-9.]", "");
			try {
				return Double.parseDouble(cleaned);
			} catch(NumberFormatException nfe2) {
				return 0.0;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	static public XDoubleListValue asDoubleListValue(XValue value) {
		if(value == null)
			return X.getValueFactory().createDoubleListValue(new Double[] {});
		else if(value instanceof XDoubleListValue)
			return (XDoubleListValue)value;
		else if(value instanceof XIntegerListValue) {
			XIntegerListValue old = (XIntegerListValue)value;
			double[] list = new double[old.size()];
			for(int i = 0; i < list.length; i++) {
				list[i] = old.get(i);
			}
			return X.getValueFactory().createDoubleListValue(list);
		} else if(value instanceof XLongListValue) {
			XLongListValue old = (XLongListValue)value;
			double[] list = new double[old.size()];
			for(int i = 0; i < list.length; i++) {
				list[i] = old.get(i);
			}
			return X.getValueFactory().createDoubleListValue(list);
		} else if(value instanceof XListValue<?>) {
			XListValue old = (XListValue)value;
			double[] list = new double[old.size()];
			for(int i = 0; i < list.length; i++) {
				list[i] = generateDouble(old.get(i).toString());
			}
			return X.getValueFactory().createDoubleListValue(list);
		} else if(value instanceof XLongValue) {
			return X.getValueFactory().createDoubleListValue(
			        new double[] { ((XLongValue)value).contents() });
		} else if(value instanceof XIntegerValue) {
			return X.getValueFactory().createDoubleListValue(
			        new double[] { ((XIntegerValue)value).contents() });
		} else
			return X.getValueFactory().createDoubleListValue(
			        new double[] { generateDouble(asString(value)) });
	}
	
	@SuppressWarnings("unchecked")
	static public XIntegerListValue asIntegerListValue(XValue value) {
		if(value == null)
			return X.getValueFactory().createIntegerListValue(new int[] {});
		else if(value instanceof XIntegerListValue)
			return (XIntegerListValue)value;
		else if(value instanceof XDoubleListValue) {
			XDoubleListValue old = (XDoubleListValue)value;
			int[] list = new int[old.size()];
			for(int i = 0; i < list.length; i++) {
				list[i] = old.get(i).intValue();
			}
			return X.getValueFactory().createIntegerListValue(list);
		} else if(value instanceof XLongListValue) {
			XLongListValue old = (XLongListValue)value;
			int[] list = new int[old.size()];
			for(int i = 0; i < list.length; i++) {
				list[i] = old.get(i).intValue();
			}
			return X.getValueFactory().createIntegerListValue(list);
		} else if(value instanceof XListValue<?>) {
			XListValue old = (XListValue)value;
			int[] list = new int[old.size()];
			for(int i = 0; i < list.length; i++) {
				list[i] = (int)generateLong(old.get(i).toString());
			}
			return X.getValueFactory().createIntegerListValue(list);
		} else if(value instanceof XLongValue) {
			return X.getValueFactory().createIntegerListValue(
			        new int[] { (int)((XLongValue)value).contents() });
		} else if(value instanceof XDoubleValue) {
			return X.getValueFactory().createIntegerListValue(
			        new int[] { (int)((XDoubleValue)value).contents() });
		} else
			return X.getValueFactory().createIntegerListValue(
			        new int[] { (int)generateLong(asString(value)) });
	}
	
	@SuppressWarnings("unchecked")
	static public XLongListValue asLongListValue(XValue value) {
		if(value == null)
			return X.getValueFactory().createLongListValue(new long[] {});
		else if(value instanceof XLongListValue)
			return (XLongListValue)value;
		else if(value instanceof XIntegerListValue) {
			XIntegerListValue old = (XIntegerListValue)value;
			long[] list = new long[old.size()];
			for(int i = 0; i < list.length; i++) {
				list[i] = old.get(i);
			}
			return X.getValueFactory().createLongListValue(list);
		} else if(value instanceof XDoubleListValue) {
			XDoubleListValue old = (XDoubleListValue)value;
			long[] list = new long[old.size()];
			for(int i = 0; i < list.length; i++) {
				list[i] = old.get(i).longValue();
			}
			return X.getValueFactory().createLongListValue(list);
		} else if(value instanceof XListValue<?>) {
			XListValue old = (XListValue)value;
			long[] list = new long[old.size()];
			for(int i = 0; i < list.length; i++) {
				list[i] = generateLong(old.get(i).toString());
			}
			return X.getValueFactory().createLongListValue(list);
		} else if(value instanceof XDoubleValue) {
			return X.getValueFactory().createLongListValue(
			        new long[] { (long)((XDoubleValue)value).contents() });
		} else if(value instanceof XIntegerValue) {
			return X.getValueFactory().createLongListValue(
			        new long[] { ((XIntegerValue)value).contents() });
		} else
			return X.getValueFactory().createLongListValue(
			        new long[] { generateLong(asString(value)) });
	}
	
	static public boolean asBoolean(XValue value) {
		if(value == null)
			return false;
		if(value instanceof XBooleanValue)
			return ((XBooleanValue)value).contents();
		else if(value instanceof XBooleanListValue) {
			XBooleanListValue lv = (XBooleanListValue)value;
			if(!lv.isEmpty())
				return lv.get(0);
			else
				return false;
		} else
			return generateBoolean(asString(value));
	}
	
	static public XBooleanValue asBooleanValue(XValue value) {
		if(value != null && value instanceof XBooleanValue)
			return (XBooleanValue)value;
		return X.getValueFactory().createBooleanValue(asBoolean(value));
	}
	
	public static boolean generateBoolean(String s) {
		try {
			return Double.parseDouble(s) != 0;
		} catch(NumberFormatException nfe) {
			return Boolean.parseBoolean(s);
		}
	}
	
	@SuppressWarnings("unchecked")
	static public XBooleanListValue asBooleanListValue(XValue value) {
		if(value == null)
			return X.getValueFactory().createBooleanListValue(new boolean[] {});
		else if(value instanceof XBooleanListValue)
			return (XBooleanListValue)value;
		else if(value instanceof XListValue<?>) {
			XListValue old = (XListValue)value;
			boolean[] list = new boolean[old.size()];
			for(int i = 0; i < list.length; i++) {
				list[i] = generateBoolean(old.get(i).toString());
			}
			return X.getValueFactory().createBooleanListValue(list);
		} else if(value instanceof XBooleanValue) {
			return X.getValueFactory().createBooleanListValue(
			        new boolean[] { ((XBooleanValue)value).contents() });
		} else
			return X.getValueFactory().createBooleanListValue(new boolean[] { asBoolean(value) });
	}
	
	@SuppressWarnings("unchecked")
	static public long asLong(XValue value) {
		
		if(value == null)
			return 0L;
		
		if(value instanceof XDoubleValue)
			return (long)((XDoubleValue)value).contents();
		if(value instanceof XIntegerValue)
			return ((XIntegerValue)value).contents();
		if(value instanceof XLongValue)
			return ((XLongValue)value).contents();
		if(value instanceof XListValue<?>) {
			Object o = listGetFirst((XListValue)value);
			if(o instanceof Double)
				return ((Double)o).longValue();
			if(o instanceof Integer)
				return (Integer)o;
			if(o instanceof Long)
				return (Long)o;
			return generateLong(o.toString());
		} else
			return generateLong(value.toString());
	}
	
	static public XLongValue asLongValue(XValue value) {
		
		if(value == null)
			return X.getValueFactory().createLongValue(0L);
		
		if(value instanceof XLongValue)
			return (XLongValue)value;
		return X.getValueFactory().createLongValue(asLong(value));
	}
	
	static public XIntegerValue asIntegerValue(XValue value) {
		
		if(value == null)
			return X.getValueFactory().createIntegerValue(0);
		
		if(value instanceof XIntegerValue)
			return (XIntegerValue)value;
		return X.getValueFactory().createIntegerValue((int)asLong(value));
	}
	
	static public long generateLong(String string) {
		try {
			return Long.parseLong(string);
		} catch(NumberFormatException nfe) {
			String cleaned = string.replaceAll("[^0-9]", "");
			try {
				return Long.parseLong(cleaned);
			} catch(NumberFormatException nfe2) {
				return 0L;
			}
		}
	}
	
}
