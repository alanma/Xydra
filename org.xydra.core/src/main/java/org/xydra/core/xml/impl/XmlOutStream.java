package org.xydra.core.xml.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.xydra.annotations.RunsInJava;
import org.xydra.core.xml.XmlOut;


/**
 * Maps {@link XmlOut} to a {@link OutputStream}.
 * 
 */
@RunsInJava
public class XmlOutStream extends AbstractXmlOut {
	
	private final Writer writer;
	
	public XmlOutStream(OutputStream os) {
		try {
			this.writer = new OutputStreamWriter(os, "UTF-8");
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException("missing UTF-8 charset", e);
		}
		init();
	}
	
	public XmlOutStream(Writer writer) {
		this.writer = writer;
		init();
	}
	
	@Override
	protected void append(String str) {
		try {
			this.writer.write(str);
		} catch(IOException e) {
			throw new RuntimeException("error writing to stream", e);
		}
	}
	
	public void close() {
		try {
			this.writer.close();
		} catch(IOException e) {
			throw new RuntimeException("error closing stream", e);
		}
	}
	
	public void flush() {
		try {
			this.writer.flush();
		} catch(IOException e) {
			throw new RuntimeException("error flushing stream", e);
		}
	}
	
}
