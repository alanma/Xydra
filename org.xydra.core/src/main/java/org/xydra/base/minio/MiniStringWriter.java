package org.xydra.base.minio;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RequiresAppEngine;


/**
 * @author voelkel
 * 
 *         Use toString() to obtain the produced String.
 */
@RunsInGWT(true)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class MiniStringWriter implements MiniWriter {
	
	private StringBuffer buf = new StringBuffer();
	
	public void close() throws MiniIOException {
	}
	
	public void flush() throws MiniIOException {
	}
	
	public void write(String string) throws MiniIOException {
		this.buf.append(string);
	}
	
	public void write(char c) throws MiniIOException {
		this.buf.append(c);
	}
	
	@Override
	public String toString() {
		return this.buf.toString();
	}
	
}
