package org.xydra.schema.model;

public class SDouble extends SValue {
	
	public double value;
	
	public SDouble(double value) {
		super();
		this.value = value;
	}
	
	public void toSyntax(StringBuffer buf) {
		buf.append(this.value);
	}
	
}
