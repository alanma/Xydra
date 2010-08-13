package org.xydra.core.xml;

/**
 * A minimal interface for writing XML-based streams.
 * 
 * This interface is GWT-compatible.
 * 
 * Implementations must write out the XML declaration at the beginning of a
 * stream.
 * 
 * 
 * This is similar to http://java.ociweb.com/mark/programming/WAX.html
 * 
 * 
 * @author voelkel
 */
public interface XmlOut {
	
	/**
	 * Standard XML header with UTF-8 encoding.
	 */
	public static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
	
	public void open(String elementName);
	
	public void attribute(String name, String value);
	
	public void content(String rawContent);
	
	/**
	 * Close the element with the given name -- do not confuse with
	 * {@link #close()}
	 * 
	 * @param elementName
	 */
	public void close(String elementName);
	
	public void comment(String string);
	
	/**
	 * circumvent xml and just write as-is to writer
	 * 
	 * @
	 */
	public void write(String s);
	
	public void openProcessingInstruction(String processingInstruction);
	
	public void closeProcessingInstruction();
	
	public void doctype(String doctype, String publicID, String url);
	
	public void flush();
	
	/**
	 * Close the stream (for steam-based implementations) -- do not confuse with
	 * {@link #close(String)}
	 */
	public void close();
}
