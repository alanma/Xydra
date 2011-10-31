package org.xydra.base.minio;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;


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
	
	@Override
	public void close() throws MiniIOException {
	}
	
	@Override
	public void flush() throws MiniIOException {
	}
	
	@Override
	public void write(String string) throws MiniIOException {
		this.buf.append(string);
	}
	
	@Override
	public void write(char c) throws MiniIOException {
		this.buf.append(c);
	}
	
	@Override
	public String toString() {
		return this.buf.toString();
	}
	
}
