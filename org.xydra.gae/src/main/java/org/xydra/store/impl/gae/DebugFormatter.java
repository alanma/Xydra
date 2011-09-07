package org.xydra.store.impl.gae;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.store.IMemCache.IdentifiableValue;
import org.xydra.store.impl.gae.changes.GaeChange;
import org.xydra.store.impl.gae.changes.KeyStructure;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;


public class DebugFormatter {
	
	private static final String PUT_NULL = "| X-> |";
	private static final String PUT_VALUE = "| >>> |";
	private static final String GET_NULL = "| <-X |";
	private static final String GET_VALUE = "| <<< |";
	private static final String GET_BATCH = "| <<<<< |";
	private static final String GET_BATCH_EMPTY = "| <---X |";
	private static final String PUT_BATCH = "| >>>>> |";
	private static final String PUT_BATCH_EMPTY = "| X---> |";
	
	private static final String LINE_END = "  <br/>\n";
	private static final int MAX_VALUE_STR_LEN = 40;
	
	public static String dataGet(String dataSourceName, Collection<?> keys, Map<?,?> result) {
		return (result.isEmpty() ? GET_BATCH_EMPTY : GET_BATCH) + " " + dataSourceName + "{ "
		        + format(keys) + " } = " + format(result.values());
	}
	
	public static String dataGet(String dataSourceName, String key, Object value) {
		return (value == null ? GET_NULL : GET_VALUE) + " " + dataSourceName + "{ " + key + " } = "
		        + format(value);
	}
	
	public static String format(Object value) {
		if(value == null) {
			return "-null-";
		} else if(value instanceof String) {
			return (String)value;
		} else if(value instanceof XID) {
			return "'" + value.toString() + "'";
		} else if(value instanceof IdentifiableValue) {
			Object o = ((IdentifiableValue)value).getValue();
			return format(o);
		} else if(value instanceof XAddress) {
			return "'" + value.toString() + "'";
		} else if(value instanceof Collection) {
			StringBuffer buf = new StringBuffer("{");
			for(Object o : ((Collection<?>)value)) {
				buf.append(format(o) + "; ");
			}
			buf.append("}");
			return buf.toString();
		} else if(value instanceof Entity) {
			Entity e = (Entity)value;
			if(e.equals(GaeUtils.NULL_ENTITY)) {
				return "NullEntity";
			}
			StringBuffer buf = new StringBuffer();
			buf.append("key:" + e.getKey() + " ");
			for(Entry<String,Object> a : e.getProperties().entrySet()) {
				buf.append(a.getKey() + ": " + formatString(a.getValue().toString()) + "; ");
			}
			return "Entity={" + buf.toString() + " }";
		} else if(value instanceof GaeChange) {
			GaeChange c = (GaeChange)value;
			return "GaeChange {" + formatString(c.toString()) + "}";
		} else if(value instanceof Long) {
			return "{" + value + "}";
		} else if(value instanceof Key) {
			return KeyStructure.toString((Key)value);
		} else {
			String s = value.getClass().getCanonicalName();
			String v = formatString(value.toString());
			if(v.length() > 10) {
				return s + " = {" + LINE_END + v + "}";
			} else {
				return s + " = {" + v + "}";
			}
		}
	}
	
	private static String formatString(String s) {
		if(s.length() <= MAX_VALUE_STR_LEN) {
			return s;
		} else
			return s.substring(0, Math.min(MAX_VALUE_STR_LEN, s.length())) + " ...[truncated]";
		
	}
	
	public static String dataPut(String dataSourceName, String key, Object value) {
		return (value == null ? PUT_NULL : PUT_VALUE) + " " + dataSourceName + "{ " + key
		        + " } -> " + format(value);
	}
	
	public static String dataPutIfNull(String dataSourceName, Object key, Object value) {
		return (value == null ? "-USELESS-" : ">ifWasNull>") + " " + dataSourceName + "{ " + key
		        + " } -> " + format(value);
	}
	
	public static String dataPutIfUntouched(String dataSourceName, Object key, Object oldValue,
	        Object newValue) {
		return (newValue == null ? "X-(untouched?)->" : ">>(untouched)>") + " " + dataSourceName
		        + "{ " + key + " } ? " + format(oldValue) + " -> " + format(newValue);
	}
	
	public static String dataPut(String dataSourceName, Map<? extends Object,? extends Object> map) {
		return (map.isEmpty() ? PUT_BATCH_EMPTY : PUT_BATCH) + " " + dataSourceName + "{ "
		        + format(map.keySet()) + " } -> " + format(map.values());
	}
	
	public static String init(String dataSourceName) {
		return "INIT " + dataSourceName;
	}
	
}
