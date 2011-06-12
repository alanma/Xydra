package org.xydra.json;

/*
 * Copyright (c) 2002 JSON.org
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * The Software shall be used for Good, not Evil.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.xydra.base.minio.MiniIOException;
import org.xydra.base.minio.MiniWriter;
import org.xydra.core.serialize.json.JSONException;


/**
 * A JSONObject is an unordered collection of name/value pairs. Its external
 * form is a string wrapped in curly braces with colons between the names and
 * values, and commas between the values and names. The internal form is an
 * object having <code>get</code> and <code>opt</code> methods for accessing the
 * values by name, and <code>put</code> methods for adding or replacing values
 * by name. The values can be any of these types: <code>Boolean</code>,
 * <code>JSONArray</code>, <code>JSONObject</code>, <code>Number</code>,
 * <code>String</code>, or the <code>JSONObject.NULL</code> object. A JSONObject
 * constructor can be used to convert an external form JSON text into an
 * internal form whose values can be retrieved with the <code>get</code> and
 * <code>opt</code> methods, or to convert values into a JSON text using the
 * <code>put</code> and <code>toString</code> methods. A <code>get</code> method
 * returns a value if one can be found, and throws an exception if one cannot be
 * found. An <code>opt</code> method returns a default value instead of throwing
 * an exception, and so is useful for obtaining optional values.
 * <p>
 * The generic <code>get()</code> and <code>opt()</code> methods return an
 * object, which you can cast or query for type. There are also typed
 * <code>get</code> and <code>opt</code> methods that do type checking and type
 * coercion for you.
 * <p>
 * The <code>put</code> methods adds values to an object. For example,
 * 
 * <pre>
 * myString = new JSONObject().put(&quot;JSON&quot;, &quot;Hello, World!&quot;).toString();
 * </pre>
 * 
 * produces the string <code>{"JSON": "Hello, World"}</code>.
 * <p>
 * The texts produced by the <code>toString</code> methods strictly conform to
 * the JSON syntax rules. The constructors are more forgiving in the texts they
 * will accept:
 * <ul>
 * <li>An extra <code>,</code>&nbsp;<small>(comma)</small> may appear just
 * before the closing brace.</li>
 * <li>Strings may be quoted with <code>'</code>&nbsp;<small>(single
 * quote)</small>.</li>
 * <li>Strings do not need to be quoted at all if they do not begin with a quote
 * or single quote, and if they do not contain leading or trailing spaces, and
 * if they do not contain any of these characters:
 * <code>{ } [ ] / \ : , = ; #</code> and if they do not look like numbers and
 * if they are not the reserved words <code>true</code>, <code>false</code>, or
 * <code>null</code>.</li>
 * <li>Keys can be followed by <code>=</code> or <code>=></code> as well as by
 * <code>:</code>.</li>
 * <li>Values can be followed by <code>;</code> <small>(semicolon)</small> as
 * well as by <code>,</code> <small>(comma)</small>.</li>
 * <li>Numbers may have the <code>0-</code> <small>(octal)</small> or
 * <code>0x-</code> <small>(hex)</small> prefix.</li>
 * </ul>
 * 
 * @author JSON.org
 * @version 2009-03-06
 */
public class JSONObject {
	
	/**
	 * The map where the JSONObject's properties are kept.
	 */
	private Map<String,Object> map;
	
	/**
	 * Construct an empty JSONObject.
	 */
	public JSONObject() {
		this.map = new HashMap<String,Object>();
	}
	
	/**
	 * Construct a JSONObject from a Map.
	 * 
	 * @param map A map object that can be used to initialize the contents of
	 *            the JSONObject.
	 */
	public JSONObject(Map<String,Object> map) {
		this.map = (map == null) ? new HashMap<String,Object>() : map;
	}
	
	/**
	 * Accumulate values under a key. It is similar to the put method except
	 * that if there is already an object stored under the key then a JSONArray
	 * is stored under the key to hold all of the accumulated values. If there
	 * is already a JSONArray, then the new value is appended to it. In
	 * contrast, the put method replaces the previous value.
	 * 
	 * @param key A key string.
	 * @param value An object to be accumulated under the key.
	 * @return this.
	 * @throws JSONException If the value is an invalid number or if the key is
	 *             null.
	 */
	public JSONObject accumulate(String key, Object value) throws JSONException {
		testValidity(value);
		Object o = opt(key);
		if(o == null) {
			put(key, value instanceof JSONArray ? new JSONArray().put(value) : value);
		} else if(o instanceof JSONArray) {
			((JSONArray)o).put(value);
		} else {
			put(key, new JSONArray().put(o).put(value));
		}
		return this;
	}
	
