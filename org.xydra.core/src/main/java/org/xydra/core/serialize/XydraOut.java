package org.xydra.core.serialize;

/**
 * A minimal interface for writing XML-based or JSON-based streams.
 * 
 * This interface is GWT-compatible.
 * 
 * This is similar to http://java.ociweb.com/mark/programming/WAX.html
 * 
 * @author voelkel
 * @author dscharrer
 */
public interface XydraOut {
	
	void open(String type);
	
	<T> void attribute(String name, T value);
	
	void beginChildren(String name, boolean multiple);
	
	void beginChildren(String name, boolean multiple, String type);
	
	void beginArray();
	
	void beginArray(String type);
	
	void endArray();
	
	void endChildren();
	
	<T> void value(T value);
	
	<T> void content(String name, T content);
	
	/**
	 * Close the current element, which must have the given name.
	 * 
	 * @param elementName The name of the element to close. This must be the
	 *            same as the last call to {@link #open(String)}.
	 */
	void close(String type);
	
	void nullElement();
	
	boolean isClosed();
	
	String getData();
	
	void flush();
	
	void enableWhitespace(boolean whitespace, boolean idententation);
	
	/**
	 * Convenience method to add a value list.
	 * 
	 * This method can be only called when in the context of an element.
	 * 
	 * <p>
	 * Shortcut for:
	 * 
	 * <pre>
	 * beginChildren(name, true, type);
	 * for(T value : values) {
	 * 	value(value);
	 * }
	 * endChildren();
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * XML will look like
	 * 
	 * <pre>
	 * &lt;type&gt;value1&lt;/type&gt;
	 * &lt;type&gt;value2&lt;/type&gt;
	 * ...
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * JSON will look like:
	 * 
	 * <pre>
	 * name: [ "value1", "value2", ... ]
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * This can be decoded using
	 * {@link MiniElement#getValueList(String, String)}
	 * </p>
	 * 
	 * @see #beginChildren(String, boolean, String)
	 * @see #value(Object)
	 * @see #endChildren()
	 */
	<T> void values(String name, String type, Iterable<T> values);
	
	/**
	 * Convenience method to add a single value.
	 * 
	 * This method can be only called when in the context of an element.
	 * 
	 * <p>
	 * Shortcut for:
	 * 
	 * <pre>
	 * beginChildren(name, false, type);
	 * value(value);
	 * endChildren();
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * XML will look like
	 * 
	 * <pre>
	 * &lt;type&gt;value&lt;/type&gt;
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * JSON will look like:
	 * 
	 * <pre>
	 * name: "value"
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * This can be decoded using {@link MiniElement#getValue(String, String)}
	 * </p>
	 * 
	 * @see #beginChildren(String, boolean, String)
	 * @see #value(Object)
	 * @see #endChildren()
	 */
	<T> void value(String name, String type, T value);
	
	/**
	 * Convenience method to add an element without attributes or children.
	 * 
	 * This method can be called when in the context of an element or child
	 * list.
	 * 
	 * <p>
	 * Shortcut for:
	 * 
	 * <pre>
	 * open(type);
	 * content(name, content);
	 * close(type);
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * XML will look like
	 * 
	 * <pre>
	 * &lt;type&gt;content&lt;/type&gt;
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * JSON will look like (directly in other element):
	 * 
	 * <pre>
	 * type: {
	 *   name: "content"
	 * }
	 * </pre>
	 * 
	 * or (in a child list with the same type):
	 * 
	 * <pre>
	 * { name: &quot;content&quot; }
	 * </pre>
	 * 
	 * or (in a child list with no type):
	 * 
	 * <pre>
	 * { "$type": "type", name: "content" }
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @see #open(String)
	 * @see #content(String, Object)
	 * @see #close(String)
	 */
	<T> void element(String type, String name, T content);
	
	/**
	 * Convenience method to add an empty element without attributes.
	 * 
	 * This method can be called when in the context of an element or child
	 * list.
	 * 
	 * <p>
	 * Shortcut for:
	 * 
	 * <pre>
	 * open(type);
	 * close(type);
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * XML will look like
	 * 
	 * <pre>
	 * &lt;type/&gt;
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * JSON will look like (directly in another element):
	 * 
	 * <pre>
	 * type: {
	 * }
	 * </pre>
	 * 
	 * or (in a child list with the same type):
	 * 
	 * <pre>
	 * {}
	 * </pre>
	 * 
	 * or (in a child list with no type):
	 * 
	 * <pre>
	 * { "$type": "type" }
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @see #open(String)
	 * @see #close(String)
	 */
	<T> void element(String type);
	
}
