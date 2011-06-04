package org.xydra.core.serialize;

import org.xydra.minio.MiniStreamWriter;


/**
 * An interface for writing XML-based or JSON-based streams.
 * 
 * @author voelkel
 * @author dscharrer
 */
public interface XydraOut {
	
	/**
	 * Start a new child element.
	 * 
	 * <p>
	 * This method can be called when in the context of a child list or array.
	 * No child elements can be added to elements that have a text content
	 * created using {@link #content(String, Object)}.
	 * </p>
	 * 
	 * <p>
	 * This method changes the context to a new element context.
	 * </p>
	 * 
	 * <p>
	 * For XML, this starts a new element tag.
	 * </p>
	 * 
	 * <p>
	 * For JSON, elements are encoded as objects.
	 * 
	 * For typed child list or array contexts the type is not saved in the
	 * object. For untyped child list and array contexts, the type is saved in
	 * the "$type" property:
	 * 
	 * <pre>
	 * { "$type": "type" ... }
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * Child elements can be retrieved using
	 * {@link XydraElement#getElement(String)} or
	 * {@link XydraElement#getElement(String, int)} if created in an untyped
	 * single-item child list, using
	 * {@link XydraElement#getChild(String, String)} if created in a typed
	 * single-item child list, using {@link XydraElement#getChildrenByName(String)}
	 * when created in an untyped multiple-item child list or array and using
	 * {@link XydraElement#getValues(String, String)} when created in a typed
	 * multiple-array child list or array.
	 * </p>
	 * 
	 * @param type The element type to open. This must be a valid XML element
	 *            name and a unique type unique for all child lists and elements
	 *            in the containing element. When in the context of an element,
	 *            this mist also be a unique (for that element) attribute name.
	 *            In that case,'isNull' and 'nullContent' are reserved names.
	 *            Otherwise 'xnull' and 'xvalue' are reserved names.
	 * 
	 *            When in a typed context, this type must match that of the
	 *            context.
	 */
	void open(String type);
	
	/**
	 * Set an attribute of the current element.
	 * 
	 * <p>
	 * This method can only be called when in the context of an element for that
	 * doesn't have any content, children and/or child list yet.
	 * </p>
	 * 
	 * <p>
	 * For XML, attributes are encoded as element attributes.
	 * </p>
	 * 
	 * <p>
	 * For JSON, attributes are encoded as JSON null-, number-, boolean- or
	 * string-properties.
	 * </p>
	 * 
	 * <p>
	 * Attributes can be retrieved using
	 * {@link XydraElement#getAttribute(String)}.
	 * </p>
	 * 
	 * @param <T> The type of the attribute. Some types like {@link Boolean
	 *            Booleans} and {@link Number Numbers} can be encoded
	 *            differently. For everything else, the object's
	 *            {@link T#toString()} method will determine the encoded string.
	 * @param name The name for the attribute. This must not be null and a
	 *            unique attribute name for this element. 'isNull' and
	 *            'nullContent' are reserved names. This must be a valid XML
	 *            attribute name.
	 * @param value The content to set. This must not be null.
	 */
	<T> void attribute(String name, T value);
	
	/**
	 * Start a new child list.
	 * 
	 * <p>
	 * This method can only be called when in the context of an element.
	 * </p>
	 * 
	 * <p>
	 * This method changes the context to a new untyped child list context. If
	 * this context allows multiple items depends on the parameter @param
	 * multiple.
	 * </p>
	 * 
	 * <p>
	 * For XML, this does not produce any direct output, only affects the
	 * encoding of children.
	 * </p>
	 * 
	 * <p>
	 * For JSON, this is encoded as a property with the given name. The value of
	 * that property will be whatever is added as a child.
	 * </p>
	 * 
	 * <p>
	 * Child elements can be retrieved using
	 * {@link XydraElement#getElement(String)} or
	 * {@link XydraElement#getElement(String, int)} if created in an single-item
	 * child list or using {@link XydraElement#getChildrenByName(String)} when created
	 * in an multiple-item child list.
	 * 
	 * Arrays are retrieved as {@link XydraElement}s with no text content.
	 * 
	 * Values can be retrieved using {@link XydraElement#getValue(String, int)}
	 * for single-item child lists or using
	 * {@link XydraElement#getValues(String)} for multiple-item child lists.
	 * </p>
	 * 
	 * @param name A name for the child list. This must not be null and a unique
	 *            attribute name for this element. 'isNull' and 'nullContent'
	 *            are reserved names.
	 * @param multiple True to create a multiple-item child list, false to
	 *            create a single-item child list.
	 * 
	 *            Single-item child list must contain exactly one child.
	 * 
	 *            Elements with a untyped multiple-item child list cannot have
	 *            any other content or child lists.
	 */
	void child(String name);
	