	/**
	 * Get an optional value associated with a key.
	 * 
	 * @param key A key string.
	 * @return An object which is the value, or null if there is no value.
	 */
	public Object opt(String key) {
		return key == null ? null : this.map.get(key);
	}
	
	/**
	 * Append values to the array under a key. If the key does not exist in the
	 * JSONObject, then the key is put in the JSONObject with its value being a
	 * JSONArray containing the value parameter. If the key was already
	 * associated with a JSONArray, then the value parameter is appended to it.
	 * 
	 * @param key A key string.
	 * @param value An object to be accumulated under the key.
	 * @return this.
	 * @throws JSONException If the key is null or if the current value
	 *             associated with the key is not a JSONArray.
	 */
	public JSONObject append(String key, Object value) throws JSONException {
		testValidity(value);
		Object o = opt(key);
		if(o == null) {
			put(key, new JSONArray().put(value));
		} else if(o instanceof JSONArray) {
			put(key, ((JSONArray)o).put(value));
		} else {
			throw new JSONException("JSONObject[" + key + "] is not a JSONArray.");
		}
		return this;
	}
	
	/**
	 * Get the value object associated with a key.
	 * 
	 * @param key A key string.
	 * @return The object associated with the key.
	 * @throws JSONException if the key is not found.
	 */
	public Object get(String key) throws JSONException {
		Object o = opt(key);
		if(o == null) {
			throw new JSONException("JSONObject[" + JSONWriter.quote(key) + "] not found.");
		}
		return o;
	}
	
	/**
	 * Get an array of field names from a JSONObject.
	 * 
	 * @return An array of field names, or null if there are no names.
	 */
	public static String[] getNames(JSONObject jo) {
		int length = jo.length();
		if(length == 0) {
			return null;
		}
		Iterator<String> i = jo.keys();
		String[] names = new String[length];
		int j = 0;
		while(i.hasNext()) {
			names[j] = i.next();
			j += 1;
		}
		return names;
	}
	
	/**
	 * Determine if the JSONObject contains a specific key.
	 * 
	 * @param key A key string.
	 * @return true if the key exists in the JSONObject.
	 */
	public boolean has(String key) {
		return this.map.containsKey(key);
	}
	
	/**
	 * Determine if the value associated with the key is null or if there is no
	 * value.
	 * 
	 * @param key A key string.
	 * @return true if there is no value associated with the key or if the value
	 *         is the JSONObject.NULL object.
	 */
	public boolean isNull(String key) {
		return opt(key) == null;
	}
	
	/**
	 * Get an enumeration of the keys of the JSONObject.
	 * 
	 * @return An iterator of the keys.
	 */
	public Iterator<String> keys() {
		return this.map.keySet().iterator();
	}
	
	/**
	 * Get the number of keys stored in the JSONObject.
	 * 
	 * @return The number of keys in the JSONObject.
	 */
	public int length() {
		return this.map.size();
	}
	
	/**
	 * Produce a JSONArray containing the names of the elements of this
	 * JSONObject.
	 * 
	 * @return A JSONArray containing the key strings, or null if the JSONObject
	 *         is empty.
	 */
	public JSONArray names() {
		JSONArray ja = new JSONArray();
		Iterator<String> keys = keys();
		while(keys.hasNext()) {
			ja.put(keys.next());
		}
		return ja.length() == 0 ? null : ja;
	}
	
	/**
	 * Put a key/value pair in the JSONObject. If the value is null, then the
	 * key will be removed from the JSONObject if it is present.
	 * 
	 * @param key A key string.
	 * @param value An object which is the value. It should be of one of these
	 *            types: Boolean, Double, Integer, JSONArray, JSONObject, Long,
	 *            String, or the JSONObject.NULL object.
	 * @return this.
	 * @throws JSONException If the value is non-finite number or if the key is
	 *             null.
	 */
	public JSONObject put(String key, Object value) throws JSONException {
		if(key == null) {
			throw new JSONException("Null key.");
		}
		if(value != null) {
			testValidity(value);
			this.map.put(key, value);
		} else {
			remove(key);
		}
		return this;
	}
	
