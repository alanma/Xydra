package org.xydra.schema.model;

public class SBoolean extends SValue {
	
	public static final String TRUE = "true";
	public boolean value;
	
	public SBoolean(boolean value) {
		super();
		this.value = value;
	}
	
	@Override
    public void toSyntax(StringBuffer buf) {
		buf.append(this.value);
	}
	
}
