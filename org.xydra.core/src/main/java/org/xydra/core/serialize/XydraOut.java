package org.xydra.core.serialize;

import org.xydra.base.minio.MiniWriter;

/**
 * An interface for writing XML-based or JSON-based streams from structured
 * collections. This is a stream-like API; the corresponding document-like API
 * is {@link XydraElement}.
 * 
 * E.g. beginArray maps to an array in JSON and to another construct in XML.
 * 
 * Concepts: attribute, array, map, child, open/close, content, element, entry,
 * value, childType
 * 
 * Concepts here but not in {@link XydraElement}: array, map, open/close
 * 
 * <p>
 * Encoding must start by opening the root element using {@link #open(String)}
 * and must end by closing the root element using {@link #close(String)} with
 * the same type.
 * </p>
 * 
 * <p>
 * Types of entities modeled by {@link XydraOut}:
 * <ul>
 * 
 * <li><b>Element</b>: See {@link #open(String)}, {@link #close(String)},
 * {@link #attribute(String, Object)} and {@link #child(String)} <br>
 * Object with attributes and child entities. <br>
 * Convenience functions:
 * 
 * <li><b>Array</b>: See {@link #beginArray()} and {@link #endArray()} <br>
 * Ordered list of entities. <br>
 * Convenience functions:
 * 
 * <li>Map: See {@link #beginMap(String)}, {@link #endMap()} and
 * {@link #entry(String)} <br>
 * Associative array mapping keys to entities. <br>
 * Convenience functions:
 * 
 * <li>Value: See {@link #value(Object)}. <br>
 * An atomic value: null, numbers, booleans and strings. <br>
 * Convenience functions:
 * 
 * </ul>
 * </p>
 * 
 * <p>
 * The {@link #setChildType(String)} and {@link #setDefaultType(String)}
 * functions can be used to fine-tune XML/JSON encodings of entities.
 * </p>
 * 
 * <p>
 * The {@link #enableWhitespace(boolean, boolean)} method allows to enable or
 * disable additional formatting that will not influence the meaning of the
 * output.
 * </p>
 * 
 * <p>
 * Data encoded using a {@link XydraOut} implementation can be decoded with a
 * corresponding {@link XydraParser} implementation.
 * </p>
 * 
 * @author xamde
 * @author dscharrer
 */
public interface XydraOut {

	/* Element */