	/**
	 * Throw an exception if the object is an NaN or infinite number.
	 * 
	 * @param o The object to test.
	 * @throws JSONException If o is a non-finite number.
	 */
	static public void testValidity(Object o) throws JSONException {
		if(o != null) {
			if(o instanceof Double) {
				if(((Double)o).isInfinite() || ((Double)o).isNaN()) {
					throw new JSONException("JSON does not allow non-finite numbers.");
				}
			} else if(o instanceof Float) {
				if(((Float)o).isInfinite() || ((Float)o).isNaN()) {
					throw new JSONException("JSON does not allow non-finite numbers.");
				}
			}
		}
	}
	
	/**
	 * Put a key/value pair in the JSONObject, but only if the key and the value
	 * are both non-null, and only if there is not already a member with that
	 * name.
	 * 
	 * @param key ..
	 * @param value ..
	 * @return his.
	 * @throws JSONException if the key is a duplicate
	 */
	public JSONObject putOnce(String key, Object value) throws JSONException {
		if(key != null && value != null) {
			if(opt(key) != null) {
				throw new JSONException("Duplicate key \"" + key + "\"");
			}
			put(key, value);
		}
		return this;
	}
	
	/**
	 * Remove a name and its value, if present.
	 * 
	 * @param key The name to be removed.
	 * @return The value that was associated with the name, or null if there was
	 *         no value.
	 */
	public Object remove(String key) {
		return this.map.remove(key);
	}
	
	/**
	 * Get an enumeration of the keys of the JSONObject. The keys will be sorted
	 * alphabetically.
	 * 
	 * @return An iterator of the keys.
	 */
	public Iterator<String> sortedKeys() {
		return new TreeSet<String>(this.map.keySet()).iterator();
	}
	
	/**
	 * Try to convert a string into a number, boolean, or null. If the string
	 * can't be converted, return the string.
	 * 
	 * @param s A String.
	 * @return A simple JSON value.
	 */
	static public Object stringToValue(String s) {
		if(s.equals("")) {
			return s;
		}
		if(s.equalsIgnoreCase("true")) {
			return Boolean.TRUE;
		}
		if(s.equalsIgnoreCase("false")) {
			return Boolean.FALSE;
		}
		if(s.equalsIgnoreCase("null")) {
			return null;
		}
		
		/*
		 * If it might be a number, try converting it. We support the 0- and 0x-
		 * conventions. If a number cannot be produced, then the value will just
		 * be a string. Note that the 0-, 0x-, plus, and implied string
		 * conventions are non-standard. A JSON parser is free to accept
		 * non-JSON forms as long as it accepts all correct JSON forms.
		 */

		char b = s.charAt(0);
		if((b >= '0' && b <= '9') || b == '.' || b == '-' || b == '+') {
			if(b == '0') {
				if(s.length() > 2 && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
					try {
						return new Integer(Integer.parseInt(s.substring(2), 16));
					} catch(Exception e) {
						/* Ignore the error */
					}
				} else {
					try {
						return new Integer(Integer.parseInt(s, 8));
					} catch(Exception e) {
						/* Ignore the error */
					}
				}
			}
			try {
				if(s.indexOf('.') > -1 || s.indexOf('e') > -1 || s.indexOf('E') > -1) {
					return Double.valueOf(s);
				} else {
					Long myLong = new Long(s);
					if(myLong.longValue() == myLong.intValue()) {
						return new Integer(myLong.intValue());
					} else {
						return myLong;
					}
				}
			} catch(Exception f) {
				/* Ignore the error */
			}
		}
		return s;
	}
	
	/**
	 * Produce a JSONArray containing the values of the members of this
	 * JSONObject.
	 * 
	 * @param names A JSONArray containing a list of key strings. This
	 *            determines the sequence of the values in the result.
	 * @return A JSONArray of values.
	 * @throws JSONException If any of the values are non-finite numbers.
	 */
	public JSONArray toJSONArray(JSONArray names) throws JSONException {
		if(names == null || names.length() == 0) {
			return null;
		}
		JSONArray ja = new JSONArray();
		for(int i = 0; i < names.length(); i += 1) {
			ja.put(this.opt(names.getString(i)));
		}
		return ja;
	}
	
