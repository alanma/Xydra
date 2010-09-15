package org.xydra.schema.model;

public class SName {
	
	public String name;
	
	public SName(String name) {
		super();
		this.name = name;
	}
	
	public void toSyntax(StringBuffer buf) {
		buf.append(this.name);
	}
	
	public static SName parse(String string) {
		return new SName(string);
	}
	
}