	/**
	 * Start a new element of the give type. Elements roughly resemble XML
	 * elements and can have attributes and children. While elements are
	 * powerful enough to encode all data structures, other entity types like
	 * arrays and maps can allow a better encoding in some cases.
	 * 
	 * <p>
	 * This method changes the context to a new element context.
	 * </p>
	 * 
	 * <p>
	 * For XML, this starts a new element tag: &lt;type&gt;
	 * </p>
	 * 
	 * <p>
	 * For JSON, elements are encoded as objects. If the current context does
	 * not have a default child type or this element has a different type, the
	 * element type is encoded in the "$type" property:
	 * 
	 * <pre>
	 * { "$type": "type" ... }
	 * </pre>
	 * 
	 * Otherwise, the element type is not encoded.
	 * </p>
	 * 
	 * <p>
	 * Attributes can be set for the current element using
	 * {@link #attribute(String, Object)}. No attributes can be set if the
	 * current element already has any children.
	 * </p>
	 * 
	 * <p>
	 * Entities cannot be added directly to elements. Instead, a new (named)
	 * child context must be created using {@link #child(String)}. Children and
	 * attributes names share the same name-space. Exactly one entity can be
	 * added to the child context using {@link #open(String)},
	 * {@link #beginArray(String)}, {@link #beginMap(String)} or
	 * {@link #value(Object)}.The child context is automatically closed and the
	 * element context becomes active again once the child ends.
	 * </p>
	 * 
	 * <p>
	 * A child context is untyped by default. A child context can become typed
	 * using the {@link #setDefaultType(String)} or
	 * {@link #setChildType(String)} functions. Alternatively, the
	 * {@link #child(String, String)} convenience function can be used to create
	 * typed child contexts.
	 * <p>
	 * 
	 * <p>
	 * An element that has an untyped child {@link #value(Object)} cannot have
	 * any other children. This concept is provided by the
	 * {@link #content(String, Object)} convenience function and has a special
	 * XML encoding:
	 * 
	 * <pre>
	 * &lt;type&gt;value&lt;/type&gt;
	 * </pre>
	 * 
	 * or if the value is null:
	 * 
	 * <pre>
	 * &lt;element nullContent="true"/&gt;
	 * </pre>
	 * 
	 * To add an untyped "null" that will allow other children, use
	 * {@link #nullElement()}.
	 * </p>
	 * 
	 * <p>
	 * Attributes can be retrieved using
	 * {@link XydraElement#getAttribute(String)}.
	 * </p>
	 * 
	 * <p>
	 * The element context must be closed by a corresponding call to
	 * {@link #close(String)} with the same type.
	 * </p>
	 * 
	 * <p>
	 * Children can be retrieved using {@link XydraElement#getChild(String)},
	 * {@link XydraElement#getChild(String, String)},
	 * {@link XydraElement#getChildrenByName(String)},
	 * {@link XydraElement#getChildrenByName(String, String)} and
	 * {@link XydraElement#getChildrenByType(String, String)}
	 * </p>
	 * 
	 * <p>
	 * Child values can also be retrieved directly using
	 * {@link XydraElement#getValue(String, int)},
	 * {@link XydraElement#getValue(String, String)},
	 * {@link XydraElement#getValues(String)} and
	 * {@link XydraElement#getValues(String, String)}.
	 * </p>
	 * 
	 * <p>
	 * Child elements can also be retrieved using
	 * {@link XydraElement#getElement(String)} and
	 * {@link XydraElement#getElement(String, int)}
	 * </p>
	 * 
	 * @param type The element type to open. This must be a valid XML element
	 *            name. "xnull", "xmap", "xarray" and "xvalue" are reserved
	 *            types.
	 * 
	 *            When the current context has type set using
	 *            {@link #setChildType(String)}, this type must match.
	 */
	void open(String type);

	/**
	 * Set an attribute of the current element.
	 * 
	 * <p>
	 * This method can only be called when in the context of an element that
	 * doesn't have any children yet. Attributes and children of the same
	 * element share a common namespace.
	 * </p>
	 * 
	 * <p>
	 * For XML, attributes are encoded as element attributes:
	 * 
	 * <pre>
	 * &lt;element name="value"/&gt;
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * For JSON, attributes are encoded as JSON null-, number-, boolean- or
	 * string-properties:
	 * 
	 * <pre>
	 * { "name": value }
	 * </pre>
	 * 
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
	 *            unique attribute name for this element. '$type', 'isNull' and
	 *            'nullContent' are reserved names. This must be a valid XML
	 *            attribute name.
	 * @param value The content to set. This must not be null.
	 */
	<T> void attribute(String name, T value);

	/**
	 * Add a child to the current element.
	 * 
	 * <p>
	 * This method can only be called when in the context of an element.
	 * Attributes and children of the same element share a common namespace.
	 * </p>
	 * 
	 * <p>
	 * This method changes the context to a new untyped child context. The child
	 * context can be made typed using {@link #setDefaultType(String)} or
	 * {@link #setChildType(String)}. Alternatively, the
	 * {@link #child(String, String)} convenience function can be used to create
	 * typed child contexts.
	 * </p>
	 * 
	 * <p>
	 * Exactly one entity can be added to the child context using
	 * {@link #open(String)}, {@link #beginArray(String)},
	 * {@link #beginMap(String)} or {@link #value(Object)}.The child context is
	 * automatically closed and the element context becomes active again once
	 * the child ends.
	 * </p>
	 * 
	 * <p>
	 * For XML, child names are ignored and children are generally encoded using
	 * an element:
	 * 
	 * <pre>
	 * &lt;element&gt;
	 *   &lt;child&gt;
	 * &lt;element&gt;
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * An element that has an untyped child {@link #value(Object)} cannot have
	 * any other children. This concept is provided by the
	 * {@link #content(String, Object)} convenience function and has a special
	 * XML encoding:
	 * 
	 * <pre>
	 * &lt;element&gt;value&lt;/element&gt;
	 * </pre>
	 * 
	 * or if the value is null:
	 * 
	 * <pre>
	 * &lt;element nullContent="true"/&gt;
	 * </pre>
	 * 
	 * To add an untyped "null" that will allow other children, use
	 * {@link #nullElement()}.
	 * </p>
	 * 
	 * <p>
	 * For JSON, this is encoded as a property with the given name. The value of
	 * that property will be whatever is added as a child:
	 * 
	 * <pre>
	 * { "name": child }
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * Children can be retrieved using {@link XydraElement#getChild(String)},
	 * {@link XydraElement#getChild(String, String)},
	 * {@link XydraElement#getChildrenByName(String)},
	 * {@link XydraElement#getChildrenByName(String, String)} and
	 * {@link XydraElement#getChildrenByType(String, String)}
	 * </p>
	 * 
	 * <p>
	 * Child values can also be retrieved directly using
	 * {@link XydraElement#getValue(String, int)},
	 * {@link XydraElement#getValue(String, String)},
	 * {@link XydraElement#getValues(String)} and
	 * {@link XydraElement#getValues(String, String)}.
	 * </p>
	 * 
	 * <p>
	 * Child elements can also be retrieved using
	 * {@link XydraElement#getElement(String)} and
	 * {@link XydraElement#getElement(String, int)}
	 * </p>
	 * 
	 * @param name A name for the child list. This must not be null and a unique
	 *            attribute name for this element. '$type', 'isNull' and
	 *            'nullContent' are reserved names.
	 */
	void child(String name);

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