	/**
	 * Make a JSON text of this JSONObject. For compactness, no whitespace is
	 * added. If this would not result in a syntactically correct JSON text,
	 * then null will be returned instead.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @return a printable, displayable, portable, transmittable representation
	 *         of the object, beginning with <code>{</code>&nbsp;<small>(left
	 *         brace)</small> and ending with <code>}</code>&nbsp;<small>(right
	 *         brace)</small>.
	 */
	@Override
	public String toString() {
		try {
			Iterator<String> keys = keys();
			StringBuffer sb = new StringBuffer("{");
			
			while(keys.hasNext()) {
				if(sb.length() > 1) {
					sb.append(',');
				}
				Object o = keys.next();
				sb.append(JSONWriter.quote(o.toString()));
				sb.append(':');
				sb.append(JSONWriter.valueToString(this.map.get(o)));
			}
			sb.append('}');
			return sb.toString();
		} catch(Exception e) {
			return null;
		}
	}
	
	/**
	 * Make a prettyprinted JSON text of this JSONObject.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @param indentFactor The number of spaces to add to each level of
	 *            indentation.
	 * @return a printable, displayable, portable, transmittable representation
	 *         of the object, beginning with <code>{</code>&nbsp;<small>(left
	 *         brace)</small> and ending with <code>}</code>&nbsp;<small>(right
	 *         brace)</small>.
	 * @throws JSONException If the object contains an invalid number.
	 */
	public String toString(int indentFactor) throws JSONException {
		return toString(indentFactor, 0);
	}
	
	/**
	 * Make a prettyprinted JSON text of this JSONObject.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @param indentFactor The number of spaces to add to each level of
	 *            indentation.
	 * @param indent The indentation of the top level.
	 * @return a printable, displayable, transmittable representation of the
	 *         object, beginning with <code>{</code>&nbsp;<small>(left
	 *         brace)</small> and ending with <code>}</code>&nbsp;<small>(right
	 *         brace)</small>.
	 * @throws JSONException If the object contains an invalid number.
	 */
	public String toString(int indentFactor, int indent) throws JSONException {
		int j;
		int n = length();
		if(n == 0) {
			return "{}";
		}
		Iterator<String> keys = sortedKeys();
		StringBuffer sb = new StringBuffer("{");
		int newindent = indent + indentFactor;
		Object o;
		if(n == 1) {
			o = keys.next();
			sb.append(JSONWriter.quote(o.toString()));
			sb.append(": ");
			sb.append(JSONWriter.valueToString(this.map.get(o), indentFactor, indent));
		} else {
			while(keys.hasNext()) {
				o = keys.next();
				if(sb.length() > 1) {
					sb.append(",\n");
				} else {
					sb.append('\n');
				}
				for(j = 0; j < newindent; j += 1) {
					sb.append(' ');
				}
				sb.append(JSONWriter.quote(o.toString()));
				sb.append(": ");
				sb.append(JSONWriter.valueToString(this.map.get(o), indentFactor, newindent));
			}
			if(sb.length() > 1) {
				sb.append('\n');
				for(j = 0; j < indent; j += 1) {
					sb.append(' ');
				}
			}
		}
		sb.append('}');
		return sb.toString();
	}
	
	/**
	 * Write the contents of the JSONObject as JSON text to a writer. For
	 * compactness, no whitespace is added.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @return The writer.
	 * @throws JSONException If the object contains an invalid number.
	 */
	public MiniWriter write(MiniWriter writer) throws JSONException {
		try {
			boolean b = false;
			Iterator<String> keys = keys();
			writer.write('{');
			
			while(keys.hasNext()) {
				if(b) {
					writer.write(',');
				}
				Object k = keys.next();
				writer.write(JSONWriter.quote(k.toString()));
				writer.write(':');
				Object v = this.map.get(k);
				if(v instanceof JSONObject) {
					((JSONObject)v).write(writer);
				} else if(v instanceof JSONArray) {
					((JSONArray)v).write(writer);
				} else {
					writer.write(JSONWriter.valueToString(v));
				}
				b = true;
			}
			writer.write('}');
			return writer;
		} catch(MiniIOException e) {
			throw new JSONException(e);
		}
	}
}