	/**
	 * Set the child type for the current context,.
	 * 
	 * @param type The type of the child / children. This must be a valid XML
	 *            element name and a unique type unique for all child lists and
	 *            elements in the containing element.
	 * 
	 *            All elements in this child list must have this type.
	 */
	void setChildType(String type);
	
	void beginMap(String attribute);
	
	void entry(String id);
	
	void endMap();
	
	void setDefaultType(String type);
	
	/**
	 * Shortcut for:
	 * 
	 * <pre>
	 * beginMap(attribute);
	 * setChildType(type);
	 * </pre>
	 */
	void beginMap(String attribute, String type);
	
	/**
	 * Start a new array.
	 * 
	 * <p>
	 * This method can only be called when in the context of a child list or
	 * array.
	 * </p>
	 * 
	 * <p>
	 * This method changes the context to a new untyped array context (which
	 * always allows multiple items).
	 * </p>
	 * 
	 * <p>
	 * For XML, this is mapped to a an element containing all entries of the
	 * array.
	 * 
	 * If the array is created in a typed context with type 'type', it is
	 * created as:
	 * 
	 * <pre>
	 * &lt;type&gt;...&lt;/type&gt;
	 * </pre>
	 * 
	 * if created in an untyped context, the array is created as
	 * 
	 * <pre>
	 * &lt;xarray&gt;...&lt;/xarray&gt;
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * For JSON, this is mapped to a native JSON array.
	 * </p>
	 * 
	 * <p>
	 * Arrays are retrieved as {@link XydraElement}s with no text content.
	 * 
	 * Child elements can be retrieved using
	 * {@link XydraElement#getChildrenByName(String)} on the array element. The name
	 * parameter is ignored.
	 * 
	 * Values can be retrieved using {@link XydraElement#getValues(String)} on
	 * the array element. The name parameter is ignored.
	 * </p>
	 * 
	 * @param type The type of the child list. This must be a valid XML element
	 *            name and a unique type unique for all child lists and elements
	 *            in the containing element.
	 * 
	 *            All elements in this child list must have this type.
	 */
	void beginArray();
	
	/**
	 * Shortcut for:
	 * 
	 * <pre>
	 * beginArray();
	 * setChildType(type);
	 * </pre>
	 */
	void beginArray(String type);
	
	/**
	 * End the current array.
	 * 
	 * If an array has been started, this must be called before closing the
	 * containing child list or array.
	 */
	void endArray();
	
	/**
	 * Set the content of the current element.
	 * 
	 * <p>
	 * This method can only be called when in the context of an array or child
	 * list.
	 * </p>
	 * 
	 * <p>
	 * For XML, values are encoded as child elements.
	 * 
	 * In typed contexts using with a type 'type', values are encoded as:
	 * 
	 * <pre>
	 * &lt;type&gt;value&lt;/type&gt;
	 * </pre>
	 * 
	 * for non-null values. Null values are encoded as
	 * 
	 * <pre>
	 * &lt;type isNull="true"/&gt;
	 * </pre>
	 * 
	 * In untyped contexts, values are encoded as:
	 * 
	 * <pre>
	 * &lt;xvalue&gt;value&lt;/xvalue&gt;
	 * </pre>
	 * 
	 * for non-null values. Null values are encoded as
	 * 
	 * <pre>
	 * &lt;xnull/&gt;
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * For JSON, values are encoded as JSON null, number, boolean or string.
	 * </p>
	 * 
	 * <p>
	 * Values can be retrieved using {@link XydraElement#getValue(String, int)}
	 * for untyped single-item contexts, using
	 * {@link XydraElement#getValue(String, String)} for typed single-item
	 * contexts, using {@link XydraElement#getValues(String)} for untyped
	 * multiple-item contexts and using
	 * {@link XydraElement#getValues(String, String)} for typed multiple-item
	 * contexts. Array contexts are always multiple-item.
	 * </p>
	 * 
	 * @param <T> The type of the value. Some types like {@link Boolean
	 *            Booleans} and {@link Number Numbers} can be encoded
	 *            differently. For everything else, the object's
	 *            {@link T#toString()} method will determine the encoded string.
	 * @param value The content to set. This can be null.
	 */
	<T> void value(T value);
	