	/* Array */

	/**
	 * Start a new array. Arrays are ordered lists of arbitrary entities
	 * (elements, arrays, maps or values).
	 * 
	 * <p>
	 * This method changes the context to a new untyped array context. The array
	 * context can be made typed using {@link #setDefaultType(String)} or
	 * {@link #setChildType(String)}. Alternatively, the
	 * {@link #beginArray(String)} convenience function can be used to create
	 * typed arrays.
	 * </p>
	 * 
	 * <p>
	 * For XML, this is mapped to a an element containing all entries of the
	 * array.
	 * 
	 * If the current context has a type set using
	 * {@link #setDefaultType(String)} or {@link #setChildType(String)}, the
	 * array is created as:
	 * 
	 * <pre>
	 * &lt;type&gt;...&lt;/type&gt;
	 * </pre>
	 * 
	 * otherwise, the array is created as
	 * 
	 * <pre>
	 * &lt;xarray&gt;...&lt;/xarray&gt;
	 * </pre>
	 * 
	 * Arrays added to an untyped child directly in an element are handled
	 * specially: No element is created for the array and instead the array
	 * entries are added directly inside the parent XML element. To distinguish
	 * the array elements from other children of the parent, a common type can
	 * be enforced for all entries in the array using
	 * {@link #setChildType(String)} .
	 * 
	 * </p>
	 * 
	 * <p>
	 * For JSON, this is mapped to a native JSON array:
	 * 
	 * <pre>
	 * [...]
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * Any number of arbitrary entities can be added to an array using
	 * {@link #open(String)}, {@link #beginArray()}, {@link #beginMap(String)}
	 * or {@link #value(Object)}.
	 * </p>
	 * 
	 * <p>
	 * The array context must be closed by a corresponding call to
	 * {@link #endArray()}
	 * </p>
	 * 
	 * <p>
	 * Arrays are retrieved as {@link XydraElement}s with no text content and no
	 * attributes. Child elements can be retrieved using
	 * {@link XydraElement#getChildren()} or
	 * {@link XydraElement#getChildren(String)}.
	 * </p>
	 * 
	 * <p>
	 * Value entries can be retrieved using {@link XydraElement#getValues()} on
	 * the array element.
	 * </p>
	 * 
	 */
	void beginArray();

	/**
	 * End the current array.
	 * 
	 * <p>
	 * This method can only be called in the context of an array.
	 * </p>
	 * 
	 * <p>
	 * This method changes the context back to that which was active before the
	 * corresponding {@link #beginArray()} call.
	 * </p>
	 */
	void endArray();

	/* Map */

