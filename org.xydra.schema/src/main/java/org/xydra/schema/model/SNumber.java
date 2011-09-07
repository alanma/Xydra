package org.xydra.schema.model;

public class SNumber extends SValue {
	
	public Number value;
	
	public SNumber(Number value) {
		super();
		this.value = value;
	}
	
	@Override
    public void toSyntax(StringBuffer buf) {
		buf.append(this.value.toString());
	}
	
}
