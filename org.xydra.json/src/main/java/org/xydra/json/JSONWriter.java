package org.xydra.json;

import java.util.Collection;
import java.util.Map;

import org.xydra.base.minio.MiniIOException;
import org.xydra.base.minio.MiniWriter;
import org.xydra.core.serialize.json.JSONException;


/*
 * Copyright (c) 2006 JSON.org
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

/**
 * TODO this class duplicates some functionality of SAJ. Later, this is going to
 * be replaced with a SAJ-handler.
 * 
 * 
 * JSONWriter provides a quick and convenient way of producing JSON text. The
 * texts produced strictly conform to JSON syntax rules. No whitespace is added,
 * so the results are ready for transmission or storage. Each instance of
 * JSONWriter can produce one JSON text.
 * <p>
 * A JSONWriter instance provides a <code>value</code> method for appending
 * values to the text, and a <code>key</code> method for adding keys before
 * values in objects. There are <code>array</code> and <code>endArray</code>
 * methods that make and bound array values, and <code>object</code> and
 * <code>endObject</code> methods which make and bound object values. All of
 * these methods return the JSONWriter instance, permitting a cascade style. For
 * example,
 * 
 * <pre>
 * new JSONWriter(myWriter).object().key(&quot;JSON&quot;).value(&quot;Hello, World!&quot;).endObject();
 * </pre>
 * 
 * which writes
 * 
 * <pre>
 * {"JSON":"Hello, World!"}
 * </pre>
 * <p>
 * The first method called must be <code>array</code> or <code>object</code>.
 * There are no methods for adding commas or colons. JSONWriter adds them for
 * you. Objects and arrays can be nested up to 20 levels deep.
 * <p>
 * This can sometimes be easier than using a JSONObject to build a string.
 * 
 * @author JSON.org
 * @version 2008-09-22
 */
public class JSONWriter {
	private static final int maxdepth = 20;
	
	/**
	 * The comma flag determines if a comma should be output before the next
	 * value.
	 */
	private boolean comma;
	
	/**
	 * The current mode. Values: 'a' (array), 'd' (done), 'i' (initial), 'k'
	 * (key), 'o' (object).
	 */
	protected char mode;
	
	/**
	 * The object/array stack.
	 */
	private JSONObject stack[];
	
	/**
	 * The stack top index. A value of 0 indicates that the stack is empty.
	 */
	private int top;
	
	/**
	 * The writer that will receive the output.
	 */
	protected MiniWriter writer;
	
	/**
	 * Make a fresh JSONWriter. It can be used to build one JSON text.
	 */
	public JSONWriter(MiniWriter w) {
		this.comma = false;
		this.mode = 'i';
		this.stack = new JSONObject[maxdepth];
		this.top = 0;
		this.writer = w;
	}
	
	/**
	 * Append a value.
	 * 
	 * @param s A string value.
	 * @return this
	 * @throws JSONException If the value is out of sequence.
	 */
	private JSONWriter append(String s) throws JSONException {
		if(s == null) {
			throw new JSONException("Null pointer");
		}
		if(this.mode == 'o' || this.mode == 'a') {
			try {
				if(this.comma && this.mode == 'a') {
					this.writer.write(',');
				}
				this.writer.write(s);
			} catch(MiniIOException e) {
				throw new JSONException(e);
			}
			if(this.mode == 'o') {
				this.mode = 'k';
			}
			this.comma = true;
			return this;
		}
		throw new JSONException("Value out of sequence. Mode is " + this.mode);
	}
	
	/**
	 * Begin appending a new array. All values until the balancing
	 * <code>endArray</code> will be appended to this array. The
	 * <code>endArray</code> method must be called to mark the array's end.
	 * 
	 * @return this
	 * @throws JSONException If the nesting is too deep, or if the object is
	 *             started in the wrong place (for example as a key or after the
	 *             end of the outermost array or object).
	 */
	public JSONWriter arrayStart() throws JSONException {
		if(this.mode == 'i' || this.mode == 'o' || this.mode == 'a') {
			this.push(null);
			this.append("[");
			this.comma = false;
			return this;
		}
		throw new JSONException("Misplaced array.");
	}
	
	/**
	 * End something.
	 * 
	 * @param m Mode
	 * @param c Closing character
	 * @return this
	 * @throws JSONException If unbalanced.
	 */
	private JSONWriter end(char m, char c) throws JSONException {
		if(this.mode != m) {
			throw new JSONException(m == 'o' ? "Misplaced endObject." : "Misplaced endArray.");
		}
		this.pop(m);
		try {
			this.writer.write(c);
		} catch(MiniIOException e) {
			throw new JSONException(e);
		}
		this.comma = true;
		return this;
	}
	
	/**
	 * End an array. This method most be called to balance calls to
	 * <code>array</code>.
	 * 
	 * @return this
	 * @throws JSONException If incorrectly nested.
	 */
	public JSONWriter arrayEnd() throws JSONException {
		return this.end('a', ']');
	}
	
