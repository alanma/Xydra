package org.xydra.core.xml.impl;

import org.xydra.annotations.RunsInGWT;
import org.xydra.annotations.RunsInJava;
import org.xydra.core.xml.XmlOut;


/**
 * Maps {@link XmlOut} to a {@link StringBuffer}.
 * 
 * @author voelkel
 * 
 */
@RunsInJava
@RunsInGWT
public class XmlOutStringBuffer extends AbstractXmlOut {
	
	private final StringBuffer buf = new StringBuffer();
	
	public XmlOutStringBuffer() {
		init();
	}
	
	@Override
	protected void append(String str) {
		this.buf.append(str);
	}
	
	public void close() {
		// TODO Auto-generated method stub
	}
	
	public void flush() {
		// TODO Auto-generated method stub
	}
	
	public String getXml() {
		return this.buf.toString();
	}
	
}
