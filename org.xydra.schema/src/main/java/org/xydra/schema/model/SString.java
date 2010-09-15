package org.xydra.schema.model;

public class SString extends SValue implements ISyntax {
	
	public static final char QUOTE = '\'';
	
	public String value;
	
	public SString(String value) {
		super();
		this.value = value;
	}
	
	public void toSyntax(StringBuffer buf) {
		buf.append(QUOTE);
		buf.append(this.value);
		buf.append(QUOTE);
	}
	
	public static SValue parse(String string) {
		if(!string.startsWith("" + QUOTE)) {
			throw new IllegalArgumentException("String must start with " + QUOTE);
		}
		if(!string.endsWith("" + QUOTE)) {
			throw new IllegalArgumentException("String must end with " + QUOTE);
		}
		String value = string.substring(1, string.length() - 1);
		return new SString(value);
	}
	
}
