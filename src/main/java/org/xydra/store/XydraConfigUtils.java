package org.xydra.store;

import java.util.HashMap;
import java.util.Map;


/**
 * Helper class for accessing a config map
 * 
 * @author xamde
 * 
 */
public class XydraConfigUtils {
	
	public static final String EMPTY_VALUE = "";
	
	/**
	 * @param value can be null
	 * @return a normalised config value. null, "null", "", and "false" are
	 *         represented as the empty String "".
	 */
	public static String normalizeValue(String value) {
		if(value == null || value.equals("null") || value.equals("false")) {
			return EMPTY_VALUE;
		} else
			return value;
	}
	
	/**
	 * @param current never null, the current configuration map
	 * @param update never null, the config update with empty strings or nulls
	 *            denoting removal.
	 * @return a map containing all entries of 'update' that are not present in
	 *         current or -- if present -- have a different value.
	 */
	public static Map<String,String> getChanges(Map<String,String> current,
	        Map<String,String> update) {
		
		Map<String,String> changes = new HashMap<String,String>();
		for(String key : update.keySet()) {
			String currentValue = normalizeValue(current.get(key));
			String updateValue = normalizeValue(update.get(key));
			if(currentValue.equals(updateValue)) {
				// no change
			} else {
				// change
				changes.put(key, updateValue);
			}
		}
		return changes;
	}
	
}
