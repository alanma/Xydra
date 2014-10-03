package org.xydra.xgae.util;

import java.util.Collection;
import java.util.Map;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.store.impl.utils.DebugFormatter;
import org.xydra.xgae.datastore.api.SKey;

/**
 * @author xamde
 * 
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
// FIXME rename
public class XGaeDebugHelper {

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

	/** More readable logs on AppEngine */
	private static final String PREFIX = "\n";

	private static String timing(String s, Timing timing) {
		switch (timing) {
		case Now:
			return s;
		case Finished:
			return "..." + s;
		case Started:
			return s + "...";
		}
		throw new IllegalStateException();
	}

	public static String dataGet(String dataSourceName, Collection<?> keys, Map<?, ?> result,
			Timing timing) {
		return PREFIX
				+ timing((result.isEmpty() ? GET_BATCH_EMPTY : GET_BATCH) + " " + dataSourceName
						+ formatKey(keys) + " = " + DebugFormatter.format(result.values()), timing);
	}

	public static String dataGet(String dataSourceName, String key, Object value, Timing timing) {
		return PREFIX
				+ timing((value == null ? GET_NULL : GET_VALUE) + " " + dataSourceName
						+ formatKey(key) + " = " + DebugFormatter.format(value), timing);
	}

	public static String dataPut(String dataSourceName, String key, Object value, Timing timing) {
		return PREFIX
				+ timing((value == null ? PUT_NULL : PUT_VALUE) + " " + dataSourceName
						+ formatKey(key) + " -> " + DebugFormatter.format(value), timing);
	}

	public static String dataPutIfNull(String dataSourceName, Object key, Object value,
			Timing timing) {
		return PREFIX
				+ timing((value == null ? "-USELESS-" : ">ifWasNull>") + " " + dataSourceName
						+ formatKey(key) + " -> " + DebugFormatter.format(value), timing);
	}

	public static String dataPutIfUntouched(String dataSourceName, Object key, Object oldValue,
			Object newValue, Timing timing) {
		return PREFIX
				+ timing((newValue == null ? "X-(untouched?)->" : ">>(untouched)>") + " "
						+ dataSourceName + formatKey(key) + " ? " + DebugFormatter.format(oldValue)
						+ " -> " + DebugFormatter.format(newValue), timing);
	}

	public static String dataPut(String dataSourceName,
			Map<? extends Object, ? extends Object> map, Timing timing) {
		return PREFIX
				+ timing((map.isEmpty() ? PUT_BATCH_EMPTY : PUT_BATCH) + " " + dataSourceName
						+ formatKey(map.keySet()) + " -> " + DebugFormatter.format(map.values()),
						timing);
	}

	private static final String formatKey(Object key) {
		return "{'" + DebugFormatter.format(key) + "'}";
	}

	public static String init(String dataSourceName) {
		return "INIT " + dataSourceName;
	}

	public static String clear(String dataSourceName) {
		return "CLEAR " + dataSourceName;
	}

	public static String toString(SKey key) {
		return key.getKind() + "|" + key.getName();
	}

}