	/**
	 * Start a new map. Maps are an associative array with a unique key for each
	 * entry.
	 * 
	 * <p>
	 * This method changes the context to a new untyped map context. The map
	 * context can be made typed using {@link #setDefaultType(String)} or
	 * {@link #setChildType(String)}. Alternatively, the
	 * {@link #beginMap(String, String)} convenience function can be used to
	 * create typed arrays.
	 * </p>
	 * 
	 * <p>
	 * For XML, this is mapped to a an element containing all entries of the
	 * map.
	 * 
	 * If the current context has a type set using
	 * {@link #setDefaultType(String)} or {@link #setChildType(String)}, the
	 * array is created as:
	 * 
	 * <pre>
	 * &lt;type&gt;...&lt;/type&gt;
	 * </pre>
	 * 
	 * otherwise, the map is created as
	 * 
	 * <pre>
	 * &lt;xmap&gt;...&lt;/xmapy&gt;
	 * </pre>
	 * 
	 * Maps added to an untyped child directly in an element are handled
	 * specially: No element is created for the map and instead the entries are
	 * added directly inside the parent XML element. To distinguish the map
	 * entries from other children of the parent, a common type can be enforced
	 * for all entries in the map using {@link #setChildType(String)} .
	 * 
	 * Keys are stored as a special attribute chosen by the attribute parameter
	 * in the entities added to the map.
	 * 
	 * </p>
	 * 
	 * <p>
	 * For JSON, this is mapped to a JSON object:
	 * 
	 * <pre>
	 * { "key1": entity1, ... }
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * No entities can be added directly to a map. Instead a new entry context
	 * must be created using {@link #entry(String)} to specify the key for the
	 * following entity. The entry context is automatically closed once the
	 * associated entity ends.
	 * </p>
	 * 
	 * <p>
	 * The map context must be closed by a corresponding call to
	 * {@link #endMap()}
	 * </p>
	 * 
	 * <p>
	 * Maps are retrieved as {@link XydraElement}s with no text content and no
	 * attributes. Entries can be retrieved using
	 * {@link XydraElement#getEntries(String)},
	 * {@link XydraElement#getEntries(String, String)} or
	 * {@link XydraElement#getEntriesByType(String, String)}.
	 * </p>
	 * 
	 * @param attribute The name of the attribute to store keys in.
	 * 
	 */
	void beginMap(String attribute);

	/**
	 * Add an entry to the current map.
	 * 
	 * <p>
	 * This method can only be called when in the context of a map.
	 * </p>
	 * 
	 * <p>
	 * This method changes the context to a new entry context. No type can be
	 * set on an individual entry context. Instead, (default) types should be
	 * set for the whole map.
	 * </p>
	 * 
	 * <p>
	 * Exactly one entity can be added to the entry context using
	 * {@link #open(String)}, {@link #beginArray(String)},
	 * {@link #beginMap(String)} or {@link #value(Object)}.The entry context is
	 * automatically closed and the map context becomes active again once the
	 * associated entity ends.
	 * </p>
	 * 
	 * <p>
	 * For XML, the key name is included in the associated entity as the
	 * attribute specified by the {@link #beginMap(String)} call:
	 * 
	 * <pre>
	 * &lt;entity1 attribute="key1"&gt;
	 * &lt;entity2 attribute="key2"&gt;
	 * &lt;entity3 attribute="key3"&gt;
	 * ...
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * For JSON, maps are encoded as native objects:
	 * 
	 * <pre>
	 * { "key1": entity1, "key2": entity2, ... }
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * Map entries can be retrieved using
	 * {@link XydraElement#getEntries(String)},
	 * {@link XydraElement#getEntries(String, String)} or
	 * {@link XydraElement#getEntriesByType(String, String)}.
	 * </p>
	 * 
	 * <p>
	 * Values associated with a map entry can be retrieved using
	 * {@link XydraElement#getContent()}.
	 * </p>
	 * 
	 * @param key A key for the following entity.
	 */
	void entry(String key);

	/**
	 * End the current map.
	 * 
	 * <p>
	 * This method can only be called in the context of a map.
	 * </p>
	 * 
	 * <p>
	 * This method changes the context back to that which was active before the
	 * corresponding {@link #beginMap(String)} call.
	 * </p>
	 */
	void endMap();

	/* Value */

