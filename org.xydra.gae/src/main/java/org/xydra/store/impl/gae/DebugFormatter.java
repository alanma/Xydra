package org.xydra.store.impl.gae;

import java.util.Collection;
import java.util.Map;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.XAddress;
import org.xydra.base.XID;
import org.xydra.base.change.XCommand;
import org.xydra.sharedutils.ReflectionUtils;
import org.xydra.store.IMemCache.IdentifiableValue;


/**
 * @author xamde
 * 
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
public class DebugFormatter {
	
	public static enum Timing {
		Now, Started, Finished
	}
	
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
	/** More readable logs on AppEngine */
	private static final String PREFIX = "\n";
	
	private static String timing(String s, Timing timing) {
		switch(timing) {
		case Now:
			return s;
		case Finished:
			return "..." + s;
		case Started:
			return s + "...";
		}
		throw new IllegalStateException();
	}
	
	public static String dataGet(String dataSourceName, Collection<?> keys, Map<?,?> result,
	        Timing timing) {
		return PREFIX
		        + timing((result.isEmpty() ? GET_BATCH_EMPTY : GET_BATCH) + " " + dataSourceName
		                + formatKey(keys) + " = " + format(result.values()), timing);
	}
	
	public static String dataGet(String dataSourceName, String key, Object value, Timing timing) {
		return PREFIX
		        + timing((value == null ? GET_NULL : GET_VALUE) + " " + dataSourceName
		                + formatKey(key) + " = " + format(value), timing);
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
		} else if(value instanceof Collection<?>) {
			StringBuffer buf = new StringBuffer("{");
			for(Object o : ((Collection<?>)value)) {
				buf.append(format(o) + "; ");
			}
			buf.append("}");
			return buf.toString();
		} else if(value instanceof XCommand) {
			XCommand c = (XCommand)value;
			return "Command {" + formatString(c.toString(), 140) + "}";
		} else if(value instanceof Long) {
			return "{" + value + "}";
		} else if(GaeDebugFormatter.canHandle(value)) {
			return GaeDebugFormatter.toString(value);
		} else {
			String s = ReflectionUtils.getCanonicalName(value.getClass());
			String v = formatString(value.toString());
			if(v.length() > 10) {
				return s + " = {" + LINE_END + v + "}";
			} else {
				return s + " = {" + v + "}";
			}
		}
	}
	
	static String formatString(String s) {
		return formatString(s, MAX_VALUE_STR_LEN);
	}
	
	static String formatString(String s, int maxLen) {
		if(s.length() <= maxLen) {
			return s;
		} else
			return s.substring(0, Math.min(maxLen, s.length())) + " ...[truncated]";
		
	}
	
	public static String dataPut(String dataSourceName, String key, Object value, Timing timing) {
		return PREFIX
		        + timing((value == null ? PUT_NULL : PUT_VALUE) + " " + dataSourceName
		                + formatKey(key) + " -> " + format(value), timing);
	}
	
	public static String dataPutIfNull(String dataSourceName, Object key, Object value,
	        Timing timing) {
		return PREFIX
		        + timing((value == null ? "-USELESS-" : ">ifWasNull>") + " " + dataSourceName
		                + formatKey(key) + " -> " + format(value), timing);
	}
	
	public static String dataPutIfUntouched(String dataSourceName, Object key, Object oldValue,
	        Object newValue, Timing timing) {
		return PREFIX
		        + timing((newValue == null ? "X-(untouched?)->" : ">>(untouched)>") + " "
		                + dataSourceName + formatKey(key) + " ? " + format(oldValue) + " -> "
		                + format(newValue), timing);
	}
	
	public static String dataPut(String dataSourceName, Map<? extends Object,? extends Object> map,
	        Timing timing) {
		return PREFIX
		        + timing((map.isEmpty() ? PUT_BATCH_EMPTY : PUT_BATCH) + " " + dataSourceName
		                + formatKey(map.keySet()) + " -> " + format(map.values()), timing);
	}
	
	private static final String formatKey(Object key) {
		return "{'" + format(key) + "'}";
	}
	
	public static String init(String dataSourceName) {
		return "INIT " + dataSourceName;
	}
	
	public static String clear(String dataSourceName) {
		return "CLEAR " + dataSourceName;
	}
	
}
