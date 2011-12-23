package org.xydra.base.minio;

import java.io.IOException;
import java.io.Reader;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInAppEngine;
import org.xydra.annotations.RunsInGWT;


/**
 * Delegates to a standard java Reader.
 * 
 * @author voelkel
 * 
 */
@RunsInGWT(false)
@RunsInAppEngine(true)
@RequiresAppEngine(false)
public class MiniStreamReader implements MiniReader {
	
	private Reader r;
	
	public MiniStreamReader(Reader reader) {
		this.r = reader;
	}
	
	@Override
	public void close() throws MiniIOException {
		try {
			this.r.close();
		} catch(IOException e) {
			throw new MiniIOException(e);
		}
	}
	
	@Override
	public boolean markSupported() {
		return this.r.markSupported();
	}
	
	@Override
	public int read() {
		try {
			return this.r.read();
		} catch(IOException e) {
			throw new MiniIOException(e);
		}
	}
	
	@Override
	public int read(char[] cbuf, int off, int len) {
		try {
			return this.r.read(cbuf, off, len);
		} catch(IOException e) {
			throw new MiniIOException(e);
		}
	}
	
	@Override
	public void mark(int maxValue) {
		try {
			this.r.mark(maxValue);
		} catch(IOException e) {
			throw new MiniIOException(e);
		}
	}
	
	@Override
	public void reset() {
		try {
			this.r.reset();
		} catch(IOException e) {
			throw new MiniIOException(e);
		}
	}
	
	@Override
	public boolean ready() {
		try {
			return this.r.ready();
		} catch(IOException e) {
			throw new MiniIOException(e);
		}
	}
	
}