	/**
	 * End an object. This method most be called to balance calls to
	 * <code>object</code>.
	 * 
	 * @return this
	 * @throws JSONException If incorrectly nested.
	 */
	public JSONWriter objectEnd() throws JSONException {
		return this.end('k', '}');
	}
	
	/**
	 * Append a key. The key will be associated with the next value. In an
	 * object, every value must be preceded by a key.
	 * 
	 * @param s A key string.
	 * @return this
	 * @throws JSONException If the key is out of place. For example, keys do
	 *             not belong in arrays or if the key is null.
	 */
	public JSONWriter key(String s) throws JSONException {
		if(s == null) {
			throw new JSONException("Null key.");
		}
		if(this.mode == 'k') {
			try {
				this.stack[this.top - 1].putOnce(s, Boolean.TRUE);
				if(this.comma) {
					this.writer.write(',');
				}
				this.writer.write(quote(s));
				this.writer.write(':');
				this.comma = false;
				this.mode = 'o';
				return this;
			} catch(MiniIOException e) {
				throw new JSONException(e);
			}
		}
		throw new JSONException("Misplaced key.");
	}
	
	/**
	 * Begin appending a new object. All keys and values until the balancing
	 * <code>endObject</code> will be appended to this object. The
	 * <code>endObject</code> method must be called to mark the object's end.
	 * 
	 * @return this
	 * @throws JSONException If the nesting is too deep, or if the object is
	 *             started in the wrong place (for example as a key or after the
	 *             end of the outermost array or object).
	 */
	public JSONWriter objectStart() throws JSONException {
		if(this.mode == 'i') {
			this.mode = 'o';
		}
		if(this.mode == 'o' || this.mode == 'a') {
			this.append("{");
			this.push(new JSONObject());
			this.comma = false;
			return this;
		}
		// FIXME
		this.writer.flush();
		throw new JSONException("Misplaced object.");
		
	}
	
	/**
	 * Pop an array or object scope.
	 * 
	 * @param c The scope to close.
	 * @throws JSONException If nesting is wrong.
	 */
	private void pop(char c) throws JSONException {
		if(this.top <= 0) {
			throw new JSONException("Nesting error.");
		}
		char m = this.stack[this.top - 1] == null ? 'a' : 'k';
		if(m != c) {
			throw new JSONException("Nesting error.");
		}
		this.top -= 1;
		this.mode = this.top == 0 ? 'd' : this.stack[this.top - 1] == null ? 'a' : 'k';
	}
	
	/**
	 * Push an array or object scope.
	 * 
	 * @param c The scope to open.
	 * @throws JSONException If nesting is too deep.
	 */
	private void push(JSONObject jo) throws JSONException {
		if(this.top >= maxdepth) {
			throw new JSONException("Nesting too deep.");
		}
		this.stack[this.top] = jo;
		this.mode = jo == null ? 'a' : 'k';
		this.top += 1;
	}
	
	/**
	 * Append either the value <code>true</code> or the value <code>false</code>
	 * .
	 * 
	 * @param b A boolean.
	 * @return this
	 */
	public JSONWriter value(boolean b) throws JSONException {
		return this.append(b ? "true" : "false");
	}
	
	/**
	 * Append a double value.
	 * 
	 * @param d A double.
	 * @return this
	 * @throws JSONException If the number is not finite.
	 */
	public JSONWriter value(double d) throws JSONException {
		return this.value(new Double(d));
	}
	
	/**
	 * Append a long value.
	 * 
	 * @param l A long.
	 * @return this
	 */
	public JSONWriter value(long l) throws JSONException {
		return this.append(Long.toString(l));
	}
	
	/**
	 * Append an object value.
	 * 
	 * @param o The object to append. It can be null, or a Boolean, Number,
	 *            String, JSONObject, or JSONArray, or an object with a
	 *            toJSONString() method.
	 * @return this
	 * @throws JSONException If the value is out of sequence.
	 */
	public JSONWriter value(Object o) throws JSONException {
		return this.append(valueToString(o));
	}
	
	/**
	 * Make a JSON text of an Object value. If the object has an
	 * value.toJSONString() method, then that method will be used to produce the
	 * JSON text. The method is required to produce a strictly conforming text.
	 * If the object does not contain a toJSONString method (which is the most
	 * common case), then a text will be produced by other means. If the value
	 * is an array or Collection, then a JSONArray will be made from it and its
	 * toJSONString method will be called. If the value is a MAP, then a
	 * JSONObject will be made from it and its toJSONString method will be
	 * called. Otherwise, the value's toString method will be called, and the
	 * result will be quoted.
	 * 
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @param value The value to be serialized.
	 * @return a printable, displayable, transmittable representation of the
	 *         object, beginning with <code>{</code>&nbsp;<small>(left
	 *         brace)</small> and ending with <code>}</code>&nbsp;<small>(right
	 *         brace)</small>.
	 * @throws JSONException If the value is or contains an invalid number.
	 */
	@SuppressWarnings("unchecked")
	public static String valueToString(Object value) throws JSONException {
		if(value == null || value.equals(null)) {
			return "null";
		}
		if(value instanceof Number) {
			return numberToString((Number)value);
		}
		if(value instanceof Boolean || value instanceof JSONObject || value instanceof JSONArray) {
			return value.toString();
		}
		if(value instanceof Map<?,?>) {
			return new JSONObject((Map<String,Object>)value).toString();
		}
		if(value instanceof Collection<?>) {
			return new JSONArray((Collection<Object>)value).toString();
		}
		if(value.getClass().isArray()) {
			return new JSONArray((Object[])value).toString();
		}
		return quote(value.toString());
	}
	