	/**
	 * Shortcut for:
	 * 
	 * <pre>
	 * child(name);
	 * value(content);
	 * </pre>
	 */
	<T> void content(String name, T content);
	
	/**
	 * Close the current element.
	 * 
	 * <p>
	 * This method can only be called in the context of an element.
	 * </p>
	 * 
	 * <p>
	 * This method changes the context back to that which was active before the
	 * corresponding {@link #open(String)} call.
	 * </p>
	 * 
	 * @param type The element type to close. This must match the element type
	 *            that was used for the {@link #open(String)} call that created
	 *            the current context.
	 */
	void close(String type);
	
	/**
	 * Add a null element (one that will be parsed to null).
	 * 
	 * <p>
	 * This method can be called when in the context of a child list or array.
	 * </p>
	 * 
	 * <p>
	 * This method does not change the context.
	 * </p>
	 * 
	 * <p>
	 * In typed contexts using with a type 'type', null elements are encoded as:
	 * 
	 * <pre>
	 * &lt;type isNull="true"/&gt;
	 * </pre>
	 * 
	 * In untyped contexts, null elements are encoded as:
	 * 
	 * <pre>
	 * &lt;xnull/&gt;
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * Null values can be retrieved like any other elements.
	 * </p>
	 * 
	 * <p>
	 * For JSON, this is encoded as the native 'null' value.
	 * </p>
	 */
	void nullElement();
	
	/**
	 * @return True if all elements have been closed.
	 */
	boolean isClosed();
	
	/**
	 * Get the encoded data.
	 * 
	 * This only works if the {@link XydraOut} was not constructed with a
	 * {@link MiniStreamWriter}.
	 * 
	 * The root element must have been closed.
	 * 
	 * @return The XML or JSON encoded data.
	 */
	String getData();
	
	/**
	 * Flush the underlying {@link MiniStreamWriter}.
	 */
	void flush();
	
	/**
	 * Output whitespace to make the result more readable.
	 * 
	 * @param whitespace True if whitespace should be produced, false if not.
	 * @param idententation Also enable indentation. This requires whitespace to
	 *            be enabled.
	 */
	void enableWhitespace(boolean whitespace, boolean idententation);
	
	/**
	 * Shortcut for:
	 * 
	 * <pre>
	 * child(name);
	 * setChildType(type);
	 * </pre>
	 * 
	 * @see #child(String)
	 * @see #setChildType(String)
	 */
	void child(String name, String type);
	
	/**
	 * Convenience method to add a value list.
	 * 
	 * <p>
	 * This method can be only called when in the context of an element.
	 * </p>
	 * 
	 * <p>
	 * Shortcut for:
	 * 
	 * <pre>
	 * child(name);
	 * beginArray(type);
	 * for(T value : values) {
	 * 	value(value);
	 * }
	 * endArray();
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
	 * This can be decoded using {@link XydraElement#getValues(String, String)}
	 * </p>
	 * 
	 * @see #child(String)
	 * @see #beginArray()
	 * @see #value(Object)
	 * @see #endArray()
	 */
	<T> void values(String name, String type, Iterable<T> values);
	
	/**
	 * Convenience method to add a single value.
	 * 
	 * <p>
	 * This method can be only called when in the context of an element.
	 * </p>
	 * 
	 * <p>
	 * Shortcut for:
	 * 
	 * <pre>
	 * child(name, type);
	 * value(value);
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
	 * This can be decoded using {@link XydraElement#getValue(String, String)}
	 * </p>
	 * 
	 * @see #child(String, String)
	 * @see #value(Object)
	 */
	<T> void value(String name, String type, T value);
	
	/**
	 * Convenience method to add an element without attributes or children.
	 * 
	 * <p>
	 * This method can be called when in the context of an element or child
	 * list.
	 * </p>
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
	
	/**
	 * @return the MIME content type of the produced output.
	 */
	String getContentType();
	
}