	/**
	 * Adds a value. Values are atomic types and can be stored differently
	 * depending on their class.
	 * 
	 * <p>
	 * For XML, values are encoded as child elements.
	 * 
	 * If a (default) type has been set for the current context, non-null values
	 * are encoded as:
	 * 
	 * <pre>
	 * &lt;type&gt;value&lt;/type&gt;
	 * </pre>
	 * 
	 * In untyped contexts, values are encoded as:
	 * 
	 * <pre>
	 * &lt;xvalue&gt;value&lt;/xvalue&gt;
	 * </pre>
	 * 
	 * Null values are encoded as:
	 * 
	 * <pre>
	 * &lt;xnull/&gt;
	 * </pre>
	 * 
	 * unless the current context has a forced type (set by
	 * {@link #setChildType(String)} and not {@link #setDefaultType(String)}),
	 * in which case they are encoded as:
	 * 
	 * <pre>
	 * &lt;type isNull="true"/&gt;
	 * </pre>
	 * 
	 * for non-null values. Null values are encoded as
	 * 
	 * Values stored in an untyped child context directly inside an element are
	 * handled specially: If there is any such value, the element cannot have
	 * any other children. Also, the value is stored directly inside the parent
	 * element:
	 * 
	 * <pre>
	 * &lt;element&gt;value&lt;/element&gt;
	 * </pre>
	 * 
	 * or if the value is null:
	 * 
	 * <pre>
	 * &lt;element nullContent="true"/&gt;
	 * </pre>
	 * 
	 * To add an untyped "null" that will allow other children, use
	 * {@link #nullElement()}.
	 * </p>
	 * 
	 * </p>
	 * 
	 * <p>
	 * For JSON, values are encoded as JSON null, number, boolean or string.
	 * </p>
	 * 
	 * <p>
	 * Values can be retrieved using {@link XydraElement#getContent()},
	 * {@link XydraElement#getContent(String)},
	 * {@link XydraElement#getValue(String, int)},
	 * {@link XydraElement#getValue(String, String)},
	 * {@link XydraElement#getValues()}, {@link XydraElement#getValues(String)}
	 * and {@link XydraElement#getValues(String, String)}.
	 * </p>
	 * 
	 * @param <T> The type of the value. Some types like {@link Boolean
	 *            Booleans} and {@link Number Numbers} can be encoded
	 *            differently. For everything else, the object's
	 *            {@link T#toString()} method will determine the encoded string.
	 *            While this may affect the encoding, there is no guarantee that
	 *            the decoded object will have the same type.
	 * @param value The content to set. This can be null.
	 */
	<T> void value(T value);

	/**
	 * Add a null element (one that will be parsed to null).
	 * 
	 * This is almost like {@link #value(Object)} with a null parameter, but
	 * always produces a new XML element, event in an untyped child context.
	 * This allows other children to be added to the same (current) element.
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
	 * Null elements can be retrieved like any other element.
	 * </p>
	 * 
	 * <p>
	 * For JSON, this is encoded as the native 'null' value.
	 * </p>
	 */
	void nullElement();

	/* Child type */

	/**
	 * Set a forced child type for the current context.
	 * 
	 * This behaves like {@link #setDefaultType(String)} with two additions:
	 * 
	 * <p>
	 * 1. child null entities / values will not be named &lt;xnull/&gt; but by
	 * the specified type:
	 * 
	 * <pre>
	 * &lt;type isNull="true"/&gt;
	 * </pre>
	 * 
	 * </p>
	 * 
	 * <p>
	 * 2. No entities with a type different from the specified one may be added
	 * to the current context.
	 * </p>
	 * 
	 * @param type The type to enforce for child entities.
	 * 
	 * @see #setDefaultType(String)
	 */
	void setChildType(String type);