	/**
	 * Make a prettyprinted JSON text of an object value.
	 * <p>
	 * Warning: This method assumes that the data structure is acyclical.
	 * 
	 * @param value The value to be serialized.
	 * @param indentFactor The number of spaces to add to each level of
	 *            indentation.
	 * @param indent The indentation of the top level.
	 * @return a printable, displayable, transmittable representation of the
	 *         object, beginning with <code>{</code>&nbsp;<small>(left
	 *         brace)</small> and ending with <code>}</code>&nbsp;<small>(right
	 *         brace)</small>.
	 * @throws JSONException If the object contains an invalid number.
	 */
	@SuppressWarnings("unchecked")
	static public String valueToString(Object value, int indentFactor, int indent)
	        throws JSONException {
		if(value == null || value.equals(null)) {
			return "null";
		}
		if(value instanceof Number) {
			return numberToString((Number)value);
		}
		if(value instanceof Boolean) {
			return value.toString();
		}
		if(value instanceof JSONObject) {
			return ((JSONObject)value).toString(indentFactor, indent);
		}
		if(value instanceof JSONArray) {
			return ((JSONArray)value).toString(indentFactor, indent);
		}
		if(value instanceof Map<?,?>) {
			return new JSONObject((Map<String,Object>)value).toString(indentFactor, indent);
		}
		if(value instanceof Collection<?>) {
			return new JSONArray((Collection<Object>)value).toString(indentFactor, indent);
		}
		if(value.getClass().isArray()) {
			return new JSONArray((Object[])value).toString(indentFactor, indent);
		}
		return quote(value.toString());
	}
	
	/**
	 * Produce a string from a Number.
	 * 
	 * @param n A Number
	 * @return A String.
	 * @throws JSONException If n is a non-finite number.
	 */
	static public String numberToString(Number n) throws JSONException {
		if(n == null) {
			throw new JSONException("Null pointer");
		}
		JSONObject.testValidity(n);
		
		// Shave off trailing zeros and decimal point, if possible.
		
		String s = n.toString();
		if(s.indexOf('.') > 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0) {
			while(s.endsWith("0")) {
				s = s.substring(0, s.length() - 1);
			}
			if(s.endsWith(".")) {
				s = s.substring(0, s.length() - 1);
			}
		}
		return s;
	}
	
	/**
	 * Produce a string in double quotes with backslash sequences in all the
	 * right places. A backslash will be inserted within </, allowing JSON text
	 * to be delivered in HTML. In JSON text, a string cannot contain a control
	 * character or an unescaped quote or backslash.
	 * 
	 * @param string A String
	 * @return A String correctly formatted for insertion in a JSON text.
	 */
	public static String quote(String string) {
		if(string == null || string.length() == 0) {
			return "\"\"";
		}
		
		char b;
		char c = 0;
		int i;
		int len = string.length();
		StringBuffer sb = new StringBuffer(len + 4);
		String t;
		
		sb.append('"');
		for(i = 0; i < len; i += 1) {
			b = c;
			c = string.charAt(i);
			switch(c) {
			case '\\':
			case '"':
				sb.append('\\');
				sb.append(c);
				break;
			case '/':
				if(b == '<') {
					sb.append('\\');
				}
				sb.append(c);
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\r':
				sb.append("\\r");
				break;
			default:
				if(c < ' ' || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) {
					t = "000" + Integer.toHexString(c);
					sb.append("\\u" + t.substring(t.length() - 4));
				} else {
					sb.append(c);
				}
			}
		}
		sb.append('"');
		return sb.toString();
	}
	
	/**
	 * Produce a string from a double. The string "null" will be returned if the
	 * number is not finite.
	 * 
	 * @param d A double.
	 * @return A String.
	 */
	static public String doubleToString(double d) {
		if(Double.isInfinite(d) || Double.isNaN(d)) {
			return "null";
		}
		
		// Shave off trailing zeros and decimal point, if possible.
		
		String s = Double.toString(d);
		if(s.indexOf('.') > 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0) {
			while(s.endsWith("0")) {
				s = s.substring(0, s.length() - 1);
			}
			if(s.endsWith(".")) {
				s = s.substring(0, s.length() - 1);
			}
		}
		return s;
	}
	
	/**
	 * Send a self-contained JSON-string through to the underlying writer.
	 * 
	 * @param jsonString to be written as-is
	 */
	public void jsonString(String jsonString) {
		this.writer.write(jsonString);
	}
	
	public void nullValue() throws JSONException {
		this.append("null");
	}
	
}
