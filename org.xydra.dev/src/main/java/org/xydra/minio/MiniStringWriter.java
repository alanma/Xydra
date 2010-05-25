package org.xydra.minio;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;


/**
 * @author voelkel
 * 
 *         Use toString() to obtain the produced String.
 */
@RunsInGWT
@RunsInAppEngine
@RunsInJava
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
