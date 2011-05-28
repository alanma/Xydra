package org.xydra.core.xml;

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
	
	void open(String elementName);
	
	<T> void attribute(String name, T value);
	
	void children(String name, boolean multiple);
	
	<T> void value(String type, T value);
	
	<T> void content(String name, T content);
	
	/**
	 * Close the current element, which must have the given name.
	 * 
	 * @param elementName The name of the element to close. This must be the
	 *            same as the last call to {@link #open(String)}.
	 */
	void close(String elementName);
	
	boolean isClosed();
	
	String getData();
	
	void flush();
	
}
