package org.xydra.core.serialize;

import org.xydra.annotations.NeverNull;
import org.xydra.index.query.Pair;

import java.util.Iterator;


/**
 * A minimal abstraction API to access XML/JSON elements created with
 * {@link XydraOut}.
 * 
 * @author voelkel
 * @author dscharrer
 */
public interface XydraElement {
    
    /**
     * Get an attribute of this element.
     * 
     * @param name XML: The name of the attribute to get. JSON: the property
     *            name.
     * @return the attribute value or null if it does not exist
     * 
     *         This may be a {@link String}, a {@link Boolean} or a
     *         {@link Number}.
     * 
     *         The type of the returned object may differ from that used for
     *         writing via {@link XydraOut#attribute(String, Object)}
     */
    public Object getAttribute(String name);
    
    /**
     * Get this element's text content.
     * 
     * @return the contained character data. If this element contains additional
     *         children, the return value is undefined.
     * 
     *         This may be null, a {@link String}, a {@link Boolean} or a
     *         {@link Number}.
     * 
     *         The type of the returned object may differ from that used for
     *         writing via {@link XydraOut#content(String, Object)}
     */
    public Object getContent();
    
    /**
     * Get this element's text content.
     * 
     * @param name The name under which the content is saved. This is not used
     *            for XML. For JSON this is the same as
     *            {@link #getAttribute(String)}.
     * @return return the contained character data. If this element contains
     *         additional children, the return value is undefined.
     * 
     *         This may be null, a {@link String}, a {@link Boolean} or a
     *         {@link Number}.
     * 
     *         The type of the returned object may differ from that used for
     *         writing via {@link XydraOut#content(String, Object)}
     */
    public Object getContent(String name);
    
    /**
     * @return children as XydraElement iterator
     */
    public Iterator<XydraElement> getChildren();
    
    /**
     * @param defaultType TODO document
     * @return children as XydraElement iterator
     */
    public Iterator<XydraElement> getChildren(String defaultType);
    
    /**
     * Get all child elements.
     * 
     * For XML this returns all child elements.
     * 
     * For JSON this returns the value of that property. If the property value
     * is an array, the array elements are returned. If the property value is an
     * object, that object is returned.
     * 
     * This method is meant to be used for retrieving elements or arrays that
     * have been added using {@link XydraOut#open(String)} or
     * {@link XydraOut#beginArray()} to an untyped multiple-item context.
     * 
     * @param name The name under which the children are saved. This is not used
     *            for XML. For JSON this determines the property name.
     * @return all child elements in document order
     */
    public Iterator<XydraElement> getChildrenByName(String name);
    
    /**
     * @param name
     * @param defaultType
     * @return children as XydraElement iterator
     */
    public Iterator<XydraElement> getChildrenByName(String name, String defaultType);
    
    /**
     * Get all child elements of a specific type.
     * 
     * For XML this returns all child elements with the given type.
     * 
     * For JSON this returns the value of that property. If the property value
     * is an array, the array elements are returned. If the property value is an
     * object, that object is returned.
     * 
     * This method is meant to be used for retrieving elements or arrays that
     * have been added using {@link XydraOut#open(String)} or
     * {@link XydraOut#beginArray()} to a typed multiple-item context.
     * 
     * @param name The name under which the children are saved. This is not used
     *            for XML. For JSON this determines the property name.
     * @param type Only get elements with the specified type. This is not used
     *            for JSON except to set the type of returned elements if not
     *            stored in the JSON.
     * @return all matching child elements in document order
     */
    public Iterator<XydraElement> getChildrenByType(String name, String type);
    
    /**
     * Get the first child element.
     * 
     * For XML this returns the first child.
     * 
     * For JSON this returns the value of that property, but only if it is an
     * object or array.
     * 
     * If this element wraps a JSON array, this throws an error.
     * 
     * This method is meant to be used for retrieving elements or arrays that
     * have been added using {@link XydraOut#open(String)} or
     * {@link XydraOut#beginArray()} to an untyped single-item context.
     * 
     * @param name The name under which the child is saved. This is not used for
     *            XML. For JSON this determines the property name.
     * @return the first child element in document order
     */
    public XydraElement getElement(String name);
    
    /**
     * Get the i'th child element.
     * 
     * For XML this returns the i'th child.
     * 
     * For JSON this is equivalent to {@link #getElement(String)}.
     * 
     * If this element wraps a JSON array, this throws an error.
     * 
     * This method is meant to be used for retrieving elements or arrays that
     * have been added using {@link XydraOut#open(String)} or
     * {@link XydraOut#beginArray()} to an untyped single-item context where the
     * type is not unique for the containing element.
     * 
     * @param name The name under which the child is saved. This is not used for
     *            XML. For JSON this determines the property name.
     * @param index The index of the child element to get. This is not used for
     *            JSON.
     * @return the child element in document order at the given index
     */
    public XydraElement getElement(String name, int index);
    
