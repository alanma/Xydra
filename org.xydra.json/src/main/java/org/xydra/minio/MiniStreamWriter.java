package org.xydra.minio;

import java.io.IOException;
import java.io.Writer;

import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RequiresAppEngine;



/**
 * Delegates to a standard java Writer.
 * 
 * @author voelkel
 * 
 */
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class MiniStreamWriter implements MiniWriter {
	
	private Writer w;
	
	public MiniStreamWriter(Writer w) {
		this.w = w;
	}
	
	public void close() throws MiniIOException {
		try {
			this.w.close();
		} catch(IOException e) {
			throw new MiniIOException(e);
		}
	}
	
	public void flush() throws MiniIOException {
		try {
			this.w.flush();
		} catch(IOException e) {
			throw new MiniIOException(e);
		}
	}
	
	public void write(String string) throws MiniIOException {
		try {
			this.w.write(string);
		} catch(IOException e) {
			throw new MiniIOException(e);
		}
	}
	
	public void write(char c) throws MiniIOException {
		try {
			this.w.write(c);
		} catch(IOException e) {
			throw new MiniIOException(e);
		}
		
	}
	
}