	/**
	 * Defines a default type for child entities of the current entity.
	 * 
	 * <p>
	 * No type can be set for element or map entry contexts. Instead the
	 * (default) type should be set for the element child or map contexts
	 * respectively.
	 * </p>
	 * 
	 * <p>
	 * No type can be set after any child entities have been added and the type
	 * can only be set once for each context. The type is not inherited by child
	 * contexts.
	 * </p>
	 * 
	 * <p>
	 * Effects (XML):
	 * 
	 * <ul>
	 * 
	 * <li>Arrays, maps and values added to an entity child context always
	 * create a new XML element - their contents are no longer inlined into the
	 * parent element.
	 * 
	 * <li>The given type is used for array, map and value XML elements instead
	 * of "xarray", "xmap" and "xvalue". Null values / elements still use
	 * "xnull". To also use the given type when encoding null, use
	 * {@link #setChildType(String)}.
	 * 
	 * </p>
	 * 
	 * <p>
	 * Effects (JSON):
	 * 
	 * If the type of a child element matches the default type, it s not encoded
	 * in the JSON output.
	 * </p>
	 * 
	 * This does not prevent elements with different type from being added. To
	 * enforce a common child type use {@link #setChildType(String)}.
	 * 
	 * @param defaultType The default type to use for child entities.
	 */
	void setDefaultType(String defaultType);

	/* Output */

	/**
	 * Output whitespace to make the result more readable.
	 * 
	 * @param whitespace True if whitespace should be produced, false if not.
	 * @param idententation Also enable indentation. This requires whitespace to
	 *            be enabled.
	 */
	void enableWhitespace(boolean whitespace, boolean idententation);

	/**
	 * Flush the underlying {@link MiniWriter}.
	 */
	void flush();

	/**
	 * @return the MIME content type of the produced output.
	 */
	String getContentType();

	/**
	 * @return True if all elements have been closed, including the root
	 *         element.
	 */
	boolean isClosed();

	/**
	 * Get the encoded data.
	 * 
	 * This only works if the {@link XydraOut} was not constructed with a
	 * streaming {@link MiniWriter}.
	 * 
	 * The root element must have been closed.
	 * 
	 * @return The XML or JSON encoded data.
	 */
	String getData();

	/* Convenience --------------------------------------- */

	/**
	 * Convenience method to start typed maps:
	 * 
	 * <pre>
	 * beginMap(attribute);
	 * setChildType(type);
	 * </pre>
	 * 
	 * @param attribute
	 * @param type
	 */
	void beginMap(String attribute, String type);

	/**
	 * Convenience method to start typed arrays:
	 * 
	 * <pre>
	 * beginArray();
	 * setChildType(type);
	 * </pre>
	 * 
	 * @param type
	 */
	void beginArray(String type);

	/**
	 * Convenience method to start a typed child context:
	 * 
	 * <pre>
	 * child(name);
	 * setChildType(type);
	 * </pre>
	 * 
	 * @param name
	 * @param type
	 * 
	 * @see #child(String)
	 * @see #setChildType(String)
	 */
	void child(String name, String type);

	/**
	 * Convenience method to add an untyped child value (aka. content):
	 * 
	 * <pre>
	 * child(name);
	 * value(content);
	 * </pre>
	 * 
	 * @param name
	 * @param content
	 */
	<T> void content(String name, T content);

	/**
	 * Convenience method to add a value list:
	 * 
	 * <pre>
	 * child(name);
	 * beginArray(type);
	 * for (T value : values) {
	 * 	value(value);
	 * }
	 * endArray();
	 * </pre>
	 * 
	 * @param name
	 * @param type
	 * @param values
	 * 
	 * @see #child(String)
	 * @see #beginArray()
	 * @see #value(Object)
	 * @see #endArray()
	 */
	<T> void values(String name, String type, Iterable<T> values);

	/**
	 * Convenience method to add a single value:
	 * 
	 * <p>
	 * 
	 * <pre>
	 * child(name, type);
	 * value(value);
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param name
	 * @param type
	 * @param value
	 * 
	 * @see #child(String, String)
	 * @see #value(Object)
	 */
	<T> void value(String name, String type, T value);

	/**
	 * Convenience method to add an element without attributes and exactly one
	 * untyped child value:
	 * 
	 * <p>
	 * 
	 * <pre>
	 * open(type);
	 * content(name, content);
	 * close(type);
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param type
	 * @param name
	 * @param content
	 * 
	 * @see #open(String)
	 * @see #content(String, Object)
	 * @see #close(String)
	 */
	<T> void element(String type, String name, T content);

	/**
	 * Convenience method to add an empty element without attributes or
	 * children:
	 * 
	 * <p>
	 * 
	 * <pre>
	 * open(type);
	 * close(type);
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param type
	 * 
	 * @see #open(String)
	 * @see #close(String)
	 */
	void element(String type);

}
