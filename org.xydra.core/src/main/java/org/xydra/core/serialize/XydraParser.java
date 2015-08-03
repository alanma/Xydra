package org.xydra.core.serialize;

import org.xydra.base.minio.MiniReader;

/**
 * A minimal XML/JSON parser for documents generated with {@link XydraOut}.
 *
 * @author dscharrer
 *
 */
public interface XydraParser {

	/**
	 * Parse the given String as a document and return the root element.
	 *
	 * @param data
	 * @return the parsed result or throws an Exception
	 *
	 * @throws IllegalArgumentException if the given string is not a valid
	 *             document.
	 */
	XydraElement parse(String data) throws IllegalArgumentException;

	XydraElement parse(MiniReader miniReader) throws IllegalArgumentException;

	/**
	 * @return The MIME content type accepted by this parser.
	 */
	String getContentType();

}
