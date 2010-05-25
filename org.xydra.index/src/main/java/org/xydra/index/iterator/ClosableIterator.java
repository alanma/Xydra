/**
 * LICENSE INFORMATION
 * 
 * Copyright 2005-2008 by FZI (http://www.fzi.de). Licensed under a BSD license
 * (http://www.opensource.org/licenses/bsd-license.php) <OWNER> = Max Völkel
 * <ORGANIZATION> = FZI Forschungszentrum Informatik Karlsruhe, Karlsruhe,
 * Germany <YEAR> = 2008
 */

package org.xydra.index.iterator;

import java.util.Iterator;


/**
 * An <b>closable</b> iterator over a collection. Iterator takes the place of
 * Enumeration in the Java collections framework.
 * 
 * @author Max Voelkel
 * @see Iterator
 */
public interface ClosableIterator<E> extends Iterator<E> {
	
	/**
	 * The underlying implementation frees resources. For some it is absolutely
	 * necessary to call this method.
	 */
	void close();
}
