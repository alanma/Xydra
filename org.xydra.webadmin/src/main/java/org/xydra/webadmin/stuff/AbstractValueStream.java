package org.xydra.webadmin.stuff;

import org.xydra.annotations.RequiresAppEngine;
import org.xydra.annotations.RunsInGWT;
import org.xydra.base.value.ValueType;
import org.xydra.base.value.XValueStream;
import org.xydra.log.Logger;
import org.xydra.log.LoggerFactory;


/**
 * Common parts for serialising XValues.
 * 
 * @author xamde
 * 
 */
@RunsInGWT(true)
@RequiresAppEngine(false)
public abstract class AbstractValueStream implements XValueStream {
	
	private static final Logger log = LoggerFactory.getLogger(AbstractValueStream.class);
	
	protected StringBuffer buf = new StringBuffer();
	
	public String getString() {
		return this.buf.toString();
	}
	
	@Override
	public void startValue() {
	}
	
	@Override
	public void endValue() {
	}
	
	@Override
	public void startCollection(ValueType type) {
		this.buf.append("[");
	}
	
	@Override
	public void endCollection() {
		this.buf.append("]");
	}
	
	@Override
	public void javaBoolean(Boolean a) {
		this.buf.append(a);
	}
	
	@Override
	public void javaDouble(Double a) {
		if(a == null) {
			javaNull();
		} else if(a.isInfinite() || a.isNaN()) {
			log.warn("Encoding an infinite/NaN-Double as null");
			javaNull();
		} else {
			// Shave off trailing zeros and decimal point, if possible.
			String s = a.toString();
			if(s.indexOf('.') > 0 && s.indexOf('e') < 0 && s.indexOf('E') < 0) {
				while(s.endsWith("0")) {
					s = s.substring(0, s.length() - 1);
				}
				if(s.endsWith(".")) {
					s = s.substring(0, s.length() - 1);
				}
			}
			this.buf.append(s);
		}
	}
	
	@Override
	public void javaInteger(Integer a) {
		this.buf.append(a.toString());
	}
	
	@Override
	public void javaLong(Long a) {
		this.buf.append(a.toString());
	}
	
	@Override
	public void javaString(String a) {
		this.buf.append(encode(a.toString()));
	}
	
	public abstract String encode(String s);
	
	@Override
	public void javaNull() {
		this.buf.append("null");
	}
	
}