    /**
     * Get the first child element of a specific type.
     * 
     * For XML this returns the first child of that type.
     * 
     * For JSON this returns the value of that property, but only if it is an
     * object or array.
     * 
     * If this element wraps a JSON array, this throws an error.
     * 
     * This method is meant to be used for retrieving elements or arrays that
     * have been added using {@link XydraOut#open(String)} or
     * {@link XydraOut#beginArray()} to a typed single-item context.
     * 
     * @param name The name under which the child is saved. This is not used for
     *            XML. For JSON this determines the property name.
     * @param type Only get elements with the specified type. This is not used
     *            for JSON except to set the type of returned elements if not
     *            stored in the JSON.
     * @return the first matching child element in document order
     */
    public XydraElement getChild(String name, String type);
    
    /**
     * @return The type of this element.
     */
    public @NeverNull
    String getType();
    
    /**
     * @return ???
     */
    public Iterator<Object> getValues();
    
    /**
     * Get all values.
     * 
     * For XML this returns all child elements, interpreted as values.
     * 
     * For JSON this returns the value of that property, but only if it is an
     * array. If the array contains objects or array, the result is undefined.
     * 
     * This method is meant to be used for retrieving values that have been
     * added using {@link XydraOut#value(Object)} to an untyped multiple-item
     * context.
     * 
     * @param name The name under which the values are saved. This is not used
     *            for XML. For JSON this determines the property name.
     * @return all values in document order
     */
    public Iterator<Object> getValues(String name);
    
    /**
     * Get all values of a specific type.
     * 
     * For XML this returns all child elements with the given type, interpreted
     * as values.
     * 
     * For JSON this returns the value of that property, but only if it is an
     * array. If the array contains objects or array, the result is undefined.
     * 
     * This method is meant to be used for retrieving values that have been
     * added using {@link XydraOut#value(Object)} to a typed multiple-item
     * context.
     * 
     * @param name The name under which the values are saved. This is not used
     *            for XML. For JSON this determines the property name.
     * @param type Only get values with the specified type. This is not used for
     *            JSON except to set the type of returned elements if not stored
     *            in the JSON.
     * @return all matching values in document order
     */
    public Iterator<Object> getValues(String name, String type);
    
    /**
     * Get the i'th value.
     * 
     * For XML this returns the i'th child interpreted as a value.
     * 
     * For JSON this returns the value of that property, but only if it is no
     * object or array.
     * 
     * If this element wraps a JSON array, this throws an error.
     * 
     * This method is meant to be used for retrieving values that have been
     * added using {@link XydraOut#value(Object)} to an untyped single-item
     * context where the type is not unique for the containing element.
     * 
     * @param name The name under which the value is saved. This is not used for
     *            XML. For JSON this determines the property name.
     * @param index The index of the value to get. This is not used for JSON.
     * @return the value in document order at the given index
     */
    public Object getValue(String name, int index);
    
    /**
     * Get the first child value of a specific type.
     * 
     * For XML this returns the first child of that type, interpreted as a
     * value.
     * 
     * For JSON this returns the value of that property, but only if it is no
     * object or array.
     * 
     * If this element wraps a JSON array, this throws an error.
     * 
     * This method is meant to be used for retrieving value that have been added
     * using {@link XydraOut#value(Object)} to a typed single-item context.
     * 
     * @param name The name under which the value is saved. This is not used for
     *            XML. For JSON this determines the property name.
     * @param type Only get elements with the specified type. This is not used
     *            for JSON except to set the type of returned elements if not
     *            stored in the JSON.
     * @return the first matching value in document order
     */
    public Object getValue(String name, String type);
    
    /**
     * @param name
     * @return ...
     */
    public XydraElement getChild(String name);
    
    /**
     * @param attribute
     * @return ...
     */
    public Iterator<Pair<String,XydraElement>> getEntries(String attribute);
    
    /**
     * @param attribute
     * @param defaultType
     * @return ...
     */
    public Iterator<Pair<String,XydraElement>> getEntries(String attribute, String defaultType);
    
    /**
     * @param attribute
     * @param type
     * @return ...
     */
    public Iterator<Pair<String,XydraElement>> getEntriesByType(String attribute, String type);
    
}
