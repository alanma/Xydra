package org.xydra.schema.model;

public class SType implements ISyntax {
	
	public SType(String typeName) {
		this.typeName = typeName;
	}
	
	/**
	 * e.g. for XBooleanListValue use 'BooleanList' here.
	 */
	public String typeName;
	
	public boolean isListType() {
		return this.typeName.endsWith("List");
	}
	
	public String toClassName() {
		return "X" + this.typeName + "Value";
	}
	
	public boolean classExists() {
		String className = toClassName();
		try {
			Class.forName(className);
			return true;
		} catch(ClassNotFoundException e) {
			return false;
		}
	}
	
	@Override
    public void toSyntax(StringBuffer buf) {
		buf.append(this.typeName);
	}
	
	public static SType parse(String string) {
		return new SType(string);
	}
	
}
