package org.xydra.client.gwt.editor.value;

import java.util.Iterator;

import org.xydra.core.XX;
import org.xydra.core.model.XID;
import org.xydra.core.value.XBooleanListValue;
import org.xydra.core.value.XBooleanValue;
import org.xydra.core.value.XByteListValue;
import org.xydra.core.value.XDoubleListValue;
import org.xydra.core.value.XDoubleValue;
import org.xydra.core.value.XIDListValue;
import org.xydra.core.value.XIDSetValue;
import org.xydra.core.value.XIDValue;
import org.xydra.core.value.XIntegerListValue;
import org.xydra.core.value.XIntegerValue;
import org.xydra.core.value.XListValue;
import org.xydra.core.value.XLongListValue;
import org.xydra.core.value.XLongValue;
import org.xydra.core.value.XStringListValue;
import org.xydra.core.value.XStringSetValue;
import org.xydra.core.value.XValue;
import org.xydra.index.iterator.AbstractFilteringIterator;
import org.xydra.index.iterator.AbstractTransformingIterator;
import org.xydra.index.iterator.NoneIterator;
import org.xydra.index.iterator.SingleValueIterator;


/**
 * Collection of procedures to convert XValues to different types but keep as
 * much information as possible while doing so.
 */
abstract public class XValueUtils {
	
	protected static Object listGetFirst(XListValue<?> v) {
		if(!v.isEmpty()) {
			return v.get(0);
		} else {
			return "";
		}
	}
	
	static public String asString(XValue value) {
		if(value == null) {
			return "";
		}
		if(value instanceof XByteListValue) {
			return byteArrayToString(((XByteListValue)value).contents());
		} else if(value instanceof XListValue<?>) {
			return listGetFirst((XListValue<?>)value).toString();
		} else {
			return value.toString();
		}
	}
	
	private static String byteArrayToString(byte[] bytes) {
		char[] chars = new char[bytes.length];
		for(int i = 0; i < bytes.length; i++) {
			chars[i] = (char)bytes[i];
		}
		return new String(chars);
	}
	
	static public Iterator<String> asStringList(XValue value) {
		if(value == null) {
			return new NoneIterator<String>();
		} else if(value instanceof XStringListValue) {
			return ((XStringListValue)value).iterator();
		} else if(value instanceof XStringSetValue) {
			return ((XStringSetValue)value).iterator();
		} else if(value instanceof XByteListValue) {
			return new SingleValueIterator<String>(byteArrayToString(((XByteListValue)value)
			        .contents()));
		} else if(value instanceof XListValue<?>) {
			return transform((XListValue<?>)value);
		} else {
			return new SingleValueIterator<String>(value.toString());
		}
	}
	
	static private <E> Iterator<String> transform(XListValue<E> value) {
		return new AbstractTransformingIterator<E,String>(value.iterator()) {
			@Override
			public String transform(E in) {
				return in.toString();
			}
		};
	}
	
	static public XID asXID(XValue value) {
		
		if(value == null) {
			return XX.createUniqueID();
		}
		
		if(value instanceof XIDValue) {
			return ((XIDValue)value).contents();
		} else if(value instanceof XIDListValue) {
			XIDListValue lv = (XIDListValue)value;
			if(!lv.isEmpty()) {
				return lv.get(0);
			} else {
				return XX.createUniqueID();
			}
		} else {
			XID id = generateXid(asString(value));
			if(id == null) {
				return XX.createUniqueID();
			}
			return id;
		}
	}
	
	private static final String nameStartChar = "A-Z_a-z\\xC0-\\xD6\\xD8-\\xF6"
	        + "\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D"
	        + "\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF" + "\\uFDF0-\\uFFFD";
	private static final String nameChar = nameStartChar
	        + "\\-\\.0-9\\xB7\\u0300-\u036F\\u203F-\\u2040";
	private static final String startClass = "[" + nameStartChar + "]";
	
	public static XID generateXid(String string) {
		
		String cleaned = string.replaceAll("[^" + nameChar + "]+", "");
		
		if(cleaned.length() == 0) {
			return null;
		}
		
		if(!cleaned.substring(0, 1).matches(startClass)) {
			cleaned = "_" + cleaned;
		}
		
		return XX.toId(cleaned);
	}
	
	static public Iterator<XID> asXIDList(XValue value) {
		if(value == null) {
			return new NoneIterator<XID>();
		} else if(value instanceof XIDListValue) {
			return ((XIDListValue)value).iterator();
		}
		if(value instanceof XIDSetValue) {
			return ((XIDSetValue)value).iterator();
		} else if(value instanceof XListValue<?>) {
			Iterator<XID> it = new AbstractTransformingIterator<String,XID>(
			        transform((XListValue<?>)value)) {
				@Override
				public XID transform(String in) {
					return generateXid(in);
				}
			};
			return new AbstractFilteringIterator<XID>(it) {
				@Override
				protected boolean matchesFilter(XID entry) {
					return entry != null;
				}
			};
		} else {
			XID xid = asXID(value);
			if(xid == null) {
				return new NoneIterator<XID>();
			}
			return new SingleValueIterator<XID>(xid);
		}
	}
	
