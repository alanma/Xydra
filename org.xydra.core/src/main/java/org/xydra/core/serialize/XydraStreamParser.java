package org.xydra.core.serialize;

import org.xydra.base.minio.MiniReader;
import org.xydra.core.serialize.xml.XmlOut;

/**
 * A minimal XML/JSON parser for documents generated with {@link XydraOut}.
 * 
 * @author dscharrer
 * 
 */
public interface XydraStreamParser {

	/**
	 * Parse the given String as an event stream
	 * 
	 * @param miniReader
	 * @param xmlOut where to send the events to
	 * @return true if all went well
	 * 
	 * @throws IllegalArgumentException if the given string is not a valid
	 *             document.
	 */
	boolean parse(MiniReader miniReader, XmlOut xmlOut) throws IllegalArgumentException;

	/**
	 * @return The MIME content type accepted by this parser.
	 */
	String getContentType();

}
