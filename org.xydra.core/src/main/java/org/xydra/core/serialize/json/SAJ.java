package org.xydra.core.serialize.json;

/**
 * SAX-like API for JSON.
 * 
 * @author voelkel
 * 
 */
public interface SAJ {
	
	void onDouble(double d) throws JSONException;
	
	void onInteger(int i) throws JSONException;
	
	void onBoolean(boolean b) throws JSONException;
	
	void onString(String s) throws JSONException;
	
	void arrayStart() throws JSONException;
	
	void arrayEnd() throws JSONException;
	
	void objectStart() throws JSONException;
	
	void objectEnd() throws JSONException;
	
	void onNull() throws JSONException;
	
	void onLong(long l) throws JSONException;
	
	/**
	 * Only legal within 'objectStart' and 'objectEnd'
	 * 
	 * @param key
	 */
	void onKey(String key) throws JSONException;
}