	static public double asDouble(XValue value) {
		
		if(value == null) {
			return 0.0;
		}
		
		if(value instanceof XDoubleValue) {
			return ((XDoubleValue)value).contents();
		}
		if(value instanceof XIntegerValue) {
			return ((XIntegerValue)value).contents();
		}
		if(value instanceof XLongValue) {
			return ((XLongValue)value).contents();
		}
		if(value instanceof XListValue<?>) {
			Object o = listGetFirst((XListValue<?>)value);
			if(o instanceof Double) {
				return (Double)o;
			}
			if(o instanceof Integer) {
				return (Integer)o;
			}
			if(o instanceof Long) {
				return (Long)o;
			}
			return generateDouble(o.toString());
		} else {
			return generateDouble(value.toString());
		}
		
	}
	
	static public double generateDouble(Object object) {
		if(object instanceof Number) {
			return ((Number)object).doubleValue();
		}
		String string = object.toString();
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
	
	static public <E> Iterator<Double> transformToDoubles(XListValue<E> value) {
		return new AbstractTransformingIterator<E,Double>(value.iterator()) {
			@Override
			public Double transform(E in) {
				return generateDouble(in);
			}
		};
	}
	
	static public <E> Iterator<Integer> transformToIntegers(XListValue<E> value) {
		return new AbstractTransformingIterator<E,Integer>(value.iterator()) {
			@Override
			public Integer transform(E in) {
				return (int)generateLong(in);
			}
		};
	}
	
	static public <E> Iterator<Long> transformToLongs(XListValue<E> value) {
		return new AbstractTransformingIterator<E,Long>(value.iterator()) {
			@Override
			public Long transform(E in) {
				return generateLong(in);
			}
		};
	}
	
	static public Iterator<Double> asDoubleList(XValue value) {
		if(value == null) {
			return new NoneIterator<Double>();
		} else if(value instanceof XDoubleListValue) {
			return ((XDoubleListValue)value).iterator();
		} else if(value instanceof XListValue<?>) {
			return transformToDoubles((XListValue<?>)value);
		} else {
			return new SingleValueIterator<Double>(asDouble(value));
		}
	}
	
	static public Iterator<Long> asLongList(XValue value) {
		if(value == null) {
			return new NoneIterator<Long>();
		} else if(value instanceof XLongListValue) {
			return ((XLongListValue)value).iterator();
		} else if(value instanceof XListValue<?>) {
			return transformToLongs((XListValue<?>)value);
		} else {
			return new SingleValueIterator<Long>(asLong(value));
		}
	}
	
	static public Iterator<Integer> asIntegerList(XValue value) {
		if(value == null) {
			return new NoneIterator<Integer>();
		} else if(value instanceof XIntegerListValue) {
			return ((XIntegerListValue)value).iterator();
		} else if(value instanceof XListValue<?>) {
			return transformToIntegers((XListValue<?>)value);
		} else {
			return new SingleValueIterator<Integer>(asInteger(value));
		}
	}
	
	static public boolean asBoolean(XValue value) {
		if(value == null) {
			return false;
		}
		if(value instanceof XBooleanValue) {
			return ((XBooleanValue)value).contents();
		} else if(value instanceof XBooleanListValue) {
			XBooleanListValue lv = (XBooleanListValue)value;
			if(!lv.isEmpty()) {
				return lv.get(0);
			} else {
				return false;
			}
		} else {
			return generateBoolean(asString(value));
		}
	}
	
	public static boolean generateBoolean(String s) {
		try {
			return Double.parseDouble(s) != 0;
		} catch(NumberFormatException nfe) {
			return Boolean.parseBoolean(s);
		}
	}
	
	static public Iterator<Boolean> asBooleanList(XValue value) {
		if(value == null) {
			return new NoneIterator<Boolean>();
		} else if(value instanceof XBooleanListValue) {
			return ((XBooleanListValue)value).iterator();
		} else if(value instanceof XListValue<?>) {
			return transformToBoolean((XListValue<?>)value);
		} else {
			return new SingleValueIterator<Boolean>(asBoolean(value));
		}
	}
	
	static private <E> Iterator<Boolean> transformToBoolean(XListValue<E> value) {
		return new AbstractTransformingIterator<E,Boolean>(value.iterator()) {
			@Override
			public Boolean transform(E in) {
				return generateBoolean(in.toString());
			}
		};
	}
	
	static public long asLong(XValue value) {
		
		if(value == null) {
			return 0L;
		}
		
		if(value instanceof XDoubleValue) {
			return (long)((XDoubleValue)value).contents();
		}
		if(value instanceof XIntegerValue) {
			return ((XIntegerValue)value).contents();
		}
		if(value instanceof XLongValue) {
			return ((XLongValue)value).contents();
		}
		if(value instanceof XListValue<?>) {
			Object o = listGetFirst((XListValue<?>)value);
			if(o instanceof Double) {
				return ((Double)o).longValue();
			}
			if(o instanceof Integer) {
				return (Integer)o;
			}
			if(o instanceof Long) {
				return (Long)o;
			}
			return generateLong(o.toString());
		} else {
			return generateLong(value.toString());
		}
		
	}
	
	static public long generateLong(Object object) {
		if(object instanceof Number) {
			return ((Number)object).longValue();
		}
		String string = object.toString();
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
	
	static public int asInteger(XValue value) {
		return (int)asLong(value);
	}
	
	static public byte[] asByteList(XValue value) {
		if(value == null) {
			return new byte[0];
		}
		
		if(value instanceof XByteListValue) {
			return ((XByteListValue)value).contents();
		} else {
			char[] chars = value.toString().toCharArray();
			byte[] bytes = new byte[chars.length];
			for(int i = 0; i < chars.length; i++) {
				bytes[i] = (byte)chars[i];
			}
			return bytes;
		}
		
	}
	
}
